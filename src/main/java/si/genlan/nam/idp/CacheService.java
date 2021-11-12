package si.genlan.nam.idp;

import com.novell.nidp.NIDPSession;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushed;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushedCache;
import com.novell.nidp.servlets.NIDPServletSession;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CacheService {
    private Tracer tracer;

    public WSCCachePushedCache getPushedCache(NIDPSession m_Session)
    {
        NIDPServletSession sessionServlet = (NIDPServletSession)m_Session;
        WSCCachePushed cachePushed = WSCCachePushed.getInstance();
        WSCCachePushedCache cache = cachePushed.getCache(m_Session.getID());
        if ( cache == null )
        {
            cache = new WSCCachePushedCache();
        }

        tracer.trace("WSCCache Size: " + cache.size());
        tracer.trace("WSCCache: " + cache.toString());
        tracer.trace("WSCCache Iterator: " + cache.iterator());

        return cache;
    }
}
