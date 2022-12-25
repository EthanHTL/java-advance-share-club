package validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author JASONJ
 * @dateTime: 2021-05-28 16:48:06
 * @description: customer validate
 */
public class CustomerValidator implements Validator {
    private final Validator addressValidator;
    public CustomerValidator(Validator addressValidator){
        this.addressValidator = addressValidator;
    }
    @Override
    public boolean supports(Class<?> aClass) {
        return Customer.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // 验证
        // 注册空或者空白
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required");
        // 注册空或者空白
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "surname", "field.required");
        Customer customer = (Customer) target;
        try {
            // 推入到哪一个path
            errors.pushNestedPath("address");
            //  然后调用指定的验证器去执行;
            ValidationUtils.invokeValidator(this.addressValidator, customer.getAddress(), errors);
        } finally {
            // 跳出内嵌路径
            errors.popNestedPath();
        }
    }
}
