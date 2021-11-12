package si.genlan.nam.idp;

import com.novell.nidp.NIDPSession;
import com.novell.nidp.NIDPSubject;
import com.novell.nidp.liberty.wsc.cache.WSCCacheEntry;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushedCache;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushedCacheSet;
import com.novell.nidp.liberty.wsf.idsis.ldapservice.schema.LDAPAttributeValue;
import com.novell.nidp.liberty.wsf.idsis.ldapservice.schema.LDAPUserAttribute;

import java.util.*;

public class SamlResponseService {

    private final Tracer tracer;
    private final Properties properties;
    private CacheService cacheService;

    public SamlResponseService(Properties properties) {
        if(properties.containsKey(AttributesQueryConstants.PROP_NAME_TRACE)) {
            this.tracer = Tracer.getInstance(properties.getProperty(AttributesQueryConstants.PROP_NAME_TRACE), "SamlResponseService GenLan");
        }
        else
            this.tracer = Tracer.getInstance("true", "SamlResponseService GenLan");

        this.properties = properties;
        tracer.trace("SamlResponseService initiated!");
        this.cacheService = CacheService
                .builder()
                .tracer(tracer)
                .build();
    }
    public Map<String, List<String>> getAccessManagerUserAttribute(NIDPSession m_Session)
    {
        Map<String, List<String>> objectObjectHashMap = new HashMap<>();
        tracer.trace("m_Session SamlResponse: " + m_Session);
        if(m_Session != null) {
            tracer.trace("m_Session ID: " +  m_Session.getID());
            NIDPSubject subject = m_Session.getSubject();
            if(subject != null)
            {
                WSCCachePushedCache cache = cacheService.getPushedCache(m_Session);
                Iterator<WSCCachePushedCacheSet> iterator = cache.iterator();
                while(iterator.hasNext())
                {
                    WSCCachePushedCacheSet xy = iterator.next();
                    tracer.trace(xy.toString());
                    for (WSCCacheEntry xyEntry : xy.getEntries()) {
                        tracer.trace("Select String: "+xyEntry.getSelectString()); //Get Select String

                        String str1  = xyEntry.getSelectString();
                        str1 = str1.replace("/UserAttribute[@ldap:targetAttribute=\"", ""); //GETTING ATTRIBUTE NAME
                        str1 = str1.replace("\"]", ""); //GETTING ATTRIBUTE NAME
                        //tracer.trace("Data Item Class" + xyEntry.getDataItemValue().getClass());
                        if(xyEntry.getDataItemValue() instanceof LDAPUserAttribute) {

                            LDAPUserAttribute ldapAttr = (LDAPUserAttribute) xyEntry.getDataItemValue(); //CHANGING INTO RIGHT CLASS
                            Iterator<LDAPAttributeValue> values = ldapAttr.getValues(); //GETS ALL VALUES FROM LDAP

                            if (values != null) {
                                List<String> valuesList = new ArrayList<>();
                                valuesList.clear();
                                while (values.hasNext()) {

                                    LDAPAttributeValue val = values.next(); //GET VALUE
                                    valuesList.add(val.getValue());
                                    tracer.trace("Adding LDAP Attribute: " + str1 + " LDAP Value : " + val.getValue());
                                }
                                objectObjectHashMap.put(str1, valuesList);
                            }
                        }
                        else
                            tracer.trace("Not instance of LDAPUserAttribute but: " + xyEntry.getDataItemValue().getClass());
                    }
                }
            }
            else
                tracer.trace("m_Session subject is null!");
        }
        else
            tracer.trace("m_Session null");

        return objectObjectHashMap;
    }
}
