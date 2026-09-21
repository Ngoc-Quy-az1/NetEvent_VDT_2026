package com.example.notification.routing.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "recipient_group_member")
@IdClass(RecipientGroupMemberId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientGroupMemberEntity {

    @Id
    @Column(name = "recipient_id")
    private UUID recipientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipientId")
    @JoinColumn(name = "recipient_id")
    private RecipientEntity recipient;

    @Id
    @Column(name = "group_id")
    private UUID groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private RecipientGroupEntity group;
}
