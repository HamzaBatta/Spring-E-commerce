package com.codewithmosh.store.dtos.resources;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class StorageResource {
    private Long id;
    private String name;
    private String location;
    private List<StorageItemResource> items = new ArrayList<>();
}
