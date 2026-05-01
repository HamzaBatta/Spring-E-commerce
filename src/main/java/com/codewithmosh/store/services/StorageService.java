package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.StorageDto;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.mappers.StorageMapper;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.repositories.StorageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StorageService {
    private StorageRepository storageRepository;
    private StorageMapper storageMapper;
    private ProductRepository productRepository;

    public List<StorageDto> getAllStorages(Long productId) {
        var storages = (productId != null)
                ? storageRepository.findByProductId(productId)
                : storageRepository.findAllWithProduct();
        return storages.stream()
                .map(storageMapper::toDto)
                .toList();
    }

    public StorageDto getStorage(Long id) {
        var storage = storageRepository.findById(id).orElse(null);
        if (storage == null) {
            throw new StorageNotFoundException();
        }
        return storageMapper.toDto(storage);
    }

    public StorageDto createStorage(StorageDto storageDto) {
        var product = productRepository.findById(storageDto.getProductId()).orElse(null);
        if (product == null) {
            throw new ProductNotFoundException();
        }
        var storage = storageMapper.toEntity(storageDto);
        storage.setProduct(product);
        storageRepository.save(storage);
        return storageMapper.toDto(storage);
    }

    public StorageDto updateStorage(Long id, StorageDto storageDto) {
        var storage = storageRepository.findById(id).orElse(null);
        if (storage == null) {
            throw new StorageNotFoundException();
        }
        var product = productRepository.findById(storageDto.getProductId()).orElse(null);
        if (product == null) {
            throw new ProductNotFoundException();
        }
        storageMapper.update(storageDto, storage);
        storage.setProduct(product);
        storageRepository.save(storage);
        return storageMapper.toDto(storage);
    }

    public void deleteStorage(Long id) {
        var storage = storageRepository.findById(id).orElse(null);
        if (storage == null) {
            throw new StorageNotFoundException();
        }
        storageRepository.delete(storage);
    }
}
