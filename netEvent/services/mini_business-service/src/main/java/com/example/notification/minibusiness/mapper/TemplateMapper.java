package com.example.notification.minibusiness.mapper;

import com.example.notification.minibusiness.domain.entity.TemplateEntity;
import com.example.notification.minibusiness.dto.ConfigureTemplateRequest;
import com.example.notification.minibusiness.dto.TemplateResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    @Mapping(target = "config", source = "configJson")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TemplateEntity toEntity(ConfigureTemplateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "config", source = "configJson")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget TemplateEntity entity, ConfigureTemplateRequest request);

    @Mapping(target = "channelName", source = "channel.channelName")
    TemplateResponse toResponse(TemplateEntity entity);

    List<TemplateResponse> toResponseList(List<TemplateEntity> entities);
}
