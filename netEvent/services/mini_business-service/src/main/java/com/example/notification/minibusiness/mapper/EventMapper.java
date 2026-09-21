package com.example.notification.minibusiness.mapper;

import com.example.notification.minibusiness.domain.entity.EventEntity;
import com.example.notification.minibusiness.dto.EventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    EventResponse toResponse(EventEntity entity);

    List<EventResponse> toResponseList(List<EventEntity> entities);
}
