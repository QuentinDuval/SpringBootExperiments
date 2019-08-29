package com.example.threading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/threading")
public class Controller {

    @Autowired
    private WaitingService service;

    @RequestMapping("/wait")
    public ResponseEntity<String> waitTime(@RequestParam long millis) {
        String answer = service.trySleep(millis);
        return new ResponseEntity<>("Finished: " + answer, HttpStatus.OK);
    }

    @RequestMapping("/wait-async")
    public ResponseEntity<String> waitTimeAsync(@RequestParam long millis) throws ExecutionException, InterruptedException {
        Future<String> answer1 = service.trySleepAsync(millis / 2);
        Future<String> answer2 = service.trySleepAsync(millis / 2);
        answer1.get();
        return new ResponseEntity<>("Finished: " + answer2.get(), HttpStatus.OK);
    }
}
