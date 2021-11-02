package si.genlan.nam.idp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import java.util.Properties;

@Data
@Builder
@AllArgsConstructor
public class LdapUserStoreRepository {

    private String providerUrl;
    private String securityPrincipal;
    private String securityCredentials;

    @Builder.Default
    private DirContext ldapConnection = null;
    private Tracer tracer;

    public LdapUserStoreRepository() {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
        try {
            ldapConnection = new InitialDirContext(env);
            tracer.trace("newConnection: LDAP Connection Established: " + ldapConnection);

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(String name, ModificationItem[] modificaitonItems) throws NamingException {
        ldapConnection.modifyAttributes(name, modificaitonItems);
    }
}
