package club.smileboy.ws.tests;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.client.support.destination.Wsdl11DestinationProvider;

import java.net.URI;

/**
 * @author jasonj
 * @date 2023/9/27
 * @time 15:00
 * @description
 **/
public class DestinationProviderTests {

    @Test
    public void tests() {
        Wsdl11DestinationProvider wsdl11DestinationProvider = new Wsdl11DestinationProvider();
        wsdl11DestinationProvider.setWsdl(
                new ClassPathResource("/wsdl/test.wsdl")
        );

        URI destination = wsdl11DestinationProvider.getDestination();
        System.out.println(destination);
    }
}
