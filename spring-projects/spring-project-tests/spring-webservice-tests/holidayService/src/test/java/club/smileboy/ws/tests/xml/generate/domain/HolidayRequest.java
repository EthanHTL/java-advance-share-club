
package club.smileboy.ws.tests.xml.generate.domain;


import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Holiday" type="{http://mycompany.com/hr/schemas}HolidayType"/&gt;
 *         &lt;element name="Employee" type="{http://mycompany.com/hr/schemas}EmployeeType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@jakarta.xml.bind.annotation.XmlAccessorType(jakarta.xml.bind.annotation.XmlAccessType.FIELD)
@jakarta.xml.bind.annotation.XmlType(name = "", propOrder = {
    "holiday",
    "employee"
})
@XmlRootElement(name = "HolidayRequest")
@jakarta.xml.bind.annotation.XmlRootElement(name = "HolidayRequest",namespace = "http://mycompany.com/hr/schemas")
public class HolidayRequest {

    @XmlElement(name = "Holiday", required = true)
    protected HolidayType holiday;
    @XmlElement(name = "Employee", required = true)
    protected EmployeeType employee;

    /**
     * Gets the value of the holiday property.
     * 
     * @return
     *     possible object is
     *     {@link HolidayType }
     *     
     */
    public HolidayType getHoliday() {
        return holiday;
    }

    /**
     * Sets the value of the holiday property.
     * 
     * @param value
     *     allowed object is
     *     {@link HolidayType }
     *     
     */
    public void setHoliday(HolidayType value) {
        this.holiday = value;
    }

    /**
     * Gets the value of the employee property.
     * 
     * @return
     *     possible object is
     *     {@link EmployeeType }
     *     
     */
    public EmployeeType getEmployee() {
        return employee;
    }

    /**
     * Sets the value of the employee property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmployeeType }
     *     
     */
    public void setEmployee(EmployeeType value) {
        this.employee = value;
    }

}
