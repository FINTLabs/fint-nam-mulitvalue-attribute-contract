package si.genlan.nam.idp;

import com.novell.nidp.*;
import com.novell.nidp.authentication.local.LocalAuthenticationClass;
import com.novell.nidp.common.authority.UserAuthority;
import com.novell.nidp.logging.NIDPLog;
import org.apache.catalina.connector.RequestFacade;
import org.opensaml.xml.parse.BasicParserPool;

import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class UpdateUserStoreBySamlResponseContract extends LocalAuthenticationClass {

    private final Tracer tracer;
    private final String sessionUser;
    public static String matchingAttribute;

    private final LdapUserStoreRepository ldapUserStoreRepository;
    private final AttributeRepository attributeRepository;

    public static SamlRequestVariableList samlRequestVariableList = new SamlRequestVariableList();

    public UpdateUserStoreBySamlResponseContract(Properties props, ArrayList<UserAuthority> arrayList) {
        super(props, arrayList);

        tracer = new Tracer(getProperty(AttributesQueryConstants.PROP_NAME_TRACE));
        String version = UpdateUserStoreBySamlResponseContract.class.getPackage().getImplementationVersion();
        matchingAttribute = getProperty(AttributesQueryConstants.PROP_NAME_MATCHING_NAME);

        NIDPLog.logAppFine(String.format("Tracer is enabled: %s. Version: %s" + tracer.getTracing(), version));
        tracer.trace("Reading Attributes Class Initialized. Matching Attribute/s: " + matchingAttribute);

        BasicParserPool parsers = new BasicParserPool();
        parsers.setNamespaceAware(true);

        ldapUserStoreRepository = LdapUserStoreRepository
                .builder()
                .securityCredentials(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_PASSWORD_PARAMETER))
                .securityPrincipal(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_USER_PARAMETER))
                .providerUrl(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_URL))
                .tracer(tracer)
                .build();

        attributeRepository = new AttributeRepository();

        tracer.traceConfig(props);

        sessionUser = getProperty("findSessionUser");

    }


    private void printList(List<MyAttribute> list) {
        list.forEach(attr ->
                tracer.trace("printList: Attribute "
                        + attr.getName()
                        + ": "
                        + attr.getAttributeValues())
        );
    }







    protected int doAuthenticate() {
        tracer.trace("doAuthenticate()");
        String samlResponse = null;
        SamlResponseService samlResponseService = new SamlResponseService(tracer, m_Properties);

        try {
            NIDPPrincipal nidpPrincipal = resolveUserPrincipal();
            UserAuthority userAuthority = nidpPrincipal.getAuthority();

            if (userAuthority != null) {

                Attributes attributes = userAuthority.getAttributes(
                        nidpPrincipal,
                        new String[]{getProperty(AttributesQueryConstants.PROP_NAME_MATCHING_NAME)}
                );

                Attribute matchingAttribute = attributes.get(getProperty(AttributesQueryConstants.PROP_NAME_MATCHING_NAME));
                samlResponse = samlRequestVariableList.getLast((String) matchingAttribute.get());                                                                                            // matching

                samlRequestVariableList.removeRequestsOlderThan5Minutes();
                tracer.trace("doAuthenticate: MatchingAttr: " + UpdateUserStoreBySamlResponseContract.matchingAttribute + " -> " + matchingAttribute.get());

                tracer.trace("doAuthenticate samlResponse: " + samlResponse);

                attributeRepository.clear();
                samlResponseService
                        .getSamlResponseAttributes(samlResponse)
                        .forEach(attributeRepository::add);


                printList(attributeRepository.getAttributes());

                String userPath = getUserPrincipal().getUserIdentifier();
                String[] gotAttributes = attributeRepository.getSentAttributes();
                attributes = userAuthority.getAttributes(nidpPrincipal, gotAttributes);

                for (String gotAttribute : gotAttributes) {
                    Attribute attr = attributes.get(gotAttribute);

                    if (attr != null) {
                        NamingEnumeration<?> multivalue = attr.getAll();
                        StringBuilder multivalueVal = new StringBuilder();

                        String[] samlValues = attributeRepository.getSentAttributesArray(gotAttribute);

                        String[] multivalueStoreArray = new String[0];

                        tracer.trace("MultiVal: " + multivalue);
                        while (multivalue.hasMore()) {
                            String val = multivalue.next().toString();
                            multivalueVal.append(val).append("; ");

                            multivalueStoreArray = Arrays.copyOf(multivalueStoreArray, multivalueStoreArray.length + 1);
                            multivalueStoreArray[multivalueStoreArray.length - 1] = val;
                        }
                        ModificationItem[] mods = new ModificationItem[0];
                        if (!ListUtils.isListsEqual(samlValues, multivalueStoreArray)) {
                            tracer.trace(
                                    "doAuthenticate: Attribute " + gotAttribute + " SamlValue: " + attributeRepository.getListValue(gotAttribute) + " StoreValue: " + multivalueVal);
                            tracer.trace("doAuthenticate: Going through multivalue array: " + Arrays.toString(multivalueStoreArray));
                            for (String s : multivalueStoreArray) {
                                tracer.trace("doAuthenticate: Attr: " + gotAttribute + "String: " + s);
                                if (!(Arrays.asList(samlValues).contains(s))) {
                                    tracer.trace("doAuthenticate: Removing Attribute: " + gotAttribute + " Value: " + s);
                                    mods = Arrays.copyOf(mods, mods.length + 1);
                                    mods[mods.length - 1] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                                            new BasicAttribute(gotAttribute, s));

                                }
                            }
                            ldapUserStoreRepository.updateUser(userPath, mods);

                            tracer.trace("doAuthenticate: New multivalue array");
                            multivalueStoreArray = new String[0];
                            multivalue = attr.getAll();

                            while (multivalue.hasMore()) {
                                String val = multivalue.next().toString();
                                multivalueVal.append(val).append("; ");

                                multivalueStoreArray = Arrays.copyOf(multivalueStoreArray, multivalueStoreArray.length + 1);
                                multivalueStoreArray[multivalueStoreArray.length - 1] = val;
                            }
                            mods = new ModificationItem[0];
                            tracer.trace("doAuthenticate: Going through saml values");
                            for (String s : samlValues) {
                                if (!(Arrays.asList(multivalueStoreArray).contains(s))) {
                                    tracer.trace("doAuthenticate: Adding Attribute: " + gotAttribute + " Value: " + s);
                                    mods = Arrays.copyOf(mods, mods.length + 1);
                                    mods[mods.length - 1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                                            new BasicAttribute(gotAttribute, s));

                                }
                            }
                            ldapUserStoreRepository.updateUser(userPath, mods);
                        }

                    } else {
                        ModificationItem[] mods;
                        String[] samlValues = attributeRepository.getSentAttributesArray(gotAttribute);
                        mods = new ModificationItem[0];
                        tracer.trace("doAuthenticate: Going through saml values");
                        for (String s : samlValues) {
                            mods = Arrays.copyOf(mods, mods.length + 1);
                            mods[mods.length - 1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                                    new BasicAttribute(gotAttribute, s));
                        }
                        ldapUserStoreRepository.updateUser(userPath, mods);
                    }

                }
            }
            // tracer.trace("SamlResponse doc: " + samlResp);
        } catch (Exception e) {
            tracer.trace(e.getMessage());
            e.printStackTrace();
        }

        tracer.trace("doAuthenticate: GetUserPrincipal: ");
        getUserPrincipalAttributes();
        boolean auth = validAuthentication();
        tracer.trace("doAuthenticate: ValidAuthentication:" + auth);

        samlRequestVariableList.removeSamlRequest(samlResponse);

        return AUTHENTICATED;
    }

    private boolean validAuthentication() {
        NIDPPrincipal nidpPrincipal = getUserPrincipal();
        if (nidpPrincipal != null) {
            String username = nidpPrincipal.getUserIdentifier();
            tracer.trace("validAuthentication: User identifier: " + username);
            setPrincipal(nidpPrincipal);
            return true;
        }
        setUserErrorMsg("validAuthentication: No Authenticated User Found");
        tracer.trace(getUserErrorMsg());
        return false;

    }

    private NIDPPrincipal getUserPrincipal() {
        NIDPPrincipal idpPrincipal = getPrincipal();
        if (idpPrincipal == null) {
            idpPrincipal = getContractUser();

            if (idpPrincipal == null) {
                idpPrincipal = getSessionUser();
            }
        }
        return idpPrincipal;
    }

    private void getUserPrincipalAttributes() {
        Attributes attr;
        String[] attrNames = {"sn", "mail"};

        String id = m_Request.getParameter(NIDPConstants.PARM_USERID);
        String saml2 = m_Request.getParameter(NIDPConstants.SAML2);
        tracer.trace("getUPA: id: " + id);
        tracer.trace("getUPA: saml2: " + saml2);
        attr = getPrincipalAttributes(attrNames);
        if (attr != null)
            tracer.trace("Principal attebiutes length: " + attr.size());
        else
            tracer.trace("No principal attributes found");
    }

    private NIDPPrincipal getContractUser() {
        NIDPPrincipal contractUser = (NIDPPrincipal) m_Properties.get("Principal");
        if (contractUser != null) {
            tracer.trace("Found contract authenticated user: ", contractUser.getUserIdentifier());
        }
        return contractUser;
    }

    private NIDPPrincipal getSessionUser() {
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

    private NIDPPrincipal resolveUserPrincipal() {
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
                        setPrincipal(nidpprincipal);
                    }
                }
                if (nidpprincipal == null)
                    tracer.trace("resolveUserPrincipal: no principal in session");
            }
        } else {
            tracer.trace("resolveUserPrincipal: retrieved principal from properties " +
                    nidpprincipal.getUserIdentifier());
            setPrincipal(nidpprincipal);
        }
        return nidpprincipal;
    }

    public void initializeRequest(HttpServletRequest request, HttpServletResponse response, NIDPSession session,
                                  NIDPSessionData data, boolean following, String url) {

        super.initializeRequest(request, response, session, data, following, url);

        RequestFacade requestFacade = (RequestFacade) request;
        Enumeration<String> attributes = requestFacade.getAttributeNames();
        while (attributes.hasMoreElements()) {
            String attributeName = attributes.nextElement();
            Object attributeValue = request.getAttribute(attributeName);
            tracer.trace("initRequ: attr: " + attributeName + " val: " + attributeValue);
        }
        String paramSn = request.getParameter("sn");
        String attrReq = (String) request.getAttribute("sn");
        tracer.trace("initializeRequest: Initialize Request: " + request);
        tracer.trace("initializeRequest: sn: " + attrReq);
        tracer.trace("initializeRequest: paramSn: " + paramSn);
        String respMsg = requestFacade.getParameter("SAMLResponse");
        if (respMsg != null) {
            tracer.trace("initializeRequest: saml: " + respMsg);
            // Base64 base64Decoder = new Base64();

        } else
            tracer.trace("initializeRequest: no saml message");
    }


}
