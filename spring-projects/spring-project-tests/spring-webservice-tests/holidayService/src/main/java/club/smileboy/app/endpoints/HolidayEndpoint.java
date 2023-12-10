package club.smileboy.app.endpoints;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import club.smileboy.app.services.HumanResourceService;
import org.jdom2.NamespaceAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

@Endpoint
public class HolidayEndpoint {

    private static final String NAMESPACE_URI = "http://mycompany.com/hr/schemas";

    private XPathExpression<Element> startDateExpression;

    private XPathExpression<Element> endDateExpression;

    private XPathExpression<Element> firstNameExpression;

    private XPathExpression<Element> lastNameExpression;

    private HumanResourceService humanResourceService;

    @Autowired
    public HolidayEndpoint(HumanResourceService humanResourceService) throws JDOMException {
        this.humanResourceService = humanResourceService;

        Namespace namespace = Namespace.getNamespace("hr", "http://mycompany.com/hr/schemas");
        XPathFactory xPathFactory = XPathFactory.instance();
        startDateExpression = xPathFactory.compile("//hr:StartDate", Filters.element(), null, Namespace.getNamespace("hr", "http://mycompany.com/hr/schemas"));
        endDateExpression = xPathFactory.compile("//hr:EndDate", Filters.element(), null, namespace);
        firstNameExpression = xPathFactory.compile("//hr:FirstName", Filters.element(), null, namespace);
        lastNameExpression = xPathFactory.compile("//hr:LastName", Filters.element(), null, namespace);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "HolidayRequest")
    public void handleHolidayRequest(@RequestPayload Element holidayRequest) throws Exception {
        Date startDate = parseDate(startDateExpression, holidayRequest);
        Date endDate = parseDate(endDateExpression, holidayRequest);
        String name = firstNameExpression.evaluateFirst(holidayRequest).getText() + " " + lastNameExpression.evaluateFirst(holidayRequest).getText();

        humanResourceService.bookHoliday(startDate, endDate, name);
    }

    private Date parseDate(XPathExpression<Element> expression, Element element) throws ParseException {
        Element result = expression.evaluateFirst(element);
        if (result != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(result.getText());
        } else {
            throw new IllegalArgumentException("Could not evaluate [" + expression + "] on [" + element + "]");
        }
    }

}

