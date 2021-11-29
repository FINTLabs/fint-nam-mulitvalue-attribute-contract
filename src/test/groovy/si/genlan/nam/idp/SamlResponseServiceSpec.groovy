package si.genlan.nam.idp

import si.genlan.nam.repositories.LdapUserStoreRepository
import si.genlan.nam.services.SamlResponseService
import spock.lang.Specification

import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

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

    def "Saml response should be decoded"() {
        given:
        def samlResponse = "PHNhbWxwOlJlc3BvbnNlIElEPSJfNGZhN2ZmZTMtNTM1Mi00MGRiLThjNmEtMzlkNWQ4OGVjOWEyIgogICAgICAgICAgICAgICAgVmVyc2lvbj0iMi4wIgogICAgICAgICAgICAgICAgSXNzdWVJbnN0YW50PSIyMDIxLTEwLTMwVDEwOjA4OjIyLjY4OFoiCiAgICAgICAgICAgICAgICBEZXN0aW5hdGlvbj0iaHR0cHM6Ly9pZHAuZmVsbGVza29tcG9uZW50Lm5vL25pZHAvc2FtbDIvc3Bhc3NlcnRpb25fY29uc3VtZXIiCiAgICAgICAgICAgICAgICBJblJlc3BvbnNlVG89ImR1bW15IgogICAgICAgICAgICAgICAgeG1sbnM6c2FtbHA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIKPgogICAgPElzc3VlciB4bWxucz0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+CiAgICAgICAgaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvMDhmMzgxM2MtOWYyOS00ODJmLTlhZWMtMTZlZjdjYmY0NzdhLwogICAgPC9Jc3N1ZXI+CiAgICA8c2FtbHA6U3RhdHVzPgogICAgICAgIDxzYW1scDpTdGF0dXNDb2RlIFZhbHVlPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6c3RhdHVzOlN1Y2Nlc3MiLz4KICAgIDwvc2FtbHA6U3RhdHVzPgogICAgPEFzc2VydGlvbiBJRD0iX2ZhZTRkMDRkLTUzZGEtNDY1Yy04NTMxLWMzYmJkYjk4NDYwMCIKICAgICAgICAgICAgICAgSXNzdWVJbnN0YW50PSIyMDIxLTEwLTMwVDEwOjA4OjIyLjY4OFoiCiAgICAgICAgICAgICAgIFZlcnNpb249IjIuMCIKICAgICAgICAgICAgICAgeG1sbnM9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iCiAgICA+CiAgICAgICAgPElzc3Vlcj5odHRwczovL3N0cy53aW5kb3dzLm5ldC8wOGYzODEzYy05ZjI5LTQ4MmYtOWFlYy0xNmVmN2NiZjQ3N2EvPC9Jc3N1ZXI+CiAgICAgICAgPFN1YmplY3Q+CiAgICAgICAgICAgIDxOYW1lSUQgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDp0cmFuc2llbnQiPgogICAgICAgICAgICAgICAgVGJsYnNkZnIyT05JN3ptRTFKaTJVcDdXUG9HNzNOL1hiUG5Ob1hqSEN3Q0hjPQogICAgICAgICAgICA8L05hbWVJRD4KICAgICAgICAgICAgPFN1YmplY3RDb25maXJtYXRpb24gTWV0aG9kPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y206YmVhcmVyIj4KICAgICAgICAgICAgICAgIDxTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBJblJlc3BvbnNlVG89ImR1bW15IgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5vdE9uT3JBZnRlcj0iMjAyMS0xMC0zMFQxMTowODoyMi40MzhaIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIFJlY2lwaWVudD0iaHR0cHM6Ly9pZHAuZmVsbGVza29tcG9uZW50Lm5vL25pZHAvc2FtbDIvc3Bhc3NlcnRpb25fY29uc3VtZXIiCiAgICAgICAgICAgICAgICAvPgogICAgICAgICAgICA8L1N1YmplY3RDb25maXJtYXRpb24+CiAgICAgICAgPC9TdWJqZWN0PgogICAgICAgIDxDb25kaXRpb25zIE5vdEJlZm9yZT0iMjAyMS0xMC0zMFQxMDowMzoyMi40MzhaIgogICAgICAgICAgICAgICAgICAgIE5vdE9uT3JBZnRlcj0iMjAyMS0xMC0zMFQxMTowODoyMi40MzhaIgogICAgICAgID4KICAgICAgICAgICAgPEF1ZGllbmNlUmVzdHJpY3Rpb24+CiAgICAgICAgICAgICAgICA8QXVkaWVuY2U+aHR0cHM6Ly9pZHAuZmVsbGVza29tcG9uZW50Lm5vL25pZHAvc2FtbDIvbWV0YWRhdGE8L0F1ZGllbmNlPgogICAgICAgICAgICA8L0F1ZGllbmNlUmVzdHJpY3Rpb24+CiAgICAgICAgPC9Db25kaXRpb25zPgogICAgICAgIDxBdHRyaWJ1dGVTdGF0ZW1lbnQ+CiAgICAgICAgICAgIDxBdHRyaWJ1dGUgTmFtZT0iaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9pZGVudGl0eS9jbGFpbXMvdGVuYW50aWQiPgogICAgICAgICAgICAgICAgPEF0dHJpYnV0ZVZhbHVlPkE3RDhCN0EwLTUxQTctNEMxRC1BRjM3LUVFRjBEN0U1QjFFRjwvQXR0cmlidXRlVmFsdWU+CiAgICAgICAgICAgIDwvQXR0cmlidXRlPgogICAgICAgICAgICA8QXR0cmlidXRlIE5hbWU9Imh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vaWRlbnRpdHkvY2xhaW1zL29iamVjdGlkZW50aWZpZXIiPgogICAgICAgICAgICAgICAgPEF0dHJpYnV0ZVZhbHVlPkE3RDhCN0EwLTUxQTctNEMxRC1BRjM3LUVFRjBEN0U1QjFFRjwvQXR0cmlidXRlVmFsdWU+CiAgICAgICAgICAgIDwvQXR0cmlidXRlPgogICAgICAgICAgICA8QXR0cmlidXRlIE5hbWU9Imh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vaWRlbnRpdHkvY2xhaW1zL2Rpc3BsYXluYW1lIj4KICAgICAgICAgICAgICAgIDxBdHRyaWJ1dGVWYWx1ZT5PbGEgTm9ybWFuPC9BdHRyaWJ1dGVWYWx1ZT4KICAgICAgICAgICAgPC9BdHRyaWJ1dGU+CiAgICAgICAgICAgIDxBdHRyaWJ1dGUgTmFtZT0iaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9jbGFpbXMvYXV0aG5tZXRob2RzcmVmZXJlbmNlcyI+CiAgICAgICAgICAgICAgICA8QXR0cmlidXRlVmFsdWU+aHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS93cy8yMDA4LzA2L2lkZW50aXR5L2F1dGhlbnRpY2F0aW9ubWV0aG9kL3Bhc3N3b3JkCiAgICAgICAgICAgICAgICA8L0F0dHJpYnV0ZVZhbHVlPgogICAgICAgICAgICA8L0F0dHJpYnV0ZT4KICAgICAgICA8L0F0dHJpYnV0ZVN0YXRlbWVudD4KICAgICAgICA8QXV0aG5TdGF0ZW1lbnQgQXV0aG5JbnN0YW50PSIyMDIxLTEwLTMwVDEwOjA4OjE5LjQyN1oiCiAgICAgICAgICAgICAgICAgICAgICAgIFNlc3Npb25JbmRleD0iX2ZhZTRkMDRkLTUzZGEtNDY1Yy04NTMxLWMzYmJkYjk4NDYwMCIKICAgICAgICA+CiAgICAgICAgICAgIDxBdXRobkNvbnRleHQ+CiAgICAgICAgICAgICAgICA8QXV0aG5Db250ZXh0Q2xhc3NSZWY+dXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFjOmNsYXNzZXM6UGFzc3dvcmQ8L0F1dGhuQ29udGV4dENsYXNzUmVmPgogICAgICAgICAgICA8L0F1dGhuQ29udGV4dD4KICAgICAgICA8L0F1dGhuU3RhdGVtZW50PgogICAgPC9Bc3NlcnRpb24+Cjwvc2FtbHA6UmVzcG9uc2U+"
        def xPath = XPathFactory.newInstance().newXPath();

        when:
        def document = samlResponseService.decodeSAMLResponse(samlResponse)
        def displayNameClainValue = xPath
                .compile("//Attribute[@Name=\"http://schemas.microsoft.com/identity/claims/displayname\"]/AttributeValue")
                .evaluate(document, XPathConstants.STRING)


        then:
        document
        displayNameClainValue == "Ola Norman"
    }
}
