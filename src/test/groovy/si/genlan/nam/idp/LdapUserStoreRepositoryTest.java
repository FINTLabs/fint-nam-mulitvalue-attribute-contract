package si.genlan.nam.idp;

import si.genlan.nam.repositories.LdapUserStoreRepository;

class LdapUserStoreRepositoryTest {
    LdapUserStoreRepository ldap = LdapUserStoreRepository
            .builder()
            .securityCredentials("SNovak1928!")
            .securityPrincipal("cn=admin,ou=sa,o=system")
            .providerUrl("ldaps://10.10.3.63:636")
            .tracer(Tracer.getInstance(Boolean.toString(true), "LDAP UserStore Repository"))
            .build();
}