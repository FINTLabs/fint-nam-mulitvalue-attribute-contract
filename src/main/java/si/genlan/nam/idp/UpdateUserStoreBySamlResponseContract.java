package si.genlan.nam.idp;

import com.novell.nidp.NIDPPrincipal;
import com.novell.nidp.authentication.local.LocalAuthenticationClass;
import com.novell.nidp.common.authority.UserAuthority;
import com.novell.nidp.logging.NIDPLog;
import lombok.SneakyThrows;
import org.opensaml.xml.parse.BasicParserPool;
import si.genlan.nam.attributes.AuthenticatedUserPrincipalAttributes;
import si.genlan.nam.attributes.SamlResponseAttribute;
import si.genlan.nam.constants.AttributesQueryConstants;
import si.genlan.nam.repositories.LdapUserStoreRepository;
import si.genlan.nam.repositories.SamlResponseAttributeRepository;
import si.genlan.nam.utils.ListUtils;
import si.genlan.nam.services.SamlResponseService;

import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class UpdateUserStoreBySamlResponseContract extends LocalAuthenticationClass {

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

        NIDPLog.logAppFine(String.format("Tracer is enabled: %s. Version: %s", tracer.getTracing(), version));
        NIDPLog.logAppFine(String.format("Tracing Property: %s. Value: %s", AttributesQueryConstants.PROP_NAME_TRACE, getProperty(AttributesQueryConstants.PROP_NAME_TRACE)));

        BasicParserPool parsers = new BasicParserPool();
        parsers.setNamespaceAware(true);

        ldapUserStoreRepository = LdapUserStoreRepository //CREATES CONNECTION TO LDAP USER STORE
                .builder()
                .securityCredentials(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_PASSWORD_PARAMETER))
                .securityPrincipal(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_USER_PARAMETER))
                .providerUrl(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_URL))
                .tracer(Tracer.getInstance(Boolean.toString(tracer.getTracing()), "LDAP UserStore Repository"))
                .build();
        ldapUserStoreRepository.Connect();

        attributeRepository = new SamlResponseAttributeRepository(); //CREATES NEW LDAP REPOSITORY

        tracer.traceConfig(props); //SHOWS PROPERTIES IN LOG

        sessionUser = getProperty("findSessionUser"); //GETS SESSION USER

    }


    private void printList(List<SamlResponseAttribute> list) {
        tracer.trace("Printing Retrieved attributes");
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

                attributeRepository.clear(); //CLEARS ATTRIBUTE REPOSITORY

                samlResponseService
                        .getAccessManagerUserAttribute(m_Session)
                        .forEach(attributeRepository::add); //ADDS ALL ATTRIBUTES INTO CLEAN ATTRIBUTE REPOSITORY

                printList(attributeRepository.getAttributes()); //PRINTS ALL ATTRIBUTES FROM ATTRIBUTE REPO

                String userPath = userPrincipalAttributes.getUserPrincipal(getPrincipal(), m_Properties, m_Session, tracer).getUserIdentifier();

                if (userPath != null) {
                    String[] gotAttributes = attributeRepository.getArrayOfAttributeNames(); //GETS ARRAY OF REMOTE ATTRIBUTES FROM IDP RESPONSE
                    attributes = userAuthority.getAttributes(nidpPrincipal, gotAttributes); //GETS ALL ATTRIBUTES FROM AUTHENTICATED USER
                    tracer.traceBreak(2);
                    tracer.trace("Going through attributes: ");
                    for (String gotAttribute : gotAttributes) {
                        Attribute attr = attributes.get(gotAttribute);
                        tracer.traceBreak(1);
                        tracer.trace("Attribute Name: " + gotAttribute);
                        if (attr != null) {

                            List<String> multivalued = ListUtils.EnumToStringList(attr.getAll());
                            String[] samlValues = attributeRepository.getValuesAsArrayByAttributeName(gotAttribute);
                            String[] multivaluedStoreArray = ldapUserStoreRepository.getAttributeValues(multivalued);

                            tracer.trace("Multivalued Array: " + Arrays.toString(multivaluedStoreArray));
                            tracer.trace("Saml-Values Array: " + Arrays.toString(samlValues));

                            ModificationItem[] mods;

                            if (!ListUtils.isListsEqual(samlValues, multivaluedStoreArray)) {

                                mods = ldapUserStoreRepository.AttributeValuesToAddToUserStore(samlValues, multivaluedStoreArray, gotAttribute);
                                ldapUserStoreRepository.updateUser(userPath, mods);

                                multivalued = ListUtils.EnumToStringList(attr.getAll());
                                multivaluedStoreArray = ldapUserStoreRepository.getAttributeValues(multivalued);
                                mods = ldapUserStoreRepository.AttributeValuesToDeleteFromUserStore(samlValues, multivaluedStoreArray, gotAttribute);
                                if (mods.length >= 0)
                                    ldapUserStoreRepository.updateUser(userPath, mods);
                                else
                                    tracer.trace("Nothing to delete from User Store");
                            }
                            else
                                tracer.trace("Remote values and our directory values are same!");

                        }
                        else
                        {
                            tracer.trace("This Attribute is not define in User Store");
                        }

                    }
                }
            }
            else
                tracer.trace("ERROR: User Principal Path not found. Maybe you have not set up your Authentication Class as StepUp Method?");
        } catch (Exception e) {
            tracer.trace(e.getMessage());
            e.printStackTrace();
        }
        return AUTHENTICATED;
    }

    public void setPrincipalPublic(NIDPPrincipal nidpPrincipal) { setPrincipal(nidpPrincipal); }
    public Attributes getPrincipalAttributesPublic(String[] strings){ return getPrincipalAttributes(strings);}

}
