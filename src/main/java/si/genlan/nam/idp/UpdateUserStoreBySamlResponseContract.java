package si.genlan.nam.idp;

import com.novell.nidp.NIDPPrincipal;
import com.novell.nidp.authentication.local.LocalAuthenticationClass;
import com.novell.nidp.common.authority.UserAuthority;
import com.novell.nidp.logging.NIDPLog;
import lombok.SneakyThrows;
import org.opensaml.xml.parse.BasicParserPool;

import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.*;

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
        tracer = Tracer.getInstance(getProperty(AttributesQueryConstants.PROP_NAME_TRACE), "UpdateUserStore GenLan");
        String version = UpdateUserStoreBySamlResponseContract.class.getPackage().getImplementationVersion();
        matchingAttribute = getProperty(AttributesQueryConstants.PROP_NAME_MATCHING_NAME);

        NIDPLog.logAppFine(String.format("Tracer is enabled: %s. Version: %s", tracer.getTracing(), version));
        NIDPLog.logAppFine(String.format("Tracing Property: %s. Value: %s", AttributesQueryConstants.PROP_NAME_TRACE, getProperty(AttributesQueryConstants.PROP_NAME_TRACE)));
        tracer.trace("Reading Attributes Class Initialized. Matching Attribute/s: " + matchingAttribute);

        BasicParserPool parsers = new BasicParserPool();
        parsers.setNamespaceAware(true);

        ldapUserStoreRepository = LdapUserStoreRepository //CREATES CONNECTION TO LDAP USER STORE
                .builder()
                .securityCredentials(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_PASSWORD_PARAMETER))
                .securityPrincipal(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_USER_PARAMETER))
                .providerUrl(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_URL))
                .tracer(Tracer.getInstance(Boolean.toString(tracer.getTracing()), "LDAP UserStore Repository"))
                .build();

        attributeRepository = new SamlResponseAttributeRepository(); //CREATES NEW LDAP REPOSITORY

        tracer.traceConfig(props); //SHOWS PROPERTIES IN LOG

        sessionUser = getProperty("findSessionUser"); //GETS SESSION USER

    }


    private void printList(List<SamlResponseAttribute> list) {
        tracer.trace("Printing Retrieved Attributes");
        list.forEach(attr ->
                tracer.trace("printList: Attribute "
                        + attr.getName()
                        + ": "
                        + attr.getAttributeValues())
        );
    }


    @SneakyThrows
    protected int doAuthenticate() {
        tracer.trace("doAuthenticate()");
        String samlResponse = null;
        SamlResponseService samlResponseService = new SamlResponseService(m_Properties);

        try {
            NIDPPrincipal nidpPrincipal = userPrincipalAttributes.resolveUserPrincipal(tracer,sessionUser,m_Session,m_Properties,this); //GETS USER PRINCIPAL
            UserAuthority userAuthority = nidpPrincipal.getAuthority(); //GETS USER AUTHORITY FROM USER PRINCIPAL

            if (userAuthority != null) {
                Attributes attributes = userAuthority.getAttributes(
                        nidpPrincipal,
                        new String[]{getProperty(AttributesQueryConstants.PROP_NAME_MATCHING_NAME)}
                );

                //Attribute matchingAttribute = attributes.get(getProperty(AttributesQueryConstants.PROP_NAME_MATCHING_NAME));
                //samlResponse = samlRequestVariableList.getLast((String) matchingAttribute.get());
                samlRequestVariableList.removeRequestsOlderThan5Minutes();

                //tracer.trace("doAuthenticate: MatchingAttr: " + UpdateUserStoreBySamlResponseContract.matchingAttribute + " -> " + matchingAttribute.get());
                //tracer.trace("doAuthenticate samlResponse: " + samlResponse);

                attributeRepository.clear(); //CLEARS ATTRIBUTE REPOSITORY

                samlResponseService
                        .getAccessManagerUserAttribute(m_Session)
                        .forEach(attributeRepository::add); //ADDS ALL ATTRIBUTES INTO CLEAN ATTRIBUTE REPOSITORY

                printList(attributeRepository.getAttributes()); //PRINTS ALL ATTRIBUTES FROM ATTRIBUTE REPO

                String userPath = userPrincipalAttributes.getUserPrincipal(getPrincipal(), m_Properties, m_Session, tracer).getUserIdentifier();
                String[] gotAttributes = attributeRepository.getArrayOfAttributeNames(); //GETS ARRAY OF REMOTE ATTRIBUTES FROM IDP RESPONSE
                attributes = userAuthority.getAttributes(nidpPrincipal, gotAttributes); //GETS ALL ATTRIBUTES FROM AUTHENTICATED USER
                for (String gotAttribute : gotAttributes) {
                    Attribute attr = attributes.get(gotAttribute);
                    if (attr != null) {
                        NamingEnumeration<?> multivalued = attr.getAll();
                        StringBuilder multivaluedVal = ldapUserStoreRepository.getAttributeValuesString(multivalued);
                        String[] samlValues = attributeRepository.getValuesAsArrayByAttributeName(gotAttribute);
                        String[] multivaluedStoreArray = ldapUserStoreRepository.getAttributeValues(multivalued);
                        tracer.trace("MultiVal: " + multivalued);
                        tracer.trace("Multivalued Array: " + Arrays.toString(multivaluedStoreArray));
                        ModificationItem[] mods;
                        //REMOVE ATTRIBUTE VALUE FROM USER STORE IF NOT IN SAML RESPONSE
                        //region REMOVE_ATTRIBUTE
                        if (!ListUtils.isListsEqual(samlValues, multivaluedStoreArray)) {
                            tracer.trace(
                                    "doAuthenticate: Attribute " + gotAttribute + " SamlValue: " + attributeRepository.getJoinedValueListByName(gotAttribute) + " StoreValue: " + multivaluedVal);
                            tracer.trace("doAuthenticate: Going through multivalued array: " + Arrays.toString(multivaluedStoreArray));
                            mods = ldapUserStoreRepository.AttributeValuesToDeleteFromUserStore(samlValues, multivaluedStoreArray, gotAttribute);
                            if(mods.length >= 0)
                                ldapUserStoreRepository.updateUser(userPath, mods);
                            else
                                tracer.trace("Nothing to delete from User Store");
                            /* endregion */

                            //ADD ATTRIBUTE VALUE TO USER STORE IF IN SAML RESPONSE AND NOT IN USER STORE
                            multivalued = attr.getAll();
                            multivaluedStoreArray = ldapUserStoreRepository.getAttributeValues(multivalued);
                            mods = ldapUserStoreRepository.AttributeValuesToAddFromUserStore(samlValues, multivaluedStoreArray, gotAttribute);
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

        //testFunctions();
        userPrincipalAttributes.getUserPrincipalAttributes(m_Request, tracer, this);
        samlRequestVariableList.removeSamlRequest(samlResponse);
        return AUTHENTICATED;
    }
    /*public void testFunctions() throws Exception {
        if(m_Session != null) {
            tracer.trace("m_Session ID: " +  m_Session.getID());
            NIDPSubject subject = m_Session.getSubject();
            if(subject != null)
            {

                NIDPServletSession sessionServlet = (NIDPServletSession)m_Session;

                WSCCachePushed cachePushed = WSCCachePushed.getInstance();
                WSCCachePushedCache cache = cachePushed.getCache(m_Session.getID());

                if ( cache == null )
                {
                    cache = new WSCCachePushedCache();
                }

                tracer.trace("WSCCache Size: " + cache.size());
                tracer.trace("WSCCache: " + cache.toString());
                tracer.trace("WSCCache Iterator: " + cache.iterator());

                attributeRepository.clear();
                Iterator<WSCCachePushedCacheSet> iterator = cache.iterator();
                while(iterator.hasNext())
                {
                    WSCCachePushedCacheSet xy = iterator.next();
                    tracer.trace(xy.toString());
                    for (WSCCacheEntry xyEntry : xy.getEntries()) {
                        tracer.trace("Select String: "+xyEntry.getSelectString());
                        String str1  = xyEntry.getSelectString();
                        str1 = str1.replace("/UserAttribute[@ldap:targetAttribute=\"", "");
                        str1 = str1.replace("\"]", "");

                        tracer.trace("LDAP Attribute string: " + str1);
                        //tracer.trace("UniqueID: "+xyEntry.getTokenUniqueId());
                        if(xyEntry.getDataItemValue() instanceof  LDAPUserAttribute)
                            tracer.trace("Instance went through");
                        LDAPUserAttribute ldapAttr = (LDAPUserAttribute) xyEntry.getDataItemValue();
                        Iterator<LDAPAttributeValue> values = ldapAttr.getValues();
                        if(values != null)
                        {
                            while(values.hasNext())
                            {
                                LDAPAttributeValue val = values.next();
                                attributeRepository.add(str1, val.getValue());
                                tracer.trace("LDAP Attribute: "+str1+" LDAP Value : "+val.getValue());
                            }
                        }
                    }

                }


            }
            else
                tracer.trace("m_Session subject is null!");

        }
        else
            tracer.trace("m_Session null");

    }*/
    public void setPrincipalPublic(NIDPPrincipal nidpPrincipal) { setPrincipal(nidpPrincipal); }
    public Attributes getPrincipalAttributesPublic(String[] strings){ return getPrincipalAttributes(strings);}

}
