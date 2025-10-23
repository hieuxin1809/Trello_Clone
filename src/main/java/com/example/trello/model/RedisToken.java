package com.example.trello.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Data
@RedisHash("RedisHas")
@Builder
public class RedisToken {
    @Id
    private String jwtId;
    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiredTime;
}
