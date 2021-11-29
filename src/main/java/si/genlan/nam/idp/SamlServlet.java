package si.genlan.nam.idp;

import com.novell.nidp.servlets.NIDPServlet;
import org.opensaml.xml.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import si.genlan.nam.attributes.SamlResponseAttribute;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;

public class SamlServlet extends NIDPServlet {
    public Tracer tracer;
    public static String getOne = "null";
    public static String SamlResponse = "";
    public static String matchingAttr;
    public static ArrayList<SamlResponseAttribute> gotAttributes = new ArrayList<SamlResponseAttribute>();

    public SamlServlet() {
        super();
        tracer = Tracer.getInstance("true");
        matchingAttr = null; //UpdateUserStoreBySamlResponseContract.matchingAttribute;
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        gotAttributes.clear();
        resp.setContentType("text/html");
        tracer.trace("readAttributesServlet: doPost()");
        String respMsg = req.getParameter("SAMLResponse");

        if (respMsg != null) {
            SamlResponse = respMsg;
            tracer.trace("readAttributesServlet: doPost(): saml: " + respMsg);
            String xmlBytes = respMsg;
            byte[] decodedXmlBytes = Base64.decode(xmlBytes);
            String decodedResponse = new String(decodedXmlBytes, 0, decodedXmlBytes.length);
            tracer.trace("readAttributesServlet: doPost(): saml-decoded: " + decodedResponse);
            tracer.trace("readAttributesServlet: doPost(): matching attribute: " + matchingAttr);
            Document samlResp;
            try {
                samlResp = decodeSAMLResponse(respMsg);
                NodeList nl = samlResp.getElementsByTagName("saml:Attribute");
                tracer.trace("Found nodes: " + nl.getLength());
                for (int i = 0; i < nl.getLength(); i++) {
                    Element e = (Element) nl.item(i);
                    String name = e.getAttribute("Name");
                    tracer.trace("readAttributesServlet: doPost(): attr name: " + name + " matcAttr: " + matchingAttr);
                    if (name.equals(matchingAttr)) {
                        NodeList children = nl.item(i).getChildNodes();
                        for (int j = 0; j < children.getLength(); j++) {
                            String childValue = children.item(j).getTextContent();
                            tracer.trace("readAttributesServlet: doPost(): Adding into svar: " + childValue);

                            // TODO: 29/11/2021 This needs refactoring
                            //  UpdateUserStoreBySamlResponseContract.samlRequestVariableList.getSamlRequests().add(new SamlRequest(childValue, respMsg));
                        }
                    }

                }
                nl = samlResp.getElementsByTagName("Attribute");
                tracer.trace("readAttributesServlet: doPost(): Found nodes: " + nl.getLength());
                for (int i = 0; i < nl.getLength(); i++) {
                    Element e = (Element) nl.item(i);
                    String name = e.getAttribute("Name");
                    tracer.trace("readAttributesServlet: doPost(): attr name: " + name + " matcAttr: " + matchingAttr);
                    if (name.equals(matchingAttr)) {
                        NodeList children = nl.item(i).getChildNodes();
                        for (int j = 0; j < children.getLength(); j++) {
                            String childValue = children.item(j).getTextContent();
                            tracer.trace("readAttributesServlet: doPost(): Adding into svar: " + childValue);

                            // TODO: 29/11/2021 This needs refactoring
                            //  UpdateUserStoreBySamlResponseContract.samlRequestVariableList.getSamlRequests().add(new SamlRequest(childValue, respMsg));
                        }
                    }

                }
            } catch (Exception e1) {

                e1.printStackTrace();
            }


        } else if (req.getParameter("SAMLart") != null) {
            String respMsgArt = req.getParameter("SAMLart");
            tracer.trace("readAttributesServlet: doPost(): Artifact message found: " + respMsgArt);
        } else
            tracer.trace("readAttributesServlet: doPost(): no saml message");


        super.doPost(req, resp);
    }

    public static Document decodeSAMLResponse(String responseMessage)
            throws Exception {
        byte[] base64DecodedResponse = Base64.decode(responseMessage);
        String decodedResponse = new String(base64DecodedResponse);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(decodedResponse)));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        resp.setContentType("text/html");
        tracer.trace("readAttributesServlet: doGet()");
        super.doGet(req, resp);

    }

}
