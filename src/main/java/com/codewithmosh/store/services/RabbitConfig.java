package com.codewithmosh.store.services;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String INVOICE_QUEUE = "invoices";
    public static final String INVOICE_EXCHANGE = "invoices.exchange";
    public static final String INVOICE_ROUTING = "invoices";

    @Bean
    public Queue invoiceQueue() {
        return new Queue(INVOICE_QUEUE, true);
    }

    @Bean
    public TopicExchange invoiceExchange() {
        return new TopicExchange(INVOICE_EXCHANGE);
    }

    @Bean
    public Binding invoiceBinding(Queue invoiceQueue, TopicExchange invoiceExchange) {
        return BindingBuilder.bind(invoiceQueue).to(invoiceExchange).with(INVOICE_ROUTING);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
