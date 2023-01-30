package com.template.contracts;

import com.template.states.Balance;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class BalanceContract implements Contract {
    public static final String ID = "com.template.contracts.BalanceContract";

    /**
     * All contract instances implement the verify function
     * veryify is automatically called on a transaction when it is executed.
     */
    //note: all of the .get(0) functions are due to our tx only having one input/output.
    //we can add functionality for more inputs/outputs per tx.

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        //Extract the command type from the transaction
        final CommandData commandType = tx.getCommands().get(0).getValue();
        /**
         * An Issue command should not use any input states, and should only create one output state (basic)
         * An issued balance must  be 0, currency must be specified (balance specific)
         */
        if (commandType instanceof BalanceContract.Commands.Issue) {
            Balance output = tx.outputsOfType(Balance.class).get(0);

            requireThat(require -> {
                //basic requirements
                require.using("No inputs should be consumed when issuing a new balance.", tx.getInputStates().size() == 0);
                require.using("Exactly one output should be created when issuing a new balance.", tx.getOutputStates().size() == 1);
                //balance specific
                require.using("Balance must be 0", output.getBalance() == 0);
                require.using("Currency must be specified", !output.getCurrency().equals(""));
                return null;
            });
        }

        /**
         * Deposit/Withdraw - only one field should be edited. all other variables should
         *                    be the same before and after transaction
         */
        else if (commandType instanceof BalanceContract.Commands.Deposit) {
            Balance input = tx.inputsOfType(Balance.class).get(0);
            Balance output = tx.outputsOfType(Balance.class).get(0);

            requireThat(require -> {
                //basic requirements
                require.using("Exactly one input should be consumed when issuing a new balance.", tx.getInputStates().size() == 1);
                require.using("Exactly one output should be created when issuing a new balance.", tx.getOutputStates().size() == 1);
                //operation specific
                require.using("only moneyIn changes", input.getMoneyOut() = output.getMoneyOut() &&
                        input.getCurrency() = output.getCurrency() &&
                        input.getIssuer() = output.getIssuer() &&
                        input.getLinearId() = output.getLinearId());
                return null;
            });
        }
        else if (commandType instanceof  BalanceContract.Commands.Withdraw) {
            Balance input = tx.inputsOfType(Balance.class).get(0);
            Balance output = tx.outputsOfType(Balance.class).get(0);

            requireThat(require -> {
                //basic requirements
                require.using("Exactly one input should be consumed when issuing a new balance.", tx.getInputStates().size() == 1);
                require.using("Exactly one output should be created when issuing a new balance.", tx.getOutputStates().size() == 1);
                //operation specific
                require.using("only moneyOut changes", input.getMoneyIn() = output.getMoneyIn() &&
                        input.getCurrency() = output.getCurrency() &&
                        input.getIssuer() = output.getIssuer() &&
                        input.getLinearId() = output.getLinearId());
                return null;
            });
        }
        else {
            //Unrecognized Command type
            throw new IllegalArgumentException("Incorrect type of Balance Commands");
        }
    }
    /**
     * Commands indicate the transaction’s intent:
     * what type of actions performed by the state the contract can verify.
     * Our state can perform three actions: IssueBalance, UpdateMoneyIn and UpdateMoneyOut
     */
    public interface Commands extends CommandData {
        class Issue implements BalanceContract.Commands {}
        class Deposit implements BalanceContract.Commands {}
        class Withdraw implements BalanceContract.Commands {}
    }
}