package com.example.notification.minibusiness.domain.entity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*; import org.hibernate.annotations.Type; import org.hibernate.annotations.TypeDef;
import javax.persistence.*; import java.io.Serializable; import java.util.Map; import java.util.UUID;
@Entity @Table(name="profile_group") @IdClass(ProfileGroupEntity.Id.class) @TypeDef(name="jsonb", typeClass=JsonBinaryType.class) @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileGroupEntity {
 @javax.persistence.Id @Column(name="profile_id") private UUID profileId;
 @javax.persistence.Id @Column(name="group_id") private UUID groupId;
 @javax.persistence.Id @Column(name="channel_id") private UUID channelId;
 @Type(type="jsonb") @Column(name="group_config",columnDefinition="jsonb") private Map<String,Object> groupConfig;
 @Data @NoArgsConstructor @AllArgsConstructor public static class Id implements Serializable { private UUID profileId,groupId,channelId; }
}
