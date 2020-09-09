package cbio.services;

import net.corda.core.flows.FlowException;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;

public class Factors {
    public static int getCarbonEmissionsFactor(String typeFuel) throws FlowException {
        return Factors.getFuelFactors(typeFuel).getInt("carbonEmissionsFactor");
    }

    public static int getCarbonReagentFactor(String typeFuel) throws FlowException {
        return Factors.getFuelFactors(typeFuel).getInt("carbonReagentFactor");
    }

    private static JsonObject getFuelFactors(String typeFuel) throws FlowException {
        JsonObject E1GC = Json.createObjectBuilder()
                .add("carbonEmissionsFactor", 2)
                .add("carbonReagentFactor", 4).build();
        JsonObject E1GM = Json.createObjectBuilder()
                .add("carbonEmissionsFactor", 2)
                .add("carbonReagentFactor", 5).build();
        JsonObject E2G = Json.createObjectBuilder()
                .add("carbonEmissionsFactor", 1)
                .add("carbonReagentFactor", 4).build();

        JsonObject BIODIESEL = Json.createObjectBuilder()
                .add("carbonEmissionsFactor", 2)
                .add("carbonReagentFactor", 5).build();
        JsonObject E1G2G = Json.createObjectBuilder()
                .add("carbonEmissionsFactor", 2)
                .add("carbonReagentFactor", 5).build();

        HashMap<String, JsonObject> map =
                new HashMap<String, JsonObject>();
        map.put("E1GC", E1GC);
        map.put("E1GM", E1GM);
        map.put("E2G", E2G);
        map.put("BIODIESEL", BIODIESEL);
        map.put("E1G2G", E1G2G);
        JsonObject factors = (JsonObject) map.get(typeFuel);

        if(factors == null) {
            throw new FlowException("Invalid fuel type");
        }
        return factors;
    }
}
