package si.genlan.nam.idp;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SamlRequestRepository {
    private List<SamlRequest> samlRequests = new ArrayList<>();

    public void add(SamlRequest saml) {
        samlRequests.add(saml);
    }

    public void removeRequestsOlderThan5Minutes() {
        long timeNow = System.currentTimeMillis();
        ArrayList<SamlRequest> toDelete = new ArrayList<SamlRequest>();
        for (SamlRequest req : samlRequests) {
            if ((timeNow - req.timeAdded) > (5 * 60 * 1000)) {
                toDelete.add(req);
            }
        }
        for (SamlRequest req : toDelete) {
            samlRequests.remove(req);
        }
    }

    public String getLast(String keyName) {
        for (int i = samlRequests.size() - 1; i >= 0; i++) {
            if (samlRequests.get(i).identifier.equals(keyName)) {
                return samlRequests.get(i).message;
            }
        }
        return null;
    }

    public void removeFromUser(String userId) {
        ArrayList<SamlRequest> toDelete = new ArrayList<SamlRequest>();
        for (SamlRequest req : samlRequests) {
            if (req.identifier.equals(userId)) {
                toDelete.add(req);
            }
        }
        for (SamlRequest req : toDelete)
            samlRequests.remove(req);

    }

    public void removeSamlRequest(String saml) {

        if (saml != null) {
            List<SamlRequest> toDelete = new ArrayList<>();
            for (SamlRequest req : samlRequests) {
                if (req.message.equals(saml)) {
                    toDelete.add(req);
                }
            }
            for (SamlRequest req : toDelete) {
                samlRequests.remove(req);
            }
        }

    }
}
