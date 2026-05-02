package com.codewithmosh.store.dtos.resources;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserResource {
    private Long id;
    private String name;
    private String email;
}
