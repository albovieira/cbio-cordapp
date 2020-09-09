package cbio.contracts;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/* Our state, defining a shared fact on the ledger.
 * See src/main/java/examples/ArtState.java for an example. */
@BelongsToContract(CbioContract.class)
public class CbioState implements ContractState {

    final private Party owner;
    final private int amount;
    final private int paidValue;
    @Nullable
    final private Party lastOwner;
    private final List<AbstractParty> participants;

    public CbioState(Party owner, int amount, int paidValue, @Nullable Party lastOwner) {
        this.lastOwner = lastOwner;
        this.owner = owner;
        this.amount = amount;
        this.paidValue = paidValue;

        this.participants = new ArrayList<>();
        this.participants.add(owner);

    }

    @Override
    @NotNull
    public List<AbstractParty> getParticipants() {
        return participants;
    }

    public Party getOwner() {
        return owner;
    }

    public int getAmount() {
        return amount;
    }

    public Party getLastOwner() {
        return lastOwner;
    }

    public int getPaidValue() {
        return paidValue;
    }
}