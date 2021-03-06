package cbio.workflows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import cbio.contracts.CbioContract;
import cbio.contracts.CbioState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@InitiatingFlow
@StartableByRPC
public class CbioRedeemFlowInitiator extends FlowLogic<SignedTransaction> {
    private final int amount;

    public CbioRedeemFlowInitiator(int amount) {
        this.amount = amount;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        /*
         * the node that receive the call to sell
         * will check cbiobalance and remove balance and send to new owner
         */
        final Party ANP = getServiceHub().getNetworkMapCache().getPeerByLegalName(Constants.ANP);
        if (ANP == null) {
            throw new FlowException("ANP node is invalid");
        }

        // get all output states not consumed
        List<StateAndRef<CbioState>> allTokenStateAndRefs =
                getServiceHub().getVaultService().queryBy(CbioState.class).getStates();

        AtomicInteger totalTokenAvailable = new AtomicInteger();
        List<StateAndRef<CbioState>> inputStateAndRef = new ArrayList<>();
        AtomicInteger change = new AtomicInteger(0);

        // loop into all outputs and increment value to set the input state value of transfer
        // and generate an ouput state with new balance
        List<StateAndRef<CbioState>> tokenStateAndRefs =  allTokenStateAndRefs.stream()
                .peek(tokenStateStateAndRef -> {
                    if(totalTokenAvailable.get() < amount)
                        inputStateAndRef.add(tokenStateStateAndRef);

                    //Increment until reach the amount value
                    totalTokenAvailable.set(totalTokenAvailable.get() + tokenStateStateAndRef.getState().getData().getAmount());

                    // new value that must be created to current node
                    if(change.get() == 0 && totalTokenAvailable.get() > amount){
                        change.set(totalTokenAvailable.get() - amount);
                    }
                }).collect(Collectors.toList());
//
        // Validate if there is sufficient tokens to spend
        if(totalTokenAvailable.get() < amount){
            throw new FlowException("Insufficient balance");
        }
        // create new output state
        CbioState outputState = new CbioState(ANP, amount, 0, getOurIdentity());

        // create transaction with the input generated by iteraction above
        TransactionBuilder txBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache()
                .getNotaryIdentities().get(0))
                .addOutputState(outputState)
                .addCommand(new CbioContract.Commands.Move(), ImmutableList.of(getOurIdentity().getOwningKey(), ANP.getOwningKey()));
        inputStateAndRef.forEach(txBuilder::addInputState);

        // if still has balance, create a new output state for current node
        if(change.get() > 0){
            CbioState changeState = new CbioState(getOurIdentity(), change.get(), 0, null);
            txBuilder.addOutputState(changeState);
        }

        txBuilder.verify(getServiceHub());

        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(txBuilder);

        FlowSession anpSession = initiateFlow(ANP);

        // collect signatures
        SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, ImmutableList.of(anpSession)));

        return subFlow(new FinalityFlow(fullySignedTransaction, ImmutableList.of(anpSession)));
    }
}