package si.genlan.nam.idp

import com.novell.nidp.common.authority.UserAuthority
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
