package com.example.notification.adapter.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "channel")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChannelEntity {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "channel_id", updatable = false, nullable = false) private UUID channelId;
    @Column(name = "channel_name", nullable = false) private String channelName;
    @OneToMany(mappedBy = "channel") @Builder.Default private List<AccountChannelEntity> accountChannels = new ArrayList<>();
    @OneToMany(mappedBy = "channel") @Builder.Default private List<ProfileChannelEntity> profileChannels = new ArrayList<>();
    @OneToMany(mappedBy = "channel") @Builder.Default private List<TemplateEntity> templates = new ArrayList<>();
}
