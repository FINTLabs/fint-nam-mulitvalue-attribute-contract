package si.genlan.nam.idp;

import com.novell.nidp.servlets.NIDPServlet;
import org.opensaml.xml.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Properties;

public class SamlServlet extends NIDPServlet {
    public static String getOne = "null";
    public static String SamlResponse = "";
    public static String matchingAttr;
    public static ArrayList<SamlResponseAttribute> gotAttributes = new ArrayList<SamlResponseAttribute>();
    public Tracer tracer;

    public SamlServlet() {
        super();
        tracer = Tracer.getInstance("true");
        matchingAttr = UpdateUserStoreBySamlResponseContract.matchingAttribute;
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        gotAttributes.clear();
        resp.setContentType("text/html");
        tracer.trace("readAttributesServlet: doPost()");
        String respMsg = req.getParameter("SAMLResponse");
        SamlResponseService responseService = new SamlResponseService(new Properties());

        if (respMsg != null) {
            SamlResponse = respMsg;
            tracer.trace("readAttributesServlet: doPost(): saml: " + respMsg);
            String xmlBytes = respMsg;
            byte[] decodedXmlBytes = Base64.decode(xmlBytes);
            String decodedResponse = new String(decodedXmlBytes, 0, decodedXmlBytes.length);
            tracer.trace("readAttributesServlet: doPost(): saml-decoded: " + decodedResponse);
            tracer.trace("readAttributesServlet: doPost(): matching attribute: " + matchingAttr);
            responseService.AddDecodedSamlResponseToList(respMsg, matchingAttr);


        } else if (req.getParameter("SAMLart") != null) {
            String respMsgArt = req.getParameter("SAMLart");
            tracer.trace("readAttributesServlet: doPost(): Artifact message found: " + respMsgArt);
        } else
            tracer.trace("readAttributesServlet: doPost(): no saml message");


        super.doPost(req, resp);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        resp.setContentType("text/html");
        tracer.trace("readAttributesServlet: doGet()");
        super.doGet(req, resp);

    }

}
