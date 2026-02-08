package com.beyond.order.common.configs;

import com.beyond.order.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
//    연결빈객체(0~9번인지)호스트, 포트정보
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Bean  //인터페이스
//    Qualifier :같은 Bean객체가 여러개 있을경우 Bean객체를 구분하기 위한 어노테이션
    @Qualifier("rtInventory")//빈객체에 이름붙이기
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
                    //구현체
        return new LettuceConnectionFactory();
    }
    @Bean  //인터페이스
    @Qualifier("stockInventory")
    public RedisConnectionFactory stockConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        //구현체
        return new LettuceConnectionFactory();
    }
//    템플릿빈객체(자료구조 설계)
    @Bean//이 빈을 주입받아 redis에 저장할 예정
    @Qualifier("rtInventory")
//    모든 template중에 무조건 redisTemplate이라는 메서드명이 반드시 1개는 있어야함.
    public RedisTemplate<String, String>redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());//다문자열이지만 이건리스트 셋,해시스,지셋이라는 태그를 가진다.
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
    @Bean//이 빈을 주입받아 redis에 저장할 예정
    @Qualifier("stockInventory")
    public RedisTemplate<String, String>stockRedisTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());//다문자열이지만 이건리스트 셋,해시스,지셋이라는 태그를 가진다.
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("ssePubSub")//빈객체에 이름붙이기
    public RedisConnectionFactory ssePubSubConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        //redis pub/sub기능은 db에 값을 저장하는 기능이 아니므로, 특정db에 의존적이지 않음.
        return new LettuceConnectionFactory();
    }
    @Bean//이 빈을 주입받아 redis에 저장할 예정
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String>ssePubSubRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());//다문자열이지만 이건리스트 셋,해시스,지셋이라는 태그를 가진다.
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
//    redis 리스너(subscribe) 객체
//특정 채널을 Listen하다가 메시지 들어왔네?                     를 통해                      클래스의 메서드에서 메시지 publish?
//    RedisMessageListenerContainer -> MessageListenerAdapter -> MessageListener를 구현한
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory
    ,@Qualifier("ssePubSub") MessageListenerAdapter messageListenerAdapter){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);//이 레디스를 리슨하고싶어
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
        //들어가보면 Topic이라는 채널명, MessageListen를 메시지가 처리 구독할거야
        return container;
    }
//    redis에서 수신된 메시지를 처리하는 객체
    @Bean
    @Qualifier("ssePubSub")//메시지를 받아 onMessage에 위임하겠다.
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService){
//        채널로부터 수신되는 message처리를 SseAlarmService의 onMessage메서드로 위임
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }
}
