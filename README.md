# CBIO Cordapp

## Set up

1. Download and install a JDK 8 JVM (minimum supported version 8u131)
2. Download and install IntelliJ Community Edition (supported versions 2017.x and 2018.x)
3. Open IntelliJ. From the splash screen, click `Import Project`
4. Select `Import project from external model > Gradle > Next > Finish`
5. Click `File > Project Structure…` and select the Project SDK (Oracle JDK 8, 8u131+)
7. Open the `Project` view by clicking `View > Tool Windows > Project`
8. Run the test in `src/test/java/ProjectImportedOKTest.java`. It should pass!

## Running our CorDapp

Normally, you'd interact with a CorDapp via a client or webserver. So we can
focus on our CorDapp, we'll be running it via the node shell instead.

Once you've finished the CorDapp's code, run it with the following steps:

* Build a test network of nodes by opening a terminal window at the root of
  your project and running the following command:

    * Windows:   `gradlew.bat deployNodes`
    * macOS:     `./gradlew deployNodes`
    

* Start the nodes by running the following command:

    * Windows:   `build\nodes\runnodes.bat`
    * macOS:     `build/nodes/runnodes`
    * linux:     `./deploy.sh`

## Components

### CbioState

States define shared facts on the ledger. Our state, CbioState, will define a CBIO. 
It will have the following structure:

    -------------------
    |                 |
    |   CbioState     |
    |                 |
    |   - owner       |
    |   - amount      |
    |   - paidValue   |
    |   - lastOwner   |
    -------------------

### CbioContract

Contracts govern how states evolve over time. Our contract, CbioContract,
will define how CbioStates evolve. 

### CbioIssueInitiatorFlow


### CbioMoveInitiatorFlow

### CbioRedeemInitiatorFlow


## Commands 
### Issue:
    //Acceptable values for typeFuel E1GC,E1GM,E2G,BIODIESEL,E1G2G,
    flow start CbioIssueFlowInitiator typeFuel: "E1GC", qtyEmission: 1000, qtyReagent: 300

### Move
    flow start CbioMoveFlowInitiator newOwner: Petrobras, amount: 500, paidValue: 1000

### Redeem
    flow start CbioRedeemFlowInitiator amount: 100

### Vault Query:

    run vaultQuery contractStateType: cbio.contracts.CbioState
