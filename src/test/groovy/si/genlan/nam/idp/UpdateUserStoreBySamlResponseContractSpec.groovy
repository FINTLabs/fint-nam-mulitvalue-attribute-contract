package si.genlan.nam.idp

import si.genlan.nam.repositories.LdapUserStoreRepository
import si.genlan.nam.repositories.SamlResponseAttributeRepository
import spock.lang.Specification
import javax.servlet.http.HttpServletRequest


class UpdateUserStoreBySamlResponseContractSpec extends Specification {

    private UpdateUserStoreBySamlResponseContract updateUserStoreBySamlResponseContract;
    void setup() {
        updateUserStoreBySamlResponseContract = new UpdateUserStoreBySamlResponseContract(
                new Properties(),
                Mock(LdapUserStoreRepository),
                new SamlResponseAttributeRepository()
        )
    }

    def "doAuthenticate should return AUTHENTICATED"() {

        when:
        def authenticate = updateUserStoreBySamlResponseContract.doAuthenticate()

        then:
        authenticate == UpdateUserStoreBySamlResponseContract.AUTHENTICATED


    }
}
