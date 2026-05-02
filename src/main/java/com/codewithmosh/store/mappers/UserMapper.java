package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.requests.UpdateUserRequest;
import com.codewithmosh.store.dtos.resources.UserResource;
import com.codewithmosh.store.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResource toResource(User user);
    void update(UpdateUserRequest request, @MappingTarget User user);
}
