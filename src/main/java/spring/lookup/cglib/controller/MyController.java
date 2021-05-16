package spring.lookup.cglib.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import spring.lookup.entity.Command;

/**
 * @author JASONJ
 * @dateTime: 2021-05-04 12:17:52
 * @description: my controller
 */
@Component
public class MyController {
    @Autowired
    @Qualifier("command2")
    private Command command;

    public void printf(){
        System.out.println(command);
    }
}
