package club.smileboy.app.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 为了cache endpoint 而测试设置的 ...
 */
@RestController
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    private AtomicLong  seed = new AtomicLong(0);


    @GetMapping("insert")
    public void insertCache() {
        if(cacheManager != null) {
            final Cache test = cacheManager.getCache("test");
            if(test != null) {
                test.put(String.valueOf(seed.incrementAndGet()), UUID.randomUUID().toString());
            }
        }
    }


    @GetMapping("{key}")
    public String getCache(@PathVariable("key") String key) {
        if(cacheManager != null) {
            final Cache test = cacheManager.getCache("test");
            if(test != null) {
                return Optional.ofNullable(test.get(key)).map(Cache.ValueWrapper::get).orElse("").toString();
            }
        }
        return "UNKNOWN";
    }

    @GetMapping
    public void setCache(@RequestParam("key") String key,@RequestParam("value") String value) {
        if(cacheManager != null) {
            final Cache test = cacheManager.getCache("test");
            if(test != null) {
                test.put(key,value);
            }
        }
    }
}
