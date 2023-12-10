package club.smileboy.ws.tests.xpath.tests;

import org.jdom2.Document;
import org.junit.jupiter.api.Test;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.xpath.NodeMapper;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author jasonj
 * @date 2023/9/27
 * @time 23:15
 * @description
 **/
public class XpathExpressionTests {

    @Test
    public void test() throws ParserConfigurationException, IOException, SAXException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<hr:HolidayRequest xmlns:hr=\"http://mycompany.com/hr/schemas\">\n" +
                "  <holiday>\n" +
                "    <hr:StartDate>2023-09-27+08:00</hr:StartDate>\n" +
                "    <hr:EndDate>2023-09-30+08:00</hr:EndDate>\n" +
                "  </holiday>\n" +
                "  <employee />\n" +
                "</hr:HolidayRequest>\n";

        StringSource stringSource = new StringSource(xml);
        Element documentElement = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(stringSource.getInputStream()))
                .getDocumentElement();

        XPathExpression xPathExpression = XPathExpressionFactory.createXPathExpression("//StartDate");
        xPathExpression.evaluateAsObject(documentElement, new NodeMapper<Object>() {
            @Override
            public Object mapNode(Node node, int i) throws DOMException {
                System.out.println(node);
                return null;
            }
        });
    }
}
