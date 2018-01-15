package project.server;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlExtractor {
    private Document document;

    public XmlExtractor(File configFile) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
        document = db.parse(configFile);
        document.getDocumentElement().normalize();
    }

    public String getRootChildText(String tagName) {
        return document.getElementsByTagName(tagName).item(0).getTextContent();
    }
}