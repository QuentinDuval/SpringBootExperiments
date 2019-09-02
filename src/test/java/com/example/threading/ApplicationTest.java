package com.example.threading;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private Controller controller;

	@Autowired
	private TestRestTemplate restTemplate;

	private static final int REQUEST_COUNT = 50;
	private static final int WAITING_TIME = 1000;

	@Test
	public void init_works() {
		Assert.assertNotNull(controller);
		Assert.assertNotNull(restTemplate);
	}

	@Test
	public void test_synchronous_wait() {
		ArrayList<CompletableFuture<String>> futures = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
		for (int i = 0; i < REQUEST_COUNT; i++) {
			CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> httpGetWaitFor(false, WAITING_TIME), executor);
			futures.add(future);
			future.thenAccept(s -> System.out.println(s));
		}

		futures.stream().map(t -> t.join()).collect(Collectors.toList());
	}

	@Test
	public void test_asynchronous_wait() {
		ArrayList<CompletableFuture<String>> futures = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
		for (int i = 0; i < REQUEST_COUNT; i++) {
			CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> httpGetWaitFor(true, WAITING_TIME), executor);
			futures.add(future);
			future.thenAccept(s -> System.out.println(s));
		}

		futures.stream().map(t -> t.join()).collect(Collectors.toList());
	}

	@Test
	public void test_web_sockets() throws Exception {
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(new StringMessageConverter());
		LinkedBlockingDeque<String> received = new LinkedBlockingDeque<>();

		StompSessionHandler sessionHandler = new StompSessionHandlerAdapter () {
			@Override
			public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
				AppLogger.getLogger().info("Connection established");
			}

			@Override
			public void handleException(StompSession stompSession, StompCommand stompCommand,
										StompHeaders stompHeaders, byte[] bytes, Throwable throwable) {
				AppLogger.getLogger().error("ERROR: " + throwable);
			}

			@Override
			public Type getPayloadType(StompHeaders stompHeaders) {
				return String.class;
			}

			@Override
			public void handleFrame(StompHeaders stompHeaders, Object o) {
				received.offer(o.toString());
			}
		};

		String url = "ws://localhost:" + port + "/gs-guide-websocket";
		StompSession session = stompClient.connect(url, sessionHandler).get();
		session.subscribe("/stream/echo", sessionHandler);

		String payload = new Integer(WAITING_TIME).toString();
		for (int i = 0; i < REQUEST_COUNT; i++) {
			session.send("/stream/input", payload);
		}

		int receivedCount = 0;
		while (receivedCount < REQUEST_COUNT) {
			String answer = received.poll(5, TimeUnit.SECONDS);
			System.out.println(answer);
			receivedCount += 1;
		}
	}

	private String httpGetWaitFor(boolean async, long millis) {
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
