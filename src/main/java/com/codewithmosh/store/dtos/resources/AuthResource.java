package com.codewithmosh.store.dtos.resources;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResource {
    private String token;
    private Long id;
    private String name;
    private String email;
}
