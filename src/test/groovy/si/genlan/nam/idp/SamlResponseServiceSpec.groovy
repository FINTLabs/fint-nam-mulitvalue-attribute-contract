package si.genlan.nam.idp

import si.genlan.nam.repositories.LdapUserStoreRepository
import si.genlan.nam.services.SamlResponseService
import spock.lang.Specification

class SamlResponseServiceSpec extends Specification {

    private SamlResponseService samlResponseService;
    private LdapUserStoreRepository ldapUserStoreRepository;

    void setup() {
        print("Setup")
        samlResponseService = new SamlResponseService(new Properties());
        ldapUserStoreRepository = LdapUserStoreRepository
                .builder()
                .securityCredentials("SNovak1928!")
                .securityPrincipal("cn=admin,o=novell")
                .providerUrl("ldaps://10.10.3.63:636")
                .matchingAttributeName("mail")
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
}
