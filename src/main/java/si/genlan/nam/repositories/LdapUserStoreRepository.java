package si.genlan.nam.repositories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import si.genlan.nam.utils.ArrayUtils;
import si.genlan.nam.idp.Tracer;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Data
@Builder
@AllArgsConstructor
public class LdapUserStoreRepository {

    private String providerUrl;
    private String securityPrincipal;
    private String securityCredentials;
    private String matchingAttributeName;


    private DirContext ldapConnection=null;
    private Tracer tracer;

    @Builder
    public LdapUserStoreRepository() {
        tracer.trace("Constructing new LDAP Class");


    }
    public void Connect(){
        tracer.trace("Constructing new LDAP Connection");
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

    public String[] getAttributeValues(List<String> attributes) throws NamingException {
        String[] multivalueStoreArray = new String[0];
        for(String s : attributes)
        {
            multivalueStoreArray = ArrayUtils.addToStringArray(multivalueStoreArray, s);
        }
        return multivalueStoreArray;
    }

    public StringBuilder getAttributeValuesString(List<String> attributes) throws NamingException {
        StringBuilder multivalueStore = new StringBuilder();
        for(String s: attributes)
        {
            multivalueStore.append(s).append("; ");
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
                tracer.trace("Adding value " + s + " to attribute " + attributeName);

            }
        }
        return mods;
    }

    public ModificationItem[] AttributeValuesToDeleteFromUserStore(String[] responseAttributeValues, String[] userStoreAttributeValues, String attributeName)
    {
        ModificationItem[] mods = new ModificationItem[0];
        for (String s : userStoreAttributeValues) {
            if (!(Arrays.asList(responseAttributeValues).contains(s))) {
                mods = Arrays.copyOf(mods, mods.length + 1);
                mods[mods.length - 1] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(attributeName, s));
                tracer.trace("Removing value " + s + " from attribute " + attributeName);

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
