package si.genlan.nam.idp

import com.sun.jndi.ldap.LdapCtxFactory
import si.genlan.nam.repositories.LdapUserStoreRepository
import si.genlan.nam.services.SamlResponseService
import spock.lang.Specification

import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class SamlResponseServiceSpec extends Specification {

    private SamlResponseService samlResponseService;
    private LdapUserStoreRepository ldapUserStoreRepository;
    private LdapCtxFactory ldap;

    void setup() {
        print("Setup")
        ldap = Mock(LdapCtxFactory)

        samlResponseService = new SamlResponseService(new Properties());
        ldapUserStoreRepository = LdapUserStoreRepository
                .builder()
                .securityCredentials("")
                .securityPrincipal("")
                .providerUrl("")
                .matchingAttributeName("")
                .tracer(Tracer.getInstance("true","Test"))
                .build()
        ldapUserStoreRepository.Connect();

    }

    def "Connect To UserStore"()
    {
        given:
        def MatchingAttribute = "sebastian.novak@genlan.si"
        when:
        ldapUserStoreRepository.MatchUser(MatchingAttribute)

        then:
        print("done")

    }

    def "Create user into ldap"()
    {
        given:
        def username = "user1";
        def attrs = new BasicAttributes();

        when:
        def objectClass = new BasicAttribute("objectClass");
        objectClass.add("personinfo");
        attrs.put(objectClass);
        attrs.put("cn", "user1")
        attrs.put("sn", "test")
        attrs.put("mail", "example@example.xyz")
        ldapUserStoreRepository.ldapConnection.createSubcontext("cn=user1test", attrs)


    }
}
