package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.resources.PaymentResource;
import com.codewithmosh.store.entities.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(target = "clientSecret", ignore = true)
    PaymentResource toResource(Payment payment);
}
