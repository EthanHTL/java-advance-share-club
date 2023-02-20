package validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author JASONJ
 * @dateTime: 2021-05-28 16:38:22
 * @description: validator
 */
public class PersonValidator implements Validator {
    // 当前对象是否支持验证
    @Override
    public boolean supports(Class<?> aClass) {
        return Person.class.equals(aClass);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        // 使用工具类进行验证
        // 判断是否为空,给出错误提示
        ValidationUtils.rejectIfEmpty(errors,"name","name.empty");
        Person p = (Person) obj;
        if(p.getAge() < 0){
            errors.rejectValue("age","negativeValue");
        }
        else if(p.getAge() > 110){
            errors.rejectValue("age","too.darn.old");
        }
    }
}
