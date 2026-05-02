package com.codewithmosh.store.dtos.requests;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String email;
}
