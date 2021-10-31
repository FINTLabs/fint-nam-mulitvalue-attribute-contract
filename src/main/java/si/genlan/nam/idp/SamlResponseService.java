package si.genlan.nam.idp;

import org.opensaml.xml.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class SamlResponseService {

    private final Tracer tracer;
    private final Properties properties;

    public SamlResponseService(Tracer tracer, Properties properties) {
        this.tracer = tracer;
        this.properties = properties;
    }

    public Map<String, String> getSamlResponseAttributes(String samlResponse) {
        Document decodedSamlResponseDocument = decodeSAMLResponse(samlResponse);
        NodeList nodeList = decodedSamlResponseDocument.getElementsByTagName("saml:Attribute");
        tracer.trace("doAuthenticate: Found nodes: " + nodeList.getLength());
        Map<String, String> objectObjectHashMap = new HashMap<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element e = (Element) nodeList.item(i);
            String name = e.getAttribute("Name");
            if (properties.getProperty(name) != null) {
                name = properties.getProperty(name);
            }
            NodeList children = nodeList.item(i).getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                String childValue = children.item(j).getTextContent();
                //attributeRepository.add(name, childValue);
                objectObjectHashMap.put(name, childValue);
            }
        }

        return objectObjectHashMap;
    }

    public Document decodeSAMLResponse(String responseMessage) {
        byte[] base64DecodedResponse = Base64.decode(responseMessage);
        String decodedResponse = new String(base64DecodedResponse);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(decodedResponse)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }
}
