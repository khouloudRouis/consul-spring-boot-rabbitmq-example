package com.orderservice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.orderservice.dto.OrderItemRequest;
import com.orderservice.entity.*;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.mq.model.OrderCreatedEvent;
import com.orderservice.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

	@Value("${metrics.exchange}")
	private String exchange;

	@Value("${metrics.routing-key}")
	private String routingKey;

	private final OrderRepository orderRepository;
	private final RabbitTemplate rabbitTemplate;

	@Transactional
	public Order createOrder(Long customerId, List<OrderItemRequest> itemRequests) {
		log.info("Creating order for customerId={} with {} items", customerId, itemRequests.size());

		// Convert DTOs to entities
		List<OrderItem> items = itemRequests.stream().map(itemRequest -> {
			OrderItem item = new OrderItem();
			item.setProductId(itemRequest.productId());
			item.setQuantity(itemRequest.quantity());
			item.setUnitPrice(itemRequest.unitPrice());
			return item;
		}).collect(Collectors.toList());

		// Calculate total amount
		BigDecimal totalAmount = items.stream()
				.map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// Build and save order first
		Order order = Order.builder().customerId(customerId).totalAmount(totalAmount).status(OrderStatus.PENDING)
				.build();

		items.forEach(item -> item.setOrder(order));
		order.setItems(items);

		Order savedOrder = orderRepository.save(order);
		log.info("Order saved successfully with id: {}", savedOrder.getId());

		// Send message to RabbitMQ AFTER successful save
		OrderCreatedEvent event = new OrderCreatedEvent("Order Created", savedOrder.getId(),
				savedOrder.getTotalAmount(), savedOrder.getCustomerId(), savedOrder.getCreatedAt());

		try {
			rabbitTemplate.convertAndSend(exchange, routingKey, event);
			log.info("Order created event sent to RabbitMQ for orderId: {}", savedOrder.getId());
		} catch (Exception ex) {
			log.error("Failed to send order created event to RabbitMQ for orderId: {}. Order is already saved.",
					savedOrder.getId(), ex);
		}

		return savedOrder;
	}

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	public Order getOrderById(Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
	}
}
