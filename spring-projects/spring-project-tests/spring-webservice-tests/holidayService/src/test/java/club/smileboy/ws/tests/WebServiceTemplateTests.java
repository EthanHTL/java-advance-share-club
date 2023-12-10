package club.smileboy.ws.tests;

import club.smileboy.ws.tests.xml.generate.domain.EmployeeType;
import club.smileboy.ws.tests.xml.generate.domain.HolidayRequest;
import club.smileboy.ws.tests.xml.generate.domain.HolidayType;
import club.smileboy.ws.tests.xml.generate.domain.ObjectFactory;
import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.destination.DestinationProvider;
import org.springframework.ws.client.support.destination.Wsdl11DestinationProvider;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author jasonj
 * @date 2023/9/27
 * @time 15:50
 * @description
 **/
@SpringJUnitConfig
public class WebServiceTemplateTests {

    @Configuration
    public static class Config {


        @Bean
        public WebServiceTemplate webServiceTemplate() {
            WebServiceTemplateBuilder webServiceTemplateBuilder = new WebServiceTemplateBuilder()
                    .setWebServiceMessageFactory(webServiceMessageFactory())
                    .setDestinationProvider(destinationProvider())
                    .messageSenders(webServiceMessageSender())
                    .setMarshaller(jaxb2Marshaller())
                    .setUnmarshaller(jaxb2Marshaller())
                    .setWebServiceMessageFactory(webServiceMessageFactory());
            return webServiceTemplateBuilder.build();
        }

        @Bean
        public WebServiceMessageFactory webServiceMessageFactory() {
            return new SaajSoapMessageFactory();
        }

        @Bean
        public WebServiceMessageSender webServiceMessageSender() {
            return new HttpUrlConnectionMessageSender();
        }

        @Bean
        public DestinationProvider destinationProvider() {
            Wsdl11DestinationProvider wsdl11DestinationProvider = new Wsdl11DestinationProvider();
            wsdl11DestinationProvider.setWsdl(
                    new ClassPathResource("/wsdl/holiday.wsdl")
            );
            return wsdl11DestinationProvider;
        }


        // Marshaller or UnMarshaller

        @Bean
        public Jaxb2Marshaller jaxb2Marshaller() {
            Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();

            Map<String, String> namespacePrefixMapper = new HashMap<>();
            namespacePrefixMapper.put("http://mycompany.com/hr/schemas", "hr"); // 将命名空间映射为 "hr"
            jaxb2Marshaller.setMarshallerProperties(Collections.singletonMap(
                    "org.glassfish.jaxb.namespacePrefixMapper",new NamespacePrefixMapper() {

                @Override
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                    if ("http://mycompany.com/hr/schemas".equals(namespaceUri)) {
                        return "hr"; // 将命名空间映射为 "hr"
                    }
                    return suggestion;
                }
            }
            ));

//            jaxb2Marshaller.setContextPath("club.smileboy.ws.tests.xml.generate.domain");
            jaxb2Marshaller.setClassesToBeBound(HolidayType.class,
                    EmployeeType.class, HolidayRequest.class, ObjectFactory.class);

            return jaxb2Marshaller;
        }
    }

    @Autowired
    private WebServiceTemplate webServiceTemplate;


    @Test
    public void test() throws DatatypeConfigurationException {
        HolidayRequest payload = new HolidayRequest();
        EmployeeType employee = new EmployeeType();
        payload.setEmployee(employee);
        HolidayType holiday = new HolidayType();

        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(
                new GregorianCalendar()
        );
        holiday.setStartDate(xmlGregorianCalendar);

        XMLGregorianCalendar xmlGregorianCalendar1 = datatypeFactory.newXMLGregorianCalendar(
                new GregorianCalendar()
        );
        xmlGregorianCalendar1
                .add(datatypeFactory.newDuration(
                        3 * 24 * 60 * 60 * 1000
                ));
        holiday.setEndDate(
                xmlGregorianCalendar1
        );
        payload.setHoliday(holiday);
        Object value = webServiceTemplate.marshalSendAndReceive(
                payload
        );

        System.out.println(value);
    }
}
