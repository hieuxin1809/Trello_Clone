package com.example.trello.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class JwtInfo implements Serializable {
    private String jwtId;
    private Date issueTime;
    private String email;
    private Date expiredTime;
}
