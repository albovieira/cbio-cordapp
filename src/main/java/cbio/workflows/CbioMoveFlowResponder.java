package cbio.workflows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

// `InitiatedBy` means that we will start this flow in response to a
// message from `CbioIssueFlowInitiator`.
@InitiatedBy(CbioMoveFlowInitiator.class)
public class CbioMoveFlowResponder extends FlowLogic<Void> {
    private final FlowSession counterPartySession;

    // Responder flows always have a single constructor argument - a
    // `FlowSession` with the counterparty who initiated the flow.
    public CbioMoveFlowResponder(FlowSession counterPartySession) {
        this.counterPartySession = counterPartySession;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // If the counterparty requests our signature on a transaction using
        // `CollectSignaturesFlow`, we need to respond by invoking our own
        // `SignTransactionFlow` subclass.
        SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(this.counterPartySession) {
              @Override
              @Suspendable
              protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

              }
          }
        );

        subFlow(new ReceiveFinalityFlow(counterPartySession, signedTransaction.getId()));

        // Once the counterparty calls `FinalityFlow`, we will
        // automatically record the transaction if we are one of the
        // `participants` on one or more of the transaction's states.

        return null;
    }
}