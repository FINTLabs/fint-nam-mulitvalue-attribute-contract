package si.genlan.nam.attributes

import com.novell.nidp.NIDPPrincipal
import com.novell.nidp.NIDPSession
import com.novell.nidp.common.authority.ldap.LDAPPrincipal
import com.novell.nidp.common.util.NIDPPrincipalUtils
import org.w3c.dom.Element
import si.genlan.nam.idp.Tracer
import spock.lang.Specification

class AuthenticatedUserPrincipalAttributesSpec extends Specification {

    def authenticatedUserPrincipalAttributes

    void setup() {
        authenticatedUserPrincipalAttributes = new AuthenticatedUserPrincipalAttributes();

//                .builder()
//        .tracer(Tracer.getInstance("true"))
//        .session(new NIDPSession("test"))
//        .principal(null)
//        .properties(new Properties())
//        .build()
    }

    def "If NIDPPrincipal is not null it should be returned"() {

        when:
        def principal = authenticatedUserPrincipalAttributes.getUserPrincipal(new LDAPPrincipal("test"), null, null, null)

        then:
        principal.ge
    }
}
