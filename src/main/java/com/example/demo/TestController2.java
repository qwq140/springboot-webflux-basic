package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@CrossOrigin
@RestController
public class TestController2 {
	// 프로세서 사용 : 중간에 데이터가 들어와도 진행
	
	Sinks.Many<String> sink;
	
	
	// multicast() 새로 들어온 데이터만 응답받음 hot 시퀀스 = 스트림
	// replay() 기존 데이터 + 새로운 데이터 응답 cold 시퀀스
	
	
	public TestController2() {
		super();
		this.sink = Sinks.many().multicast().onBackpressureBuffer();
	}

	@GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Integer> findAll(){
		return Flux.just(1,2,3,4,5,6).log();
	}
	
	@GetMapping("/send")
	public void send() {
		sink.tryEmitNext("Hello World");
	}
	
	
	// data : 실제값 \n\n\
	@GetMapping(value = "/sse")
	public Flux<ServerSentEvent<String>> sse() { // ServcerSendEvent의 ContentType은 text event stream
		return sink.asFlux().map(e -> ServerSentEvent.builder(e).build()).doOnCancel(()->{
			System.out.println("SSE 종료됨");
			sink.asFlux().blockLast(); // response를 끊음.
		}); // 구독
	}
}
