# Cenario 1
* Emissao cbio

# Cenario 2 
* Transferencia entre empresas

# Cenario 3
* Aposentadoria do cbio

##### Issue

    flow start CbioIssueFlowInitiator typeFuel: "E1GC", qtyEmission: 1000, qtyReagent: 300
    
##### Move

    flow start CbioMoveFlowInitiator newOwner: Petrobras, amount: 500, paidValue: 1000

##### Redeem

    flow start CbioRedeemFlowInitiator amount: 100
    
    
* Vault Query:

    `run vaultQuery contractStateType: contracts.CbioState`