package si.genlan.nam.idp;

import org.opensaml.xml.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SamlResponseService {

    private final Tracer tracer;
    private final Properties properties;

    public SamlResponseService(Properties properties) {
        if(properties.containsKey(AttributesQueryConstants.PROP_NAME_TRACE)) {
            this.tracer = Tracer.getInstance(properties.getProperty(AttributesQueryConstants.PROP_NAME_TRACE));
        }
        else
            this.tracer = Tracer.getInstance("true");

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
    public void AddDecodedSamlResponseToList(String responseMessage, String matchingAttribute){
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = null;
            Document document = decodeSAMLResponse(responseMessage);

            nodes = (NodeList) xPath.compile("//Attribute/*").evaluate(document, XPathConstants.NODESET);
            tracer.trace("\nNodeLength " + nodes.getLength());
            for(int i = 0; i < nodes.getLength(); i++)
            {

                Element e = (Element) nodes.item(i).getParentNode();
                String attributeValue = nodes.item(i).getTextContent();
                String attributeName = e.getAttribute("Name");

                tracer.trace("\nNodeName: "+ attributeName + " Attribute Value: " + attributeValue);

                if(attributeName.equals(matchingAttribute)) {
                    UpdateUserStoreBySamlResponseContract.samlRequestVariableList.getSamlRequests().add(new SamlRequest(attributeValue, responseMessage));
                    break;
                }
            }
        } catch (Exception e1) {

            e1.printStackTrace();
        }
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
