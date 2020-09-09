package cbio.workflows;

import net.corda.core.identity.CordaX500Name;

public interface Constants {
    CordaX500Name ANP = CordaX500Name.parse("O=Anp,L=Lagos,C=NG");
    int CBIO_UNIT_VALUE = 100;

    int CARBON_EMISSIONS_FACTOR = 2;
    int CARBON_REAGENT_FACTOR = 4;
}