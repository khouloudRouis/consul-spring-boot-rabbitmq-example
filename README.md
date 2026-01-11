# Consul Spring Boot Microservices Example

Status : WIP

A microservices architecture example built with Spring Boot, Consul for service discovery, RabbitMQ for messaging, and PostgreSQL for data persistence.



## 🏗️ Architecture Overview

This project demonstrates a microservices architecture consisting of three services:

```
┌──────────────┐
│ API Gateway  │ (Port 8082)
│   (Consul)   │
└──────┬───────┘
       │
       ├─────────────┐
       │             │
┌──────▼───────┐  ┌──▼────────────┐
│Order Service │  │Metrics Service│
│ (Port 8081)  │  │ (Port 8083)   │
│              │  │               │
│ PostgreSQL   │  │  RabbitMQ     │
│   Database   │  │   Listener    │
└──────┬───────┘  └───────────────┘
       │
       │ (Publishes events)
       │
┌──────▼────────┐
│   RabbitMQ    │
│  (Message     │
│   Broker)     │
└───────────────┘
```

### Services

1. **API Gateway** (`api-gateway`)
   - Port: 8082
   - Routes requests to backend services
   - Uses Spring Cloud Gateway with Consul service discovery
   - Load balancing enabled

2. **Order Service** (`order-service`)
   - Port: 8081
   - Manages order creation and retrieval
   - PostgreSQL database for persistence
   - Publishes order creation events to RabbitMQ
   - Registered with Consul for service discovery

3. **Metrics Service** (`metrics-service`)
   - Port: 8083
   - Listens to order creation events from RabbitMQ
   - Processes metrics related to orders
   - Registered with Consul for service discovery

## 🚀 Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **PostgreSQL 12+**
- **RabbitMQ 3.8+**
- **Consul 1.10+**

## 📦 Technologies Used

- **Spring Boot 3.4.1**
- **Spring Cloud 2024.0.0**
  - Spring Cloud Gateway
  - Spring Cloud Consul Discovery
  - Spring Cloud Load Balancer
- **Spring Data JPA** (with PostgreSQL)
- **RabbitMQ** (AMQP messaging)
- **Lombok**
- **Spring Actuator** (health checks and monitoring)

## 🔧 Setup Instructions

### 1. Start Infrastructure Services

#### Start Consul
```bash
# Download and install Consul from https://www.consul.io/downloads
consul agent -dev

# Or using Docker
docker run -d -p 8500:8500 consul:latest
```

Consul UI will be available at: http://localhost:8500

#### Start PostgreSQL
```bash
# Using Docker
docker run -d \
  --name postgres-orders \
  -e POSTGRES_DB=orders_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=admin \
  -p 5432:5432 \
  postgres:15-alpine

# Or install locally and create database
createdb orders_db
```

#### Start RabbitMQ
```bash
# Using Docker
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# Management UI: http://localhost:15672
# Default credentials: guest/guest
```

### 2. Build the Project

```bash
# Build all services
cd order-service
mvn clean install

cd ../metrics-service
mvn clean install

cd ../api-gateway
mvn clean install
```

### 3. Run the Services

#### Option 1: Run individually

```bash
# Terminal 1 - Order Service
cd order-service
mvn spring-boot:run

# Terminal 2 - Metrics Service
cd metrics-service
mvn spring-boot:run

# Terminal 3 - API Gateway
cd api-gateway
mvn spring-boot:run
```

#### Option 2: Using Docker Compose (Recommended)

```bash
# Create docker-compose.yml (see below)
docker-compose up -d
```

### 4. Verify Services are Running

- **Consul**: http://localhost:8500
  - Check that all services are registered: `order-service`, `metrics-service`, `api-gateway`
  
- **Order Service Health**: http://localhost:8081/actuator/health
- **Metrics Service Health**: http://localhost:8083/actuator/health
- **API Gateway Health**: http://localhost:8082/actuator/health

## 📡 API Endpoints

### Through API Gateway (Port 8082)

```
# Create an order
POST http://localhost:8082/orders
Content-Type: application/json

{
  "customerId": 1,
  "items": [
    {
      "productId": 101,
      "quantity": 2,
      "unitPrice": 29.99
    },
    {
      "productId": 102,
      "quantity": 1,
      "unitPrice": 15.50
    }
  ]
}

# Get all orders
GET http://localhost:8082/orders

# Get order by ID
GET http://localhost:8082/orders/{id}
```

### Direct Access to Order Service (Port 8081)

Same endpoints as above, but directly on port 8081.

## 📝 Configuration

