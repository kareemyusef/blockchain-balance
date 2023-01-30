package com.template.states;
import com.templates.contracts.BalanceContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
@BelongsToContract(BalanceContract.class)
public class Balance implements LinearState {
    /**
     * Private variables:
     *      moneyIn, moneyOut, balance - doubles representing account balance.
     *      currency - String representing currency
     *      issuer - always this node.
     *      linearId - reperesents the user/owner of this balance.
     *                 linear id stays with the state as it changes over time.
     *      participants - required of all corda states. Will only include issuer.
     */
    private double moneyIn, moneyOut, balance;
    private String currency;
    private Party issuer;
    //implement LinearState
    private UniqueIdentifier linearID;

    private List<AbstractParty> participants;
    /**
     * Constructor - balance and particpants are based off other variables.
     * linearId, currency and issuer are one time
     * moneyIn and moneyOut will be created in flows.
     */
    public Balance(double moneyIn, double moneyOut, String currency,
                   Party issuer, UniqueIdentifier linearID) {
        this.moneyIn = moneyIn;
        this.moneyOut = moneyOut;
        this.currency = currency;
        this.issuer = issuer;
        this.linearID = linearID;

        //will never need to edit these
        this.participants.add(issuer);
        this.balance = moneyIn - moneyOut;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() { return participants; }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() { return linearID; }

    public double getMoneyIn() { return moneyIn; }

    public double getMoneyOut() { return moneyOut; }

    public double getBalance() { return balance; }

    public String getCurrency() { return currency; }

    public Party getIssuer() { return issuer; }

}



