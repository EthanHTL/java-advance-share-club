package validation;

import lombok.Data;

/**
 * @author JASONJ
 * @dateTime: 2021-05-28 16:47:22
 * @description: Customer
 */
@Data
public class Customer {
    private String name;

    private Address address;
}
