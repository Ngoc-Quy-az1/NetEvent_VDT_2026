package com.example.notification.minibusiness.mapper;

import com.example.notification.minibusiness.domain.entity.BusinessRuleEntity;
import com.example.notification.minibusiness.dto.BusinessRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessRuleMapper {

    BusinessRuleResponse toResponse(BusinessRuleEntity entity);

    List<BusinessRuleResponse> toResponseList(List<BusinessRuleEntity> entities);
}
