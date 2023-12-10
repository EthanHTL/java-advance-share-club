package org.example.spring.test.review.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author jasonj
 * @date 2023/10/29
 * @time 20:19
 * @description
 **/
public class AbstractUseJsonTests {
   protected final ObjectMapper objectMapper = new ObjectMapper();

   protected String asJson(Object obj) {
       try {
           return objectMapper.writeValueAsString(obj);
       }catch (Exception e) {
           throw new IllegalArgumentException(e);
       }
   }

   protected byte[] asJsonBytes(Object obj) {
       return asJson(obj).getBytes();
   }
}
