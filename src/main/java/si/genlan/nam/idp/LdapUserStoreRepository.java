package si.genlan.nam.idp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Arrays;
import java.util.Properties;

@Data
@Builder
@AllArgsConstructor
public class LdapUserStoreRepository {

    private String providerUrl;
    private String securityPrincipal;
    private String securityCredentials;
    private String matchingAttributeName;

    @Builder.Default
    private DirContext ldapConnection=null;
    private Tracer tracer;

    public LdapUserStoreRepository() {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        try {
            ldapConnection = new InitialDirContext(env);
            tracer.trace("newConnection: LDAP Connection Established: " + ldapConnection);
            if(ldapConnection == null)
                tracer.trace("Ldap connection not established");

        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    public void updateUser(String name, ModificationItem[] modificationItems) throws NamingException {
        ldapConnection.modifyAttributes(name, modificationItems);
    }

    public String[] getAttributeValues(NamingEnumeration<?> attributes) throws NamingException {
        String[] multivalueStoreArray = new String[0];
        while (attributes.hasMore()) {
            String val = attributes.next().toString();
            tracer.trace("Adding attribute " + val);
            multivalueStoreArray = Arrays.copyOf(multivalueStoreArray, multivalueStoreArray.length + 1);
            multivalueStoreArray[multivalueStoreArray.length - 1] = val;
        }
        System.out.println("LDAP Store Array Out: " + Arrays.toString(multivalueStoreArray));
        return multivalueStoreArray;
    }

    public StringBuilder getAttributeValuesString(NamingEnumeration<?> attributes) throws NamingException {
        StringBuilder multivalueStore = new StringBuilder();
        while (attributes.hasMore()) {
            String val = attributes.next().toString();
            multivalueStore.append(val).append("; ");
        }

        return multivalueStore;
    }

    public ModificationItem[] AttributeValuesToAddFromUserStore(String[] responseAttributeValues, String[] userStoreAttributeValues, String attributeName)
    {
        ModificationItem[] mods = new ModificationItem[0];
        for (String s : responseAttributeValues) {
            if (!(Arrays.asList(userStoreAttributeValues).contains(s))) {
                mods = Arrays.copyOf(mods, mods.length + 1);
                mods[mods.length - 1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                        new BasicAttribute(attributeName, s));

            }
        }
        return mods;
    }

    public ModificationItem[] AttributeValuesToDeleteFromUserStore(String[] responseAttributeValues, String[] userStoreAttributeValues, String attributeName)
    {
        ModificationItem[] mods = new ModificationItem[0];
        for (String s : responseAttributeValues) {
            if (!(Arrays.asList(userStoreAttributeValues).contains(s))) {
                mods = Arrays.copyOf(mods, mods.length + 1);
                mods[mods.length - 1] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attributeName, s));

            }
        }
        return mods;
    }

    public void MatchUser(String LDAPMatchingAttributeValue) throws NamingException {
        String searchFilter = "(&(mail="+LDAPMatchingAttributeValue+"))";
        String[] requiredAttributes = {"mail"};

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(requiredAttributes);
        NamingEnumeration users;
        if(ldapConnection != null) {
            users = ldapConnection.search("o=Test", searchFilter, controls);
            SearchResult result = null;
            while(users.hasMore())
            {
                Attributes attr = result.getAttributes();
                String name = attr.get("cn").get(0).toString();
                System.out.println("Cn: "+attr.get("cn"));
                System.out.println("Sn: "+attr.get("sn"));
            }
        }
        else
            tracer.trace("Ldap connection null");


    }
}
