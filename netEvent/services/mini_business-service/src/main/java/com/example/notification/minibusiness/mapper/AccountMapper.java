package com.example.notification.minibusiness.mapper;

import com.example.notification.minibusiness.domain.entity.AccountEntity;
import com.example.notification.minibusiness.dto.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    AccountResponse toResponse(AccountEntity entity);

    List<AccountResponse> toResponseList(List<AccountEntity> entities);
}
