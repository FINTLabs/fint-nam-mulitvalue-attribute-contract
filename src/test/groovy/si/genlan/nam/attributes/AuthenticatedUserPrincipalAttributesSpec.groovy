package si.genlan.nam.attributes

import com.novell.nidp.NIDPPrincipal
import com.novell.nidp.NIDPSession
import com.novell.nidp.NIDPSubject
import com.novell.nidp.common.authority.ldap.LDAPPrincipal
import com.novell.nidp.common.util.NIDPPrincipalUtils
import org.w3c.dom.Element
import si.genlan.nam.helpers.NIDPPricipalTest
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
        def principal = authenticatedUserPrincipalAttributes.getUserPrincipal(new NIDPPricipalTest("guid", "userIdentifier"), null, null, null)

        then:
        principal.m_Guid == "guid"
    }

    def "If NIDPPrincipal is null contract user should be returned"() {

        given:
        def properties = new Properties()
        def ldapPrincipal = new NIDPPricipalTest("guid", "contractUserIdentifier")
        properties.put("Principal", ldapPrincipal)

        when:
        def principal = authenticatedUserPrincipalAttributes.getUserPrincipal(null, properties, null, Tracer.getInstance("true"))

        then:
        principal
        principal.getUserIdentifier() == "contractUserIdentifier"

    }

    def "If NIDPPrincipal and contract user is null session user should be returned"() {
        given:
        def session = Mock(NIDPSession)
        def subject = new NIDPSubject()
        subject.addPrincipal(new NIDPPricipalTest("guid", "sessionUserIdentifier"))

        when:
        def principal = authenticatedUserPrincipalAttributes.getUserPrincipal(null, new Properties(), session, Tracer.getInstance("true"))

        then:
        session.isAuthenticated() >> true
        session.getSubject() >> subject

        principal
        principal.getUserIdentifier() == "sessionUserIdentifier"
    }

    def "Resolve user principal should return a NIDPPrincipal"() {

        when:
        def principal = authenticatedUserPrincipalAttributes.resolveUserPrincipal(
                Tracer.getInstance("true"),
                "sessionUser",
                null,
                null,
                null
        )

        then:
        principal

    }
}
