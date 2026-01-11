package com.metricservice.mq.config.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.metricservice.entity.Metric;
import com.metricservice.mq.config.model.OrderCreatedEvent;
import com.metricservice.repository.MetricsRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class MetricsListener {
	private static final Logger log = LoggerFactory.getLogger(MetricsListener.class);
	private final MetricsRepository repository;
	@Value("${metrics.routing-key}")
	private String routingKey;

	@RabbitListener(queues = "${metrics.queue}")
	public void handleOrderCreated(OrderCreatedEvent event) {
		log.info("{} metric received: amount={}, customerId={}, createdAt={}", event.label(), event.amount(),
				event.customerId(), event.createdAt());
		Metric metric = Metric.builder().amount(event.amount()).customerId(event.customerId()).orderId(event.orderId())
				.createdAt(event.createdAt()).event(event.label()).build();

		repository.save(metric);
	}
}
