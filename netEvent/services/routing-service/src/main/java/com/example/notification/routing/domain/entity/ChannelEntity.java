package com.example.notification.routing.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "channel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "channel_id", updatable = false, nullable = false)
    private UUID channelId;

    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @OneToMany(mappedBy = "channel")
    @Builder.Default
    private List<RoutingTemplateEntity> routingTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "channel")
    @Builder.Default
    private List<NotificationTaskEntity> notificationTasks = new ArrayList<>();

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccountChannelEntity> accountChannels = new ArrayList<>();

    public void addRoutingTemplate(RoutingTemplateEntity template) {
        routingTemplates.add(template);
        template.setChannel(this);
    }

    public void removeRoutingTemplate(RoutingTemplateEntity template) {
        routingTemplates.remove(template);
        template.setChannel(null);
    }

    public void addAccountChannel(AccountChannelEntity accountChannel) {
        accountChannels.add(accountChannel);
        accountChannel.setChannel(this);
    }

    public void removeAccountChannel(AccountChannelEntity accountChannel) {
        accountChannels.remove(accountChannel);
        accountChannel.setChannel(null);
    }
}
