package com.metricservice.mq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;


/**
 * RabbitMQ configuration for order service.
 * Configures exchange, queue, binding, and JSON message converter.
 */

@Configuration
public class RabbitMQConfig {

    @Value("${metrics.exchange}")
    private String exchange;

    @Value("${metrics.queue}")
    private String queue;

    @Value("${metrics.routing-key}")
    private String routingKey;

    @Bean
    TopicExchange metricsExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    Queue metricsQueue() {
        return new Queue(queue, true);
    }

    @Bean
    Binding metricsBinding() {
        return BindingBuilder
                .bind(metricsQueue())
                .to(metricsExchange())
                .with(routingKey);
    }
    /**
     * Message converter for JSON serialization/deserialization.
     * Spring Boot will automatically use this for RabbitTemplate.
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
