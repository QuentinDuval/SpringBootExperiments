package com.example.threading;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private Controller controller;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void init_works() {
		Assert.assertNotNull(controller);
		Assert.assertNotNull(restTemplate);
	}

	@Test
	public void test_synchronous_wait() {
		final int REQUEST_COUNT = 50;
		final int WAITING_TIME = 1000;

		ArrayList<CompletableFuture<String>> futures = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
		for (int i = 0; i < REQUEST_COUNT; i++) {
			CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> getWaitFor(false, WAITING_TIME), executor);
			futures.add(future);
			future.thenAccept(s -> System.out.println(s));
		}

		futures.stream().map(t -> t.join()).collect(Collectors.toList());
	}

	@Test
	public void test_asynchronous_wait() {
		final int REQUEST_COUNT = 50;
		final int WAITING_TIME = 1000;

		ArrayList<CompletableFuture<String>> futures = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
		for (int i = 0; i < REQUEST_COUNT; i++) {
			CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> getWaitFor(true, WAITING_TIME), executor);
			futures.add(future);
			future.thenAccept(s -> System.out.println(s));
		}

		futures.stream().map(t -> t.join()).collect(Collectors.toList());
	}

	private String getWaitFor(boolean async, long millis) {
		String startTime = Utils.getCurrentTime();
		String url = "";
		if (async) {
			url = "http://localhost:" + port + "/threading/wait-async";
		} else {
			url = "http://localhost:" + port + "/threading/wait";
		}
		String endTime = restTemplate.getForObject(url + "?millis=" + millis, String.class);
		return "Received: " + startTime + " => " + endTime;
	}
}
