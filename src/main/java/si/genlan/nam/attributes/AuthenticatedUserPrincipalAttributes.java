package si.genlan.nam.attributes;

import com.novell.nidp.NIDPConstants;
import com.novell.nidp.NIDPPrincipal;
import com.novell.nidp.NIDPSession;
import com.novell.nidp.NIDPSubject;
import si.genlan.nam.idp.Tracer;
import si.genlan.nam.idp.UpdateUserStoreBySamlResponseContract;

import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

public class AuthenticatedUserPrincipalAttributes {
    public NIDPPrincipal getUserPrincipal(NIDPPrincipal principal, Properties m_Properties, NIDPSession m_Session, Tracer tracer) {
        NIDPPrincipal idpPrincipal = principal;
        if (idpPrincipal == null) {
            idpPrincipal = getContractUser(m_Properties, tracer);

            if (idpPrincipal == null) {
                idpPrincipal = getSessionUser(m_Session, tracer);
            }
        }
        return idpPrincipal;
    }

    public void getUserPrincipalAttributes(HttpServletRequest m_Request, Tracer tracer, UpdateUserStoreBySamlResponseContract authenticationContract) {
        Attributes attr;
        String[] attrNames = {"sn", "mail"};

        String id = m_Request.getParameter(NIDPConstants.PARM_USERID);
        String saml2 = m_Request.getParameter(NIDPConstants.SAML2);
        tracer.trace("getUPA: id: " + id);
        tracer.trace("getUPA: saml2: " + saml2);
        attr = authenticationContract.getPrincipalAttributesPublic(attrNames);
        if (attr != null)
            tracer.trace("Principal attebiutes length: " + attr.size());
        else
            tracer.trace("No principal attributes found");
    }

    public NIDPPrincipal getContractUser(Properties m_Properties, Tracer tracer) {
        NIDPPrincipal contractUser = (NIDPPrincipal) m_Properties.get("Principal");
        if (contractUser != null) {
            tracer.trace("Found contract authenticated user: ", contractUser.getUserIdentifier());
        }
        return contractUser;
    }

    public NIDPPrincipal getSessionUser(NIDPSession m_Session, Tracer tracer) {
        if (m_Session.isAuthenticated()) {
            NIDPPrincipal[] idpPrincipalList = m_Session.getSubject().getPrincipals();
            if (idpPrincipalList.length == 1) {
                NIDPPrincipal sessionPricipal = idpPrincipalList[0];
                tracer.trace("getSessionUser: Found session authenticated user: ", sessionPricipal.getUserIdentifier());
                return sessionPricipal;
            }
        }
        return null;
    }

    public NIDPPrincipal resolveUserPrincipal(Tracer tracer, String sessionUser, NIDPSession m_Session, Properties m_Properties, UpdateUserStoreBySamlResponseContract authenticationClass) {
        tracer.trace("resolveUserPrincipal: getting principal from properties (contract)");
        NIDPPrincipal nidpprincipal = (NIDPPrincipal) m_Properties.get("Principal");

        if (nidpprincipal == null) {
            tracer.trace("resolveUserPrincipal: getting user from session");
            if (sessionUser != null) {
                if (m_Session.isAuthenticated()) {
                    NIDPSubject nidpsubject = m_Session.getSubject();
                    NIDPPrincipal[] anidpprincipal = nidpsubject.getPrincipals();
                    if (anidpprincipal.length == 1) {
                        nidpprincipal = anidpprincipal[0];
                        tracer.trace("resolveUserPrincipal: principal retrieved from authenticated session " +
                                nidpprincipal.getUserIdentifier());
                        authenticationClass.setPrincipalPublic(nidpprincipal);
                    }
                }
                if (nidpprincipal == null)
                    tracer.trace("resolveUserPrincipal: no principal in session");
            }
        } else {
            tracer.trace("resolveUserPrincipal: retrieved principal from properties " +
                    nidpprincipal.getUserIdentifier());
            authenticationClass.setPrincipalPublic(nidpprincipal);
        }
        return nidpprincipal;
    }
}
