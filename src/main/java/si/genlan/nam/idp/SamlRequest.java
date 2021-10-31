package si.genlan.nam.idp;

public class SamlRequest {
    public String identifier;
    public String message;
    public long timeAdded;

    public SamlRequest(String id, String message) {
        this.message = message;
        this.identifier = id;
        timeAdded = System.currentTimeMillis();
    }
}