### Order Service Configuration

Key properties in `order-service/src/main/resources/application.properties`:

```properties
server.port=8081
spring.application.name=order-service

# Consul
spring.cloud.consul.host=localhost
spring.cloud.consul.port=8500

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/orders_db
spring.datasource.username=postgres
spring.datasource.password=${DATABASE_PASSWORD:admin}

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
```

### Metrics Service Configuration

Key properties in `metrics-service/src/main/resources/application.properties`:

```properties
server.port=8083
spring.application.name=metrics-service

# Consul
spring.cloud.consul.host=localhost
spring.cloud.consul.port=8500

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
```

### API Gateway Configuration

Key properties in `api-gateway/src/main/resources/application.properties`:

```properties
server.port=8082
spring.application.name=api-gateway

# Consul
spring.cloud.consul.host=localhost
spring.cloud.consul.port=8500
spring.cloud.gateway.discovery.locator.enabled=true
```

## 🔄 Event Flow

1. Client sends order creation request to API Gateway
2. API Gateway routes request to Order Service (via Consul service discovery)
3. Order Service:
   - Validates and saves order to PostgreSQL
   - Publishes `OrderCreatedEvent` to RabbitMQ exchange
4. Metrics Service:
   - Listens to RabbitMQ queue
   - Receives `OrderCreatedEvent`
   - Processes metrics (currently logs the event)

## 📊 Database Schema

### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(19,2),
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Order Items Table
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL
);
```

## 🧪 Testing

### Manual Testing

```bash
# Create an order
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 101,
        "quantity": 2,
        "unitPrice": 29.99
      }
    ]
  }'

# Get all orders
curl http://localhost:8082/orders

# Get order by ID
curl http://localhost:8082/orders/1
```

### Unit Tests

```bash
# Run tests for each service
cd order-service && mvn test
cd metrics-service && mvn test
cd api-gateway && mvn test
```

## 🐳 Docker Compose (Optional)

Create a `docker-compose.yml` in the root directory:

```yaml
version: '3.8'

services:
  consul:
    image: consul:latest
    ports:
      - "8500:8500"
    command: consul agent -dev -client=0.0.0.0

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: orders_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: admin
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

volumes:
  postgres_data:
```

Run: `docker-compose up -d`

## 📚 Project Structure

```
consul-springboot-example/
├── api-gateway/
│   ├── src/main/java/com/gateway/
│   │   ├── ApiGatewayApplication.java
│   │   └── config/
│   │       └── GatewayConfig.java
│   └── pom.xml
├── order-service/
│   ├── src/main/java/com/orderservice/
│   │   ├── OrderServiceApplication.java
│   │   ├── api/
│   │   │   └── OrderController.java
│   │   ├── dto/
│   │   │   └── OrderRequest.java
│   │   ├── entity/
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   └── OrderStatus.java
│   │   ├── repository/
│   │   ├── service/
│   │   └── mq/
│   └── pom.xml
├── metrics-service/
│   ├── src/main/java/com/metricservice/
│   │   ├── MettricsServiceApplication.java
│   │   └── mq/
│   │       └── config/
│   └── pom.xml
└── README.md
```

## 🔍 Monitoring

- **Consul UI**: http://localhost:8500 (Service discovery)
- **RabbitMQ Management**: http://localhost:15672 (Message queue)
- **Spring Actuator Health**: `/actuator/health` on each service
- **Actuator Endpoints**: `/actuator/*` (configured in api-gateway)

## 🛠️ Development

### Adding a New Service

1. Create new Spring Boot project with Consul Discovery dependency
2. Configure `spring.application.name` and port
3. Register with Consul in `application.properties`
4. Add route in API Gateway if needed

### Environment Variables

```bash
# Database password
export DATABASE_PASSWORD=your_password

# Consul host (for Docker environments)
export CONSUL_HOST=host.docker.internal
```

## 🐛 Troubleshooting

### Services not registering with Consul
- Check Consul is running: `consul members`
- Verify `spring.cloud.consul.host` and `port` are correct
- Check firewall settings

### Database connection issues
- Verify PostgreSQL is running: `psql -U postgres -d orders_db`
- Check connection string in `application.properties`
- Ensure database exists

### RabbitMQ connection issues
- Verify RabbitMQ is running: `docker ps | grep rabbitmq`
- Check credentials in `application.properties`
- Access management UI at http://localhost:15672

## 📄 License

This project is a work in progress and is intended for educational purposes.

## 👤 Author

Khouloud

---

**Note**: This is the first commit of a work-in-progress project.

