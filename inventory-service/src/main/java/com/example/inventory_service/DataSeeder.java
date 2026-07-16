package com.example.inventory_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) {
        if (inventoryRepository.count() == 0) {
            log.info("🌱 Seeding initial inventory data...");

            inventoryRepository.save(new Inventory(null, "P001", "Laptop", 50, 0));
            inventoryRepository.save(new Inventory(null, "P002", "Mouse", 100, 0));
            inventoryRepository.save(new Inventory(null, "P003", "Keyboard", 30, 0));
            inventoryRepository.save(new Inventory(null, "P004", "Monitor", 20, 0));

            log.info("Inventory data seeded successfully!");
        } else {
            log.info("Inventory data already exists, skipping seed.");
        }
    }
}