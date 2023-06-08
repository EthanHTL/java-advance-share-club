package org.example.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/simple/test")
public class SimpleTestController {

    @PutMapping
    public String formDataEarlyResolve(@RequestParam Map<String,Object> maps) {
        return "key=" + maps.getOrDefault("key","unknown");
    }

    @PostMapping
    public String formDataHandle(@RequestParam Map<String,Object> maps) {
        return "key=" +maps.getOrDefault("key","unknown");
    }
    @GetMapping
    public String get() {
        return "ok";
    }
}
