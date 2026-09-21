package com.example.notification.adapter.application.mapper;

import com.example.notification.adapter.application.dto.request.ReportCreatedEvent;
import com.example.notification.adapter.application.dto.response.IngestionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventIngestionMapper {

    @Mapping(target = "status", constant = "ACCEPTED")
    IngestionResponse toIngestionResponse(ReportCreatedEvent event);
}
