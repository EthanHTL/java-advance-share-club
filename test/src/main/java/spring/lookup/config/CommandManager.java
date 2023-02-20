package spring.lookup.config;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import spring.lookup.entity.Command;

/**
 * @author JASONJ
 * @dateTime: 2021-04-29 23:31:47
 * @description: simple bean
 */
public abstract class CommandManager {
    public Object process(Object commandState) {
        // grab a new instance of the appropriate Command interface
        Command command = createCommand();
        // set the state on the (hopefully brand new) Command instance
        command.setState(commandState);
        return command.execute();
    }

    // okay... but where is the implementation of this method?
    @Lookup
    protected abstract Command createCommand();
}
