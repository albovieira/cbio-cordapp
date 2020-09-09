package cbio.workflows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

// `InitiatedBy` means that we will start this flow in response to a
// message from `CbioIssueFlowInitiator`.
@InitiatedBy(CbioIssueFlowInitiator.class)
public class CbioIssueFlowResponder extends FlowLogic<Void> {
    private final FlowSession anpSession;

    // Responder flows always have a single constructor argument - a
    // `FlowSession` with the counterparty who initiated the flow.
    public CbioIssueFlowResponder(FlowSession anpSession) {
        this.anpSession = anpSession;
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
        final Party ANP = getOurIdentity();
        if (!ANP.getName().equals(Constants.ANP)) {
            throw new FlowException("Only ANP can issue news CBIOS");
        }
        SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(this.anpSession) {
              @Override
              @Suspendable
              protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

              }
          }
        );

        subFlow(new ReceiveFinalityFlow(anpSession, signedTransaction.getId()));

        // Once the counterparty calls `FinalityFlow`, we will
        // automatically record the transaction if we are one of the
        // `participants` on one or more of the transaction's states.

        return null;
    }
}