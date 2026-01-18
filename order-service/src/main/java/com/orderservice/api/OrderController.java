package com.orderservice.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orderservice.dto.OrderRequest;
import com.orderservice.dto.SearchRequest;
import com.orderservice.entity.Order;
import com.orderservice.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	/**
	 * Create a new order
	 * 
	 * @param request Order request containing customer ID and items
	 * @return Created order
	 */
	@PostMapping
	public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
		Order order = orderService.createOrder(request.customerId(), request.items());
		return ResponseEntity.ok(order);
	}

	/**
	 * Get all orders
	 * 
	 * @return List of all orders
	 */
	@GetMapping
	public ResponseEntity<List<Order>> getOrders() {
		return ResponseEntity.ok(orderService.getAllOrders());
	}

	/**
	 * Get order by ID
	 * 
	 * @param id Order ID
	 * @return Order with the specified ID
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrder(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.getOrderById(id));
	}

	/**
	 * Searches for orders based on dynamic criteria.
	 * 
	 * @param searchRequest object holding search filters and conditions
	 * @return List of orders
	 */
	@PostMapping("/search")
	public ResponseEntity<List<Order>> getOrder(@RequestBody SearchRequest searchRequest) {
		return ResponseEntity.ok(orderService.search(searchRequest));
	}
}
