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
                                        //메시지의 타이틀     본문
//            sseEmitter.send(SseEmitter.event().name("ordered").data(data));
//            redis pub sub기능을 활용하여 메시지 publish
            redisTemplate.convertAndSend("order-channel", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channelName = new String(pattern);
        System.out.println("channelName:" + channelName);

        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            System.out.println("message:" + dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
