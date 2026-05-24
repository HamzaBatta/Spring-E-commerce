# Asynchronous Queues (RabbitMQ)

## Goal
Move slow work (invoice generation) off the request thread so orders stay fast.

## Solution in this project
- Publish invoice jobs to RabbitMQ.
- A background consumer reads messages and generates invoices.

## Where it is implemented
- `src/main/java/com/codewithmosh/store/StoreApplication.java`
  - `@EnableRabbit`
- `src/main/java/com/codewithmosh/store/services/RabbitConfig.java`
  - Exchange, queue, routing key, and `RabbitTemplate` setup.
- `src/main/java/com/codewithmosh/store/services/InvoiceProducer.java`
  - Sends invoice messages.
- `src/main/java/com/codewithmosh/store/services/InvoiceConsumer.java`
  - `@RabbitListener` processes messages.

## How it works (short)
1. Order creation triggers invoice producer.
2. Producer publishes a message to the exchange.
3. RabbitMQ routes it to the invoice queue.
4. Consumer reads and generates the invoice.

## Why RabbitMQ fits this project
- Reliable delivery with queueing and retries.
- Decouples order latency from PDF generation.
- Easy to scale consumers separately.

## How to verify
- Run RabbitMQ locally.
- Create an order.
- Check logs for invoice processing and generated PDF output.

