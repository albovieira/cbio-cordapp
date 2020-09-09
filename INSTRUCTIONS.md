# Flows
 - Emissao CBIO
 - Transferencia entre empresas
 - Aposentadoria do CBIO

##### Issue

    flow start CbioIssueFlowInitiator typeFuel: "E1GC", qtyEmission: 1000, qtyReagent: 300
    
##### Move

    flow start CbioMoveFlowInitiator newOwner: Petrobras, amount: 500, paidValue: 1000

##### Redeem

    flow start CbioRedeemFlowInitiator amount: 100
    
    
##### Vault Query:

    `run vaultQuery contractStateType: cbio.contracts.CbioState`