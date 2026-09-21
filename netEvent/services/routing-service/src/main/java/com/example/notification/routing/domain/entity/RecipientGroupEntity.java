package com.example.notification.routing.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipient_group")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "group_name", nullable = false, unique = true)
    private String groupName;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecipientGroupMemberEntity> groupMembers = new ArrayList<>();

    public void addGroupMember(RecipientGroupMemberEntity groupMember) {
        groupMembers.add(groupMember);
        groupMember.setGroup(this);
    }

    public void removeGroupMember(RecipientGroupMemberEntity groupMember) {
        groupMembers.remove(groupMember);
        groupMember.setGroup(null);
    }
}
