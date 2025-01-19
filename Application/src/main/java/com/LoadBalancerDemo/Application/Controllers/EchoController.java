package com.LoadBalancerDemo.Application.Controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {
    @PostMapping(value = "/echo")
    public Object echo(@RequestBody Object request) {
        return request;
    }
}