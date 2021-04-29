package spring.lookup.entity;

/**
 * @author JASONJ
 * @dateTime: 2021-04-29 23:32:24
 * @description: plain bean
 */
public class Command {

    private Object state;

    public String execute(){
        return "command";
    }

    public void setState(Object state){
        this.state = state;
    }
}
