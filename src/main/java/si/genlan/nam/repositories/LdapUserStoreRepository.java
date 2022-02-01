package si.genlan.nam.repositories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.genlan.nam.idp.Tracer;
import si.genlan.nam.utils.ArrayUtils;

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

    private String[] providerUrl;
    private String[] securityPrincipal;
    private String[] securityCredentials;
    private String[] securityProtocol;

    @Builder.Default
    private DirContext ldapConnection = null;
    private Tracer tracer;

    public LdapUserStoreRepository connect(int index) {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl[index]);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal[index]);
        env.put(Context.SECURITY_CREDENTIALS, securityCredentials[index]);
        env.put(Context.SECURITY_PROTOCOL, securityProtocol[index]);
        try {
            ldapConnection = new InitialDirContext(env);
            tracer.trace("newConnection: LDAP Connection Established: " + ldapConnection);
            return this;
        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateUser(String name, ModificationItem[] modificaitonItems, String cn) throws NamingException {
        for(int i=0; i< providerUrl.length; i++)
        {
            if(findUserOnServer(i, cn)) {
                ldapConnection.modifyAttributes(name, modificaitonItems);
                break;
            }
        }

    }
    public boolean findUserOnServer(int index, String cn) throws NamingException {
        connect(index);
        String searchFilter = "(cn="+cn+")";
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setReturningAttributes(new String[]{"cn"});
        NamingEnumeration searchResult = ldapConnection.search("", searchFilter, sc);
        if(searchResult.hasMoreElements())
            return true;
        else
            return false;
    }
    public String[] getAttributeValues(List<String> attributes) throws NamingException {
        String[] multivalueStoreArray = new String[0];
        for(String s : attributes)
        {
            multivalueStoreArray = ArrayUtils.addToStringArray(multivalueStoreArray, s);
        }
        return multivalueStoreArray;
    }

    public ModificationItem[] AttributeValuesToAddToUserStore(String[] responseAttributeValues, String[] userStoreAttributeValues, String attributeName)
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
}