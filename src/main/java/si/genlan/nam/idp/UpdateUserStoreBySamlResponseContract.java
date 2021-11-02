package si.genlan.nam.idp;

import org.opensaml.xml.parse.BasicParserPool;

import com.novell.nidp.*;
import com.novell.nidp.authentication.local.LocalAuthenticationClass;
import com.novell.nidp.common.authority.UserAuthority;
import com.novell.nidp.logging.NIDPLog;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class UpdateUserStoreBySamlResponseContract extends LocalAuthenticationClass {

    public static String matchingAttribute;
    public static SamlRequestVariableList samlRequestVariableList = new SamlRequestVariableList();
    private final Tracer tracer;
    private final String sessionUser;
    private final LdapUserStoreRepository ldapUserStoreRepository;
    private final SamlResponseAttributeRepository attributeRepository;
    private final AuthenticatedUserPrincipalAttributes userPrincipalAttributes;

    public UpdateUserStoreBySamlResponseContract(Properties props, ArrayList<UserAuthority> arrayList) {
        super(props, arrayList);
        userPrincipalAttributes = new AuthenticatedUserPrincipalAttributes();
        tracer = Tracer.getInstance(getProperty(AttributesQueryConstants.PROP_NAME_TRACE));
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

        attributeRepository = new SamlResponseAttributeRepository();

        tracer.traceConfig(props);

        sessionUser = getProperty("findSessionUser");

    }


    private void printList(List<SamlResponseAttribute> list) {
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
        SamlResponseService samlResponseService = new SamlResponseService(m_Properties);

        try {
            NIDPPrincipal nidpPrincipal = userPrincipalAttributes.resolveUserPrincipal(tracer,sessionUser,m_Session,m_Properties,this);
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

                String userPath = userPrincipalAttributes.getUserPrincipal(getPrincipal(), m_Properties, m_Session, tracer).getUserIdentifier();
                String[] gotAttributes = attributeRepository.getArrayOfAttributeNames();
                attributes = userAuthority.getAttributes(nidpPrincipal, gotAttributes);

                for (String gotAttribute : gotAttributes) {
                    Attribute attr = attributes.get(gotAttribute);

                    if (attr != null) {
                        NamingEnumeration<?> multivalue = attr.getAll();
                        StringBuilder multivalueVal = new StringBuilder();

                        String[] samlValues = attributeRepository.getValuesAsArrayByAttributeName(gotAttribute);

                        String[] multivalueStoreArray = new String[0];

                        tracer.trace("MultiVal: " + multivalue);
                        while (multivalue.hasMore()) {
                            String val = multivalue.next().toString();
                            multivalueVal.append(val).append("; ");

                            multivalueStoreArray = Arrays.copyOf(multivalueStoreArray, multivalueStoreArray.length + 1);
                            multivalueStoreArray[multivalueStoreArray.length - 1] = val;
                        }
                        ModificationItem[] mods = new ModificationItem[0];
                        //REMOVE ATTRIBUTE VALUE FROM USER STORE IF NOT IN SAML RESPONSE
                        //region REMOVE_ATTRIBUTE
                        if (!ListUtils.isListsEqual(samlValues, multivalueStoreArray)) {
                            tracer.trace(
                                    "doAuthenticate: Attribute " + gotAttribute + " SamlValue: " + attributeRepository.getJoinedValueListByName(gotAttribute) + " StoreValue: " + multivalueVal);
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
                            /* endregion */

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
                            //ADD ATTRIBUTE VALUE TO USER STORE IF IN SAML RESPONSE AND NOT IN USER STORE
                            for (String s : samlValues) {
                                if (!(Arrays.asList(multivalueStoreArray).contains(s))) {
                                    mods = Arrays.copyOf(mods, mods.length + 1);
                                    mods[mods.length - 1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                                            new BasicAttribute(gotAttribute, s));

                                }
                            }
                            ldapUserStoreRepository.updateUser(userPath, mods);
                        }

                    } else {
                        //ADDS ATTRIBUTE IF ATTRIBUTE IS NOT IN USER STORE
                        ModificationItem[] mods;
                        String[] samlValues = attributeRepository.getValuesAsArrayByAttributeName(gotAttribute);
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
        userPrincipalAttributes.getUserPrincipalAttributes(m_Request, tracer, this);
        boolean auth = validAuthentication();
        tracer.trace("doAuthenticate: ValidAuthentication:" + auth);

        samlRequestVariableList.removeSamlRequest(samlResponse);

        return AUTHENTICATED;
    }

    private boolean validAuthentication() {
        NIDPPrincipal nidpPrincipal = userPrincipalAttributes.getUserPrincipal(getPrincipal(), m_Properties, m_Session, tracer);
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
    public void setPrincipalPublic(NIDPPrincipal nidpPrincipal) { setPrincipal(nidpPrincipal); }
    public Attributes getPrincipalAttributesPublic(String[] strings){ return getPrincipalAttributes(strings);}




}
