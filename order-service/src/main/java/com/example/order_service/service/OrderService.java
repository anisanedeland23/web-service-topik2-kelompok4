package com.example.order_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order_service.dto.OrderItemResponse;
import com.example.order_service.dto.OrderRequest;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.event.OrderCreatedEvent;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("[CREATE ORDER] Menerima pesanan baru dari pelanggan: {}", request.getCustomerName());
        double total = request.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        Order order = orderMapper.toEntity(request);
        order.setTotalAmount(total);
        order.setStatus(Order.OrderStatus.PENDING);
        try {
            order.setItems(objectMapper.writeValueAsString(request.getItems()));
            log.info("[CREATE ORDER] Item pesanan sukses diproses menjadi format JSON");
        } catch (JsonProcessingException e) {
            log.error("[CREATE ORDER] Gagal memproses item pesanan: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize items", e);
        }

        order = orderRepository.save(order);
        log.info("[CREATE ORDER] Pesanan (ID: {}) berhasil disimpan ke database PostgreSQL dengan status PENDING", order.getId());

        OrderCreatedEvent event = orderMapper.toEvent(order, request);
        log.info("[CREATE ORDER] Memanggil OrderProducer untuk mengirim pesanan (ID: {}) ke RabbitMQ", order.getId());
        orderProducer.sendOrderCreatedEvent(event);

        List<OrderItemResponse> items = request.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProductId(), i.getProductName(), i.getQuantity(), i.getPrice()))
                .toList();
        return orderMapper.toResponse(order, items);
    }

    public List<OrderResponse> getAllOrders() {
        log.info("[GET_ALL] Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {
        log.info("[GET_BY_ID] Fetching order id={}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest request) {
        log.info("[UPDATE] Updating order id={}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        double total = request.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setTotalAmount(total);
        try {
            order.setItems(objectMapper.writeValueAsString(request.getItems()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize items", e);
        }

        order = orderRepository.save(order);
        log.info("[UPDATE] Order updated, id={}", order.getId());

        List<OrderItemResponse> items = request.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProductId(), i.getProductName(), i.getQuantity(), i.getPrice()))
                .toList();
        return orderMapper.toResponse(order, items);
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("[DELETE] Deleting order id={}", id);
        orderRepository.deleteById(id);
    }

    private OrderResponse mapToResponse(Order order) {
        try {
            List<OrderItemResponse> items = objectMapper.readValue(
                    order.getItems(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, OrderItemResponse.class));
            return orderMapper.toResponse(order, items);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize items", e);
        }
    }
}