package cbio;

import cbio.contracts.CbioState;
import cbio.workflows.CbioMoveFlowInitiator;
import cbio.workflows.CbioRedeemFlowInitiator;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.FlowProgressHandle;
import net.corda.testing.core.TestIdentity;
import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.node.services.Vault;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;
import org.junit.Assert;
import org.junit.Test;
import cbio.workflows.CbioIssueFlowInitiator;
import cbio.workflows.Constants;

import java.util.List;
import static net.corda.testing.driver.Driver.driver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IntegrationTest {

    private final TestIdentity ANPIdentity = new TestIdentity(Constants.ANP);
    private final TestIdentity partyAIdentity = new TestIdentity(new CordaX500Name("PartyA", "New York", "US"));
    private final TestIdentity partyBIdentity = new TestIdentity(new CordaX500Name("PartyB", "Mumbai", "IN"));

    @Test
    public void shouldIssue() {
        driver(new DriverParameters()
                .withIsDebug(true)
                .withStartNodesInProcess(true)
                , dsl -> {
            // Start a pair of nodes and wait for them both to be ready.
            List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
                    dsl.startNode(new NodeParameters().withProvidedName(ANPIdentity.getName())),
                    dsl.startNode(new NodeParameters().withProvidedName(partyAIdentity.getName()))
            );

            try {
                NodeHandle ANPHandle = handleFutures.get(0).get();
                NodeHandle partyAHandle = handleFutures.get(1).get();

                Party ANP = ANPHandle.getNodeInfo().getLegalIdentities().get(0);
                Party partyA = partyAHandle.getNodeInfo().getLegalIdentities().get(0);

                // Run issue transaction using rpc
                String TYPE_FUEL = "E1GC";
                int qtyEmission = 1000;
                int qtyReagent = 300;
                partyAHandle.getRpc().startTrackedFlowDynamic(CbioIssueFlowInitiator.class, TYPE_FUEL, qtyEmission, qtyReagent)
                        .getReturnValue().get();

                // Query Node A
                Vault.Page<CbioState> cbioStateOutput = partyAHandle.getRpc().vaultQuery(CbioState.class);
                assertEquals(1, cbioStateOutput.getStates().size());

                CbioState issuedCbios = cbioStateOutput.getStates().get(0).getState().getData();
//                assertEquals(ANP, issuedCbios.getIssuer());
                assertEquals(partyA, issuedCbios.getOwner());
                assertEquals(800, issuedCbios.getAmount());

            } catch (Exception e) {
                System.err.println("Encountered exception in node startup: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Caught exception during test: ");
            }

            return null;
        });
    }

    @Test
    public void shouldMove() {
        driver(new DriverParameters()
                        .withIsDebug(true)
                        .withStartNodesInProcess(true)
                , dsl -> {
                    // Start a pair of nodes and wait for them both to be ready.
                    List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
                            dsl.startNode(new NodeParameters().withProvidedName(ANPIdentity.getName())),
                            dsl.startNode(new NodeParameters().withProvidedName(partyAIdentity.getName())),
                            dsl.startNode(new NodeParameters().withProvidedName(partyBIdentity.getName()))
                    );

                    try {
                        NodeHandle ANPHandle = handleFutures.get(0).get();
                        NodeHandle partyAHandle = handleFutures.get(1).get();
                        NodeHandle partyBHandle = handleFutures.get(2).get();

                        Party ANP = ANPHandle.getNodeInfo().getLegalIdentities().get(0);
                        Party partyA = partyAHandle.getNodeInfo().getLegalIdentities().get(0);
                        Party partyB = partyBHandle.getNodeInfo().getLegalIdentities().get(0);

                        // Run issue transaction using rpc
                        String TYPE_FUEL = "E1GC";
                        int qtyEmission = 1000;
                        int qtyReagent = 300;
                        partyAHandle.getRpc().startTrackedFlowDynamic(CbioIssueFlowInitiator.class, TYPE_FUEL, qtyEmission, qtyReagent)
                                .getReturnValue().get();

                        // move transaction
                        partyAHandle.getRpc().startTrackedFlowDynamic(CbioMoveFlowInitiator.class, partyB, 500, 1000)
                                .getReturnValue().get();

                        // Query Node A
                        Vault.Page<CbioState> partyAOuput = partyAHandle.getRpc().vaultQuery(CbioState.class);
                        assertEquals(1, partyAOuput.getStates().size());

                        CbioState partACbios = partyAOuput.getStates().get(0).getState().getData();
                        assertEquals(partyA, partACbios.getOwner());
                        assertEquals(300, partACbios.getAmount());

                        // Query Node B
                        Vault.Page<CbioState> partyBOuput = partyBHandle.getRpc().vaultQuery(CbioState.class);
                        assertEquals(1, partyBOuput.getStates().size());

                        CbioState partBCbios = partyBOuput.getStates().get(0).getState().getData();
                        assertEquals(partyB, partBCbios.getOwner());
                        assertEquals(500, partBCbios.getAmount());

                    } catch (Exception e) {
                        System.err.println("Encountered exception in node startup: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("Caught exception during test: ");
                    }

                    return null;
                });
    }

    @Test
    public void shouldRedeem() {
        driver(new DriverParameters()
                        .withIsDebug(true)
                        .withStartNodesInProcess(true)
                , dsl -> {
                    // Start a pair of nodes and wait for them both to be ready.
                    List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
                            dsl.startNode(new NodeParameters().withProvidedName(ANPIdentity.getName())),
                            dsl.startNode(new NodeParameters().withProvidedName(partyAIdentity.getName())),
                            dsl.startNode(new NodeParameters().withProvidedName(partyBIdentity.getName()))
                    );

                    try {
                        NodeHandle ANPHandle = handleFutures.get(0).get();
                        NodeHandle partyAHandle = handleFutures.get(1).get();
                        NodeHandle partyBHandle = handleFutures.get(2).get();

                        Party ANP = ANPHandle.getNodeInfo().getLegalIdentities().get(0);
                        Party partyA = partyAHandle.getNodeInfo().getLegalIdentities().get(0);
                        Party partyB = partyBHandle.getNodeInfo().getLegalIdentities().get(0);

                        // Run issue transaction using rpc
                        String TYPE_FUEL = "E1GC";
                        int qtyEmission = 1000;
                        int qtyReagent = 300;
                        partyAHandle.getRpc().startTrackedFlowDynamic(CbioIssueFlowInitiator.class, TYPE_FUEL, qtyEmission, qtyReagent)
                                .getReturnValue().get();

                        // move transaction
                        partyAHandle.getRpc().startTrackedFlowDynamic(CbioMoveFlowInitiator.class, partyB, 500, 1000)
                                .getReturnValue().get();

                        // partyA redeem 100 cbio
                        partyAHandle.getRpc().startTrackedFlowDynamic(CbioRedeemFlowInitiator.class, 100)
                                .getReturnValue().get();

                        // partyB redeem 200 cbio
                        partyBHandle.getRpc().startTrackedFlowDynamic(CbioRedeemFlowInitiator.class, 200)
                                .getReturnValue().get();

                        // Query Node A
                        Vault.Page<CbioState> partyAOutput = partyAHandle.getRpc().vaultQuery(CbioState.class);
                        CbioState partACbios = partyAOutput.getStates().get(0).getState().getData();
                        assertEquals(partyA, partACbios.getOwner());
                        assertEquals(200, partACbios.getAmount());

                        Vault.Page<CbioState> partyBOutput = partyBHandle.getRpc().vaultQuery(CbioState.class);
                        CbioState partBCbios = partyBOutput.getStates().get(0).getState().getData();
                        assertEquals(partyB, partBCbios.getOwner());
                        assertEquals(300, partBCbios.getAmount());

                    } catch (Exception e) {
                        System.err.println("Encountered exception in node startup: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("Caught exception during test: ");
                    }

                    return null;
                });
    }
}
