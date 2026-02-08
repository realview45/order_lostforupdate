package com.beyond.order.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
//    SseEmitter객체는 사용자의 연결정보(ip, macaddress등)을 의미

//    ConcurrentHashMap은 Thread-Safe한 map(동시성 이슈 발생X)
                                    //연결맺으면 객체를 만들겠다.
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    public void addSseEmitter(String email, SseEmitter sseEmitter){
        this.emitterMap.put(email, sseEmitter);
    }
    public SseEmitter getEmitter(String email){
        return this.emitterMap.get(email);
    }

    public void removeEmitter(String email){
        System.out.println(email);
        this.emitterMap.remove(email);
    }
}
