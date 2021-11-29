package si.genlan.nam.services;

import com.novell.nidp.NIDPSession;
import com.novell.nidp.NIDPSubject;
import com.novell.nidp.liberty.wsc.cache.WSCCacheEntry;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushedCache;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushedCacheSet;
import com.novell.nidp.liberty.wsf.idsis.ldapservice.schema.LDAPAttributeValue;
import com.novell.nidp.liberty.wsf.idsis.ldapservice.schema.LDAPUserAttribute;
import si.genlan.nam.constants.AttributesQueryConstants;
import si.genlan.nam.idp.Tracer;

import java.util.*;

public class SamlResponseService {

    private final Tracer tracer;
    private final Properties properties;
    private CacheService cacheService;

    public SamlResponseService(Properties properties) {
        if (properties.containsKey(AttributesQueryConstants.PROP_NAME_TRACE)) {
            this.tracer = Tracer.getInstance(properties.getProperty(AttributesQueryConstants.PROP_NAME_TRACE));
        } else {
            this.tracer = Tracer.getInstance("true");
        }

        this.properties = properties;
        tracer.trace("SamlResponseService initiated!");
        this.cacheService = CacheService
                .builder()
                .tracer(tracer)
                .build();
    }

    public Map<String, List<String>> getAccessManagerUserAttribute(NIDPSession m_Session) {
        Map<String, List<String>> objectObjectHashMap = new HashMap<>();
        tracer.trace("m_Session SamlResponse: " + m_Session);
        if (m_Session != null) {
            tracer.trace("m_Session ID: " + m_Session.getID());
            NIDPSubject subject = m_Session.getSubject();
            if (subject != null) {
                WSCCachePushedCache cache = cacheService.getPushedCache(m_Session);
                Iterator<WSCCachePushedCacheSet> pushedCacheSetIterator = cache.iterator();
                while (pushedCacheSetIterator.hasNext()) {
                    WSCCachePushedCacheSet pushedCacheSet = pushedCacheSetIterator.next();
                    tracer.trace(pushedCacheSet.toString());
                    for (WSCCacheEntry cacheEntry : pushedCacheSet.getEntries()) {
                        tracer.trace("Select String: " + cacheEntry.getSelectString()); //Get Select String

                        String str1 = cacheEntry.getSelectString();
                        str1 = str1.replace("/UserAttribute[@ldap:targetAttribute=\"", ""); //GETTING ATTRIBUTE NAME
                        str1 = str1.replace("\"]", ""); //GETTING ATTRIBUTE NAME
                        //tracer.trace("Data Item Class" + cacheEntry.getDataItemValue().getClass());
                        if (cacheEntry.getDataItemValue() instanceof LDAPUserAttribute) {

                            LDAPUserAttribute ldapUserAttribute = (LDAPUserAttribute) cacheEntry.getDataItemValue(); //CHANGING INTO RIGHT CLASS
                            Iterator<LDAPAttributeValue> values = ldapUserAttribute.getValues(); //GETS ALL VALUES FROM LDAP

                            if (values != null) {
                                List<String> valuesList = new ArrayList<>();
                                valuesList.clear();
                                while (values.hasNext()) {

                                    LDAPAttributeValue ldapAttributeValue = values.next(); //GET VALUE
                                    valuesList.add(ldapAttributeValue.getValue());
                                    tracer.trace("Adding LDAP Attribute: " + str1 + " LDAP Value : " + ldapAttributeValue.getValue());
                                }
                                objectObjectHashMap.put(str1, valuesList);
                            }
                        } else
                            tracer.trace("Not instance of LDAPUserAttribute but: " + cacheEntry.getDataItemValue().getClass());
                    }
                }
            } else
                tracer.trace("m_Session subject is null!");
        } else
            tracer.trace("m_Session null");

        return objectObjectHashMap;
    }
}
