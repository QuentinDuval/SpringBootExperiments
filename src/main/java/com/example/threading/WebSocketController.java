package com.example.threading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    WaitingService service;

    @MessageMapping("/stream/input")
    @SendTo("/stream/echo")
    public String greeting(String message) throws Exception {
        service.trySleep(1000);
        return message;
    }
}
