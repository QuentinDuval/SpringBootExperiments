package com.example.threading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    /***
     * Documentation of what is being done is available here:
     * https://www.toptal.com/java/stomp-spring-boot-websocket
     */

    @Autowired
    WaitingService service;

    @Autowired
    SimpMessagingTemplate messaging;

    /**
     * Maps messages coming from "/stream/input" to answer in "/stream/echo"
     */
    @MessageMapping("/stream/input")
    public void greeting(@Payload String message) throws Exception {
        try {
            // In case the message is a number
            int waitingTime = Integer.parseInt(message);
            String answer = service.trySleep(waitingTime);
            messaging.convertAndSend("/stream/echo", answer);
        } catch (NumberFormatException e){
            // In case it is not a number
            messaging.convertAndSend("/stream/echo", message);
        }
    }
}
