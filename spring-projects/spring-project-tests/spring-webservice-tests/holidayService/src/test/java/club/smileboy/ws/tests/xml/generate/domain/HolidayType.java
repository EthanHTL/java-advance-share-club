
package club.smileboy.ws.tests.xml.generate.domain;

import jakarta.xml.bind.annotation.XmlAccessType;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for HolidayType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="HolidayType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="StartDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="EndDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@jakarta.xml.bind.annotation.XmlAccessorType(XmlAccessType.FIELD)
@jakarta.xml.bind.annotation.XmlType(name = "HolidayType", propOrder = {
        "startDate",
        "endDate"
}, namespace = "http://mycompany.com/hr/schemas")
public class HolidayType {

    //    @XmlElement(name = "StartDate", required = true,namespace = "http://mycompany.com/hr/schemas")
    @jakarta.xml.bind.annotation.XmlElement(name = "StartDate", required = true, namespace = "http://mycompany.com/hr/schemas")
    @jakarta.xml.bind.annotation.XmlSchemaType(name = "date")
    protected XMLGregorianCalendar startDate;
    //    @XmlElement(name = "EndDate", required = true,namespace = "http://mycompany.com/hr/schemas")
    @jakarta.xml.bind.annotation.XmlSchemaType(name = "date")
    @jakarta.xml.bind.annotation.XmlElement(name = "EndDate", required = true, namespace = "http://mycompany.com/hr/schemas")
    protected XMLGregorianCalendar endDate;

    /**
     * Gets the value of the startDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the endDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setEndDate(XMLGregorianCalendar value) {
        this.endDate = value;
    }

}
