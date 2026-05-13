package com.do_an.userservice.mapper;

import com.do_an.userservice.dto.request.RoleDTO;
import com.do_an.userservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.factory.Mappers;
@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    RoleDTO toDto(Role role);

    @Mapping(target = "userRoles", ignore = true)
    Role toEntity(RoleDTO roleDto);

}


