package com.example.notification.minibusiness.dto;
import lombok.*;
import java.util.Map;
import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileGroupResponse { private UUID groupId; private UUID channelId; private Map<String, Object> groupConfig; }
