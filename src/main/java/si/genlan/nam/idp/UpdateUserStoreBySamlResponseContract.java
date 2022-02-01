package si.genlan.nam.idp;

import com.novell.nidp.NIDPPrincipal;
import com.novell.nidp.authentication.local.LocalAuthenticationClass;
import com.novell.nidp.common.authority.UserAuthority;
import com.novell.nidp.logging.NIDPLog;
import lombok.Getter;
import org.opensaml.xml.parse.BasicParserPool;
import si.genlan.nam.attributes.AuthenticatedUserPrincipalAttributes;
import si.genlan.nam.attributes.SamlResponseAttribute;
import si.genlan.nam.constants.AttributesQueryConstants;
import si.genlan.nam.repositories.LdapUserStoreRepository;
import si.genlan.nam.repositories.SamlResponseAttributeRepository;
import si.genlan.nam.services.SamlResponseService;
import si.genlan.nam.services.UserService;
import si.genlan.nam.utils.ListUtils;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Getter
public class UpdateUserStoreBySamlResponseContract extends LocalAuthenticationClass {

    private final Tracer tracer;
    private final String sessionUser;
    private final LdapUserStoreRepository ldapUserStoreRepository;
    private final SamlResponseAttributeRepository attributeRepository;
    private UserService userService;

    private AuthenticatedUserPrincipalAttributes userPrincipalAttributes;

    public UpdateUserStoreBySamlResponseContract(Properties props, ArrayList<UserAuthority> userAuthorityList) {
        super(props, userAuthorityList);

        userPrincipalAttributes = new AuthenticatedUserPrincipalAttributes();
        tracer = Tracer.getInstance(getProperty(AttributesQueryConstants.PROP_NAME_TRACE));
        String version = UpdateUserStoreBySamlResponseContract.class.getPackage().getImplementationVersion();

        NIDPLog.logAppFine(String.format("Tracer is enabled: %s. Version: %s", tracer.getTracing(), version));
        //tracer.trace("Reading Attributes Class Initialized. Matching Attribute/s: " + matchingAttribute);
        NIDPLog.logAppFine(String.format("Tracer is enabled: %s. Version: %s", tracer.getTracing(), version));
        NIDPLog.logAppFine(
                String.format(
                        "Tracing Property: %s. Value: %s",
                        AttributesQueryConstants.PROP_NAME_TRACE,
                        getProperty(AttributesQueryConstants.PROP_NAME_TRACE)
                )
        );

        BasicParserPool parsers = new BasicParserPool();
        parsers.setNamespaceAware(true);
        userService = new UserService(tracer.getTracing());
        ldapUserStoreRepository = LdapUserStoreRepository
                .builder()
                .securityCredentials(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_PASSWORD_PARAMETER).split(";"))
                .securityPrincipal(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_USER_PARAMETER).split(";"))
                .securityProtocol(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_PROTOCOL_PARAMETER).split(";"))
                .providerUrl(getProperty(AttributesQueryConstants.PROP_NAME_LDAP_URL).split(";"))
                .tracer(Tracer.getInstance(Boolean.toString(tracer.getTracing())))
                .build();
        attributeRepository = new SamlResponseAttributeRepository();

        tracer.traceConfig(props);

        sessionUser = getProperty("findSessionUser");

    }

    public UpdateUserStoreBySamlResponseContract(Properties properties,
                                                 LdapUserStoreRepository ldapUserStoreRepository,
                                                 SamlResponseAttributeRepository samlResponseAttributeRepository, UserService userService) {
        super(properties, new ArrayList<>());

        tracer = Tracer.getInstance(getProperty(AttributesQueryConstants.PROP_NAME_TRACE));
        sessionUser = getProperty("findSessionUser");
        this.ldapUserStoreRepository = ldapUserStoreRepository;
        this.attributeRepository = samlResponseAttributeRepository;
    }


    private void printList(List<SamlResponseAttribute> list) {
        tracer.trace("Retrieved attributes");
        list.forEach(attr ->
                tracer.trace("printList: Attribute "
                        + attr.getName()
                        + ": "
                        + attr.getAttributeValues())
        );
    }


    //@SneakyThrows
    protected int doAuthenticate() {
        tracer.trace("doAuthenticate()");
        String samlResponse = null;
        attributeRepository.clear(); //CLEARS ATTRIBUTE REPOSITORY
        SamlResponseService samlResponseService = new SamlResponseService(m_Properties);

        try {
            NIDPPrincipal nidpPrincipal = userPrincipalAttributes.resolveUserPrincipal(tracer, sessionUser, m_Session, m_Properties, this); //GETS USER PRINCIPAL
            UserAuthority userAuthority = nidpPrincipal.getAuthority();

            samlResponseService.getAccessManagerUserAttribute(m_Session).forEach(attributeRepository::add);

            if (userAuthority != null) {
                printList(attributeRepository.getAttributes());
                String userPath = userPrincipalAttributes.getUserPrincipal(getPrincipal(), m_Properties, m_Session, tracer).getUserIdentifier();

                if (userPath != null) {
                    String[] gotAttributes = attributeRepository.getArrayOfAttributeNames(); //GETS ARRAY OF REMOTE ATTRIBUTES FROM IDP RESPONSE
                    compareAndUpdateAttributes(gotAttributes, userAuthority, nidpPrincipal, userPath);

                } else {
                    tracer.trace("ERROR: User Principal Path not found. Maybe you have not set up your Authentication Class as StepUp Method?");
                }
            }
        } catch (Exception e) {
            tracer.trace(e.getMessage());
            e.printStackTrace();
        }

        boolean auth = validAuthentication();
        tracer.trace("doAuthenticate: ValidAuthentication:" + auth);

        return AUTHENTICATED;
    }

    public void compareAndUpdateAttributes(String[] gotAttributes, UserAuthority userAuthority, NIDPPrincipal nidpPrincipal, String userPath) throws NamingException {

        Attributes attributes = userAuthority.getAttributes(nidpPrincipal, gotAttributes); //GETS ALL ATTRIBUTES FROM AUTHENTICATED USER
        for (String gotAttribute : gotAttributes) {
            Attribute attr = attributes.get(gotAttribute);
            tracer.lineBreak(1);
            tracer.trace("Attribute Name: " + gotAttribute);
                if (attr != null) {

                    List<String> multivalued = ListUtils.EnumToStringList(attr.getAll());
                    String[] multivaluedStoreArray = ldapUserStoreRepository.getAttributeValues(multivalued);

                    String[] samlValues = attributeRepository.getValuesAsArrayByAttributeName(gotAttribute);


                    tracer.trace("Multivalued Array: " + Arrays.toString(multivaluedStoreArray));
                    tracer.trace("Saml-Values Array: " + Arrays.toString(samlValues));

                    if (!ListUtils.isListsEqual(samlValues, multivaluedStoreArray)) {
                        userService.updateUser(samlValues, multivaluedStoreArray, gotAttribute, userPath, attr, ldapUserStoreRepository, userPath);

                    } else
                        tracer.trace("Remote values and our directory values are same!");

                } else {
                    tracer.trace("This Attribute is not define in User Store");


            }
        }
    }


    public void setPrincipalPublic(NIDPPrincipal nidpPrincipal) {
        setPrincipal(nidpPrincipal);
    }

    private boolean validAuthentication() {
        NIDPPrincipal nidpPrincipal = userPrincipalAttributes.getUserPrincipal(null, null, null, null);
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

    public Attributes getPrincipalAttributesPublic(String[] strings) {
        return getPrincipalAttributes(strings);
    }

}
