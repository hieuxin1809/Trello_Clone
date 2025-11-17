package com.example.trello.dto.response;

import com.example.trello.enums.WebSocketUpdateType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketUpdateResponse {
    private WebSocketUpdateType type;
    private Object payload;
}
