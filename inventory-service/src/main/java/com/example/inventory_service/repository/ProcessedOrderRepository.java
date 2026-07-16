package com.example.inventory_service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.inventory_service.model.ProcessedOrder;

public interface ProcessedOrderRepository extends MongoRepository<ProcessedOrder, String> {
    Optional<ProcessedOrder> findByOrderId(Long orderId);
}