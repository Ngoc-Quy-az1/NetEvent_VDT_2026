package com.example.notification.routing.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientGroupMemberId implements Serializable {
    private UUID recipientId;
    private UUID groupId;
}
