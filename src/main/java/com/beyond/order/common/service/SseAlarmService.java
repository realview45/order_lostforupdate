package com.beyond.order.common.service;

import com.beyond.order.common.dtos.SseMessageDto;
import com.beyond.order.common.repository.SseEmitterRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String,String> redisTemplate;
    @Autowired
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper, @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void sendMessage(String receiver, String sender, String message) {
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);
        SseMessageDto dto = SseMessageDto.builder()
                .receiver(receiver).sender(sender).message(message).build();
        try {
            String data = objectMapper.writeValueAsString(dto);
//            만약에 emitter객체가 현재 서버에 있으면 바로 Sse알림 발송, 아니면 RedisPubSub활용
            if(sseEmitter!=null){
                                                            //메시지의 타이틀     본문
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
//                사용자가 새로고침후에 알림메시지를 조회하려면 DB에 추가적으로 저장 필요.
            }
            else{
//            redis pub sub기능을 활용하여 메시지 publish
                redisTemplate.convertAndSend("order-channel", data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channelName = new String(pattern);
        System.out.println("channelName:" + channelName);
        //메시지를 분기처리
        //        if(channelName.equals("order-channel")){
//
//        }else if(channelName.equals("create-channel")){
//
//        }
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            System.out.println("message:" + dto);
            String data = objectMapper.writeValueAsString(dto);
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());
//            해당서버에 receiver의 emitter객체가 있으면 send
            if(sseEmitter !=null){
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
