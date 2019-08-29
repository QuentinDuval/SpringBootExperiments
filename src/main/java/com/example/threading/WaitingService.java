package com.example.threading;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
public class WaitingService {

    public String trySleep(long millis) {
        forceSleep(millis);
        return Utils.getCurrentTime();
    }

    @Async(value = "executor1")
    public Future<String> trySleepAsync(long millis) {
        forceSleep(millis);
        return new AsyncResult<>(Utils.getCurrentTime());
    }

    private void forceSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
