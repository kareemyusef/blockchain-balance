package com.template.flows;
import com.template.contracts.BalanceContract;
import com.template.states.Balance;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.UUID;
public class Withdraw {
    @InitiatingFlow
    @StartableByRPC
    public static class WithdrawInitiator extends FlowLogic<SignedTransaction>{
        /**
         * Flow Parameters
         *      - The only parameters needed to withdraw are amount and linearId.
         *      - issuer, currency, and moneyOut all stay the same.
         */
        private UniqueIdentifier balanceId; //tells us which account to deposit into
        private double amount;

        public WithdrawInitiator(UniqueIdentifier balanceId, double amount) throws IllegalArgumentException {
            if (!(amount > 0)) {
                throw new IllegalArgumentException("amount must be greater than 0");
            }
            this.balanceId = balanceId;
            this.amount = amount;
        }

        public SignedTransaction call() throws FlowException {
            /**
             * In transactions with multiple parties, we need a notary to reach consenus.
             * There are two methods
             *          1. Use the first Notary on the network
             *          2. Specify a notary to use
             * Second option is better for a non-test environment
             */
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
            //final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2
            /**
             * QueryCriteria - query the vault by UniqueIdentifier for unconsumed state
             *               - this will give us the balance specified by the parameters, or originalBalance
             */
            String targetBalance = balanceId.toString();
            QueryCriteria.LinearStateQueryCriteria inputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                    .withUuid(Arrays.asList(UUID.fromString(targetBalance)))
                    .withStatus(Vault.StateStatus.UNCONSUMED)
                    .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT);
            /**
             * Input State - using inputCriteria, we retrieve the balance with the correct linearId
             *             - we want to consume this state and output a new state with the withdrawn balance.
             */
            StateAndRef balanceStateAndRef = getServiceHub().getVaultService().queryBy(Balance.class, inputCriteria).getStates().get(0);
            Balance originalBalance = (Balance) balanceStateAndRef.getState().getData();
            /**
             *  Output State - newMoneyOut should be oldMoneyOut plus this.amount
             *               - moneyIn, currency, issuer, linearId should all stay the same.
             */
            double newMoneyOut = originalBalance.getMoneyOut() + amount;
            Balance output = new Balance(originalBalance.getMoneyIn(), newMoneyOut,
                    originalBalance.getCurrency(), originalBalance.getIssuer(),
                    originalBalance.getLinearId());
            /**
             * Build Transaction - specify notary, input/output states, and corresponding contract command.
             *                   - command requires an owning key. we will use this node's (our) key.
             */
            TransactionBuilder txBuilder = newTransactionBuilder(notary)
                    .addInputState(balanceStateAndRef)
                    .addOutputState(output)
                    .addCommand(new BalanceContract.Commands.Deposit(),
                            this.getOurIdentity().getOwningKey());
            /**
             * Verify transaction
             */
            txBuilder.verify(getServiceHub());
            /**
             * Sign transaction
             */
            SignedTransaction sTx = getServiceHub().signInitialTransaction(txBuilder);
            /**
             * Notarise transaction and record states in ledger
             */
            return subFlow( new FinalityFlow(sTx, Collections.emptyList()) );
            /**
             * CordaDocs - FinalityFlow -
             *          Verifies the given transaction, then sends it to the named notary.
             *          If the notary agrees that the transaction is acceptable then
             *          it is from that point onwards committed to the ledger, and will be written through to the vault.
             *          Additionally it will be distributed to the parties reflected in the participants list of the states.
             */
        }
    }
}