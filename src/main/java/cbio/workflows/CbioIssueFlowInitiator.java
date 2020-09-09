package cbio.workflows;

import cbio.services.Factors;
import co.paralleluniverse.fibers.Suspendable;
import cbio.contracts.CbioContract;
import cbio.contracts.CbioState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;


import static java.util.Collections.singletonList;

@InitiatingFlow
@StartableByRPC
public class CbioIssueFlowInitiator extends FlowLogic<SignedTransaction> {
    private final String typeFuel;
    private final int qtyEmission;
    private final int qtyReagent;

    public CbioIssueFlowInitiator(String typeFuel, int qtyEmission, int qtyReagent) {
        this.typeFuel = typeFuel;
        this.qtyEmission = qtyEmission;
        this.qtyReagent = qtyReagent;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        final Party ANP = getServiceHub().getNetworkMapCache().getPeerByLegalName(Constants.ANP);
        final Party owner = getOurIdentity();

        if (ANP == null) {
            throw new FlowException("ANP node is invalid");
        }

        // it converts fuel to cbio
        int factorEmission = Factors.getCarbonEmissionsFactor(typeFuel);
        int factorReagent = Factors.getCarbonReagentFactor(typeFuel);
        final int amount = (qtyEmission * factorEmission ) - (qtyReagent * factorReagent);

        if(amount <= 0) {
            throw new FlowException("Cbio amount must be greather than 0");
        }

        // create state with ANP as issuer, and the node that request as owner
        CbioState tokenState = new CbioState(owner, amount, 0, null);

        TransactionBuilder transactionBuilder = new TransactionBuilder(notary);
        // use command issue for this flow
        CbioContract.Commands.Issue commandData = new CbioContract.Commands.Issue();
        // add to transaction the keys of ANP and Owner
        transactionBuilder.addCommand(commandData, ANP.getOwningKey(), owner.getOwningKey());

        // Set contract
        transactionBuilder.addOutputState(tokenState, CbioContract.ID);
        // Check contract validations
        transactionBuilder.verify(getServiceHub());
        // owner sign
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        // create a session with ANP node
        FlowSession anpSession = initiateFlow(ANP);

        // collect signatures
        SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, singletonList(anpSession)));

        return subFlow(new FinalityFlow(fullySignedTransaction, singletonList(anpSession)));
    }

}