
package com.mycompany.hr.definitions;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "HumanResourceService", targetNamespace = "http://mycompany.com/hr/definitions", wsdlLocation = "file:/Users/jasonj/software/develop-tools/idea-workspace/spring-projects/java-advance-share-club/spring-projects/spring-project-tests/spring-webservice-tests/holidayService/src/test/resources/wsdl/holiday.wsdl")
public class HumanResourceService
    extends Service
{

    private final static URL HUMANRESOURCESERVICE_WSDL_LOCATION;
    private final static WebServiceException HUMANRESOURCESERVICE_EXCEPTION;
    private final static QName HUMANRESOURCESERVICE_QNAME = new QName("http://mycompany.com/hr/definitions", "HumanResourceService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("file:/Users/jasonj/software/develop-tools/idea-workspace/spring-projects/java-advance-share-club/spring-projects/spring-project-tests/spring-webservice-tests/holidayService/src/test/resources/wsdl/holiday.wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        HUMANRESOURCESERVICE_WSDL_LOCATION = url;
        HUMANRESOURCESERVICE_EXCEPTION = e;
    }

    public HumanResourceService() {
        super(__getWsdlLocation(), HUMANRESOURCESERVICE_QNAME);
    }

    public HumanResourceService(WebServiceFeature... features) {
        super(__getWsdlLocation(), HUMANRESOURCESERVICE_QNAME, features);
    }

    public HumanResourceService(URL wsdlLocation) {
        super(wsdlLocation, HUMANRESOURCESERVICE_QNAME);
    }

    public HumanResourceService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, HUMANRESOURCESERVICE_QNAME, features);
    }

    public HumanResourceService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public HumanResourceService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns HumanResource
     */
    @WebEndpoint(name = "HumanResourceSoap11")
    public HumanResource getHumanResourceSoap11() {
        return super.getPort(new QName("http://mycompany.com/hr/definitions", "HumanResourceSoap11"), HumanResource.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns HumanResource
     */
    @WebEndpoint(name = "HumanResourceSoap11")
    public HumanResource getHumanResourceSoap11(WebServiceFeature... features) {
        return super.getPort(new QName("http://mycompany.com/hr/definitions", "HumanResourceSoap11"), HumanResource.class, features);
    }

    private static URL __getWsdlLocation() {
        if (HUMANRESOURCESERVICE_EXCEPTION!= null) {
            throw HUMANRESOURCESERVICE_EXCEPTION;
        }
        return HUMANRESOURCESERVICE_WSDL_LOCATION;
    }

}
