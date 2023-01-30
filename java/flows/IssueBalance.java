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
public class IssueBalance {
    @InitiatingFlow
    @StartableByRPC
    public static class IssueBalanceInitiator extends FlowLogic<SignedTransaction>{
        /**
         * Flow Parameters
         *      - The only parameter needed to initate balance is currency
         *      - issuer is this node, balance is 0, and linearId is created within the flow.
         */
        private String initialCurrency;
        public IssueBalanceInitiator(String initialCurrency) { this.initialCurrency = initialCurrency; }

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
             * Output State - initial balance starts at 0
             *              - currency is passed as a flow parameter.
             *              - issuer is this node.
             *              - linearId is created here
             */
            UniqueIdentifier newId = new UniqueIdentifier();
            Balance output = new Balance(0, 0, this.initialCurrency, this.getOurIdentity(), newId);
            /**
             * Build Transaction - specify notary, input/output states, and corresponding contract command.
             *                   - command requires an owning key. we will use this node's (our) key.
             */
            TransactionBuilder txBuilder = newTransactionBuilder(notary)
                                           .addOutputState(output)
                                           .addCommand(new BalanceContract.Commands.Issue(),
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