package cbio.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import java.util.concurrent.atomic.AtomicInteger;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/examples/ArtContract.java for an example. */
public class CbioContract implements Contract {
    public static String ID = "cbio.contracts.CbioContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        if(tx.getCommand(0).getValue() instanceof  Commands.Issue) {
            verifyIssue(tx);
        }
        if(tx.getCommand(0).getValue() instanceof  Commands.Move) {
            verifyMove(tx);
        }
        if(tx.getCommand(0).getValue() instanceof  Commands.Redeem) {
            verifyRedeem(tx);
        }
    }

    private void verifyIssue(LedgerTransaction tx) {
        if(tx.getInputs().size() != 0) {
            throw new IllegalArgumentException("Issue does not expect input");
        }
        if(tx.getOutputs().size() != 1) {
            throw new IllegalArgumentException("Issue must generate one output");
        }

        CbioState cbioState = (CbioState) tx.getOutput(0);
        if(cbioState.getAmount() < 1)
            throw new IllegalArgumentException("Output must be a positive value");
    }

    private void verifyMove(LedgerTransaction tx) {
        if(tx.getInputs().size() < 1) {
            throw new IllegalArgumentException("Input required");
        }

        if(!(tx.getCommand(0)
                .getSigners()
                .contains(((CbioState)tx.getInput(0)).getOwner().getOwningKey()))){
            throw new IllegalArgumentException("Owner must Sign");
        }

        // Input amount must be equal to output amount
        AtomicInteger inputSum = new AtomicInteger();
        tx.getInputs().forEach(contractStateStateAndRef -> {
            CbioState inputState = (CbioState) contractStateStateAndRef.getState().getData();
            inputSum.set(inputSum.get() + inputState.getAmount());
        });

        AtomicInteger outputSum = new AtomicInteger();
        tx.getOutputs().forEach(contractStateTransactionState -> {
            outputSum.set(outputSum.get() + ((CbioState)contractStateTransactionState.getData()).getAmount());
        });

        if(inputSum.get() != outputSum.get())
            throw new IllegalArgumentException("Incorrect Spending");
    }

    private void verifyRedeem(LedgerTransaction tx) {

    }

    public interface Commands extends CommandData {
        class Issue implements Commands { }
        class Move implements Commands { }
        class Redeem implements Commands { }
    }
}
