package si.genlan.nam.helpers;

import com.novell.nidp.NIDPPrincipal;
import com.novell.nidp.common.authority.UserAuthority;
import org.w3c.dom.Element;

import java.util.Properties;

public class NIDPPricipalTest extends NIDPPrincipal {

    private String userIdentifier;

    public NIDPPricipalTest(String guid, String userIdentifier) {

        this((UserAuthority) null, guid);
        this.userIdentifier = userIdentifier;
    }

    public NIDPPricipalTest(UserAuthority userAuthority, String s) {
        super(userAuthority, s);
    }

    @Override
    public Element toXML(Element element, Properties properties) {
        return null;
    }

    @Override
    public String getUserIdentifier() {
        return userIdentifier;
    }

    @Override
    public boolean update(NIDPPrincipal nidpPrincipal) {
        return false;
    }

    @Override
    public String toHTML() {
        return null;
    }
}
