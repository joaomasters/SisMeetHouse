# 🥩 Açougue ERP — Full-Stack Point-of-Sale System

> A production-grade ERP and PDV (Point of Sale) system built for butcher shops — handling sales, inventory, financial reports, async Kafka messaging, and digital payments.

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue?logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-Upstash-black?logo=apachekafka)](https://upstash.com/kafka)
[![Tests](https://img.shields.io/badge/Tests-97%20passing-success?logo=junit5)](/)
[![Railway](https://img.shields.io/badge/Deploy-Railway-purple?logo=railway)](https://railway.app/)
[![Vercel](https://img.shields.io/badge/Frontend-Vercel-black?logo=vercel)](https://vercel.com/)

---

## 📸 Overview

A full-featured ERP system purpose-built for butcher shops, with an embedded PDV (cashier), barcode/scale integration, event-driven stock alerting via Apache Kafka, and PIX digital payments through the Mercado Pago API.

The backend and frontend are deployed independently — Railway hosts the Spring Boot API, while Vercel serves the React SPA.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (Vercel)                        │
│               React 18 · TypeScript · Vite · TailwindCSS        │
└──────────────────────────────┬──────────────────────────────────┘
                               │ REST / JSON
┌──────────────────────────────▼──────────────────────────────────┐
│                     BACKEND (Railway)                            │
│              Spring Boot 3 · Java 17 · Spring Security           │
│                                                                  │
│  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌────────────────┐   │
│  │   PDV   │  │ Estoque  │  │Financial │  │   Messaging    │   │
│  │ module  │  │ module   │  │  module  │  │    module      │   │
│  └────┬────┘  └────┬─────┘  └────┬─────┘  └───────┬────────┘   │
│       │            │             │                 │            │
│  ┌────▼────────────▼─────────────▼─────────────────▼────────┐  │
│  │                Spring ApplicationEventPublisher            │  │
│  │         @TransactionalEventListener(AFTER_COMMIT)          │  │
│  └────────────────────────────┬──────────────────────────────┘  │
│                               │                                  │
│  ┌────────────────────────────▼──────────────────────────────┐  │
│  │                     PostgreSQL (Flyway V1→V7)              │  │
│  └───────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────┘
                               │ SASL_SSL / SCRAM-SHA-256
┌──────────────────────────────▼──────────────────────────────────┐
│                    Upstash Kafka (managed)                        │
│                                                                  │
│  acougue.vendas.fechadas  →  EstoqueConsumer                     │
│  acougue.pix.confirmados  →  PixConsumer                         │
│  acougue.estoque.alertas  →  AlertaEstoqueConsumer               │
└─────────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

| Decision | Approach | Why |
|---|---|---|
| **Dual-write prevention** | `@TransactionalEventListener(AFTER_COMMIT)` | Kafka publish only after DB commits — no inconsistency |
| **Optional Kafka** | `@ConditionalOnProperty(kafka.enabled)` | App starts fine locally without any broker |
| **PIX payments** | Mercado Pago API + polling | Native Brazilian payment standard |
| **Scale barcodes** | EAN-2 prefix parser | Supports weight-embedded barcodes from meat scales |
| **Schema evolution** | Flyway migrations | Versioned, reproducible DB state |

---

## ✨ Features

### 🛒 PDV — Point of Sale
- Open/close cash register sessions with operator tracking
- Add items by EAN-13 barcode or by scale barcode (EAN-2 weight-embedded)
- Apply discounts per item or per sale
- Multi-payment support: cash, credit/debit card, Stone TEF, PIX, and FIADO (store credit)
- Automatic change (troco) calculation
- Real-time PIX payment status polling via Mercado Pago

### 📦 Inventory (Estoque)
- Real-time stock tracking per product (Entrada / Saída / Ajuste)
- Minimum stock configuration with automatic alerts
- Butchery (Desossa) module: break down primal cuts into retail pieces
- Complete audit trail for every stock movement

### 💰 Financial
- **DRE** (Income Statement): Revenue, CMV, Gross Profit, margins
- **Sales Report**: daily breakdown, by payment method, top products
- **Loss Report**: expired/damaged goods tracking with financial impact
- **Accounts Payable**: supplier invoice management with due-date tracking
- **Cash Flow**: daily inflows vs outflows, consolidated balance

### ⚡ Async Messaging (Apache Kafka)
- `VendaFechadaEvent` published after every successful sale
- `EstoqueConsumer` checks each sold item's stock vs minimum — triggers `AlertaEstoqueEvent` when below threshold
- `PixConsumer` logs PIX confirmations — extensible to WebSocket push
- `AlertaEstoqueConsumer` logs stock alerts — extensible to email / mobile push

### 🔒 Security
- JWT authentication (JJWT 0.11.5)
- Role-based access control (ADMIN, OPERADOR, GERENTE)
- Spring Security filter chain with stateless sessions

---

## 🧪 Tests

**97 unit tests — 0 failures**

| Module | Tests | Coverage |
|---|---|---|
| PDV Service | 15 | Caixa lifecycle, venda flow, barcode, stock decrease |
| Estoque Service | 13 | Entrada/Saída/Ajuste, concurrent stock |
| Desossa Service | 13 | Primal cut breakdown, weight validation |
| EAN Barcode Parser | 14 | Weight-embedded and standard barcodes |
| Relatorio Service | 19 | DRE, sales report, loss, stock, cash flow |
| DRE Service | 10 | Revenue aggregation, margin calculation |
| VendaEventProducer | 3 | Kafka topic routing, message key |
| PixEventProducer | 2 | PIX event key = mpPaymentId |
| EstoqueConsumer | 8 | Alert threshold logic, edge cases |

```bash
cd backend && mvn test
# [INFO] Tests run: 97, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

---

## 🛠️ Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JJWT 0.11.5 |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL 16 |
| Migrations | Flyway (V1 → V7) |
| Messaging | Apache Kafka via Upstash (SASL_SSL) |
| HTTP Client | Spring WebClient (Mercado Pago) |
| Tests | JUnit 5 + Mockito + AssertJ |
| Boilerplate | Lombok |

### Frontend
| Layer | Technology |
|---|---|
| Language | TypeScript 5 |
| Framework | React 18 |
| Build | Vite |
| Styling | TailwindCSS |
| HTTP | Axios |
| State | React Context |

### Infrastructure
| Service | Platform |
|---|---|
| Backend deploy | Railway |
| Frontend deploy | Vercel |
| Database | Railway PostgreSQL |
| Message broker | Upstash Kafka |
| PIX Payments | Mercado Pago API |

---

## 🚀 Running Locally

### Prerequisites
- Java 17+
- Node 18+
- PostgreSQL 14+
- Maven 3.9+

### Backend
```bash
cd backend

# Copy and configure environment variables
cp src/main/resources/application.yml.example src/main/resources/application.yml
# Set: DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET

# Kafka is DISABLED by default — no broker needed locally
# kafka.enabled=false (default)

mvn spring-boot:run
# API available at http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# App available at http://localhost:5173
```

---

## ⚙️ Environment Variables (Railway)

### Required
```env
DB_URL=jdbc:postgresql://<host>/<db>
DB_USERNAME=<user>
DB_PASSWORD=<password>
JWT_SECRET=<256-bit-base64-secret>
```

### Kafka (Upstash — optional)
```env
KAFKA_ENABLED=true
KAFKA_BOOTSTRAP_SERVERS=<broker>.upstash.io:9092
KAFKA_SASL_USERNAME=<username>
KAFKA_SASL_PASSWORD=<password>
KAFKA_SECURITY_PROTOCOL=SASL_SSL
```

### Payments (Mercado Pago)
```env
MP_ACCESS_TOKEN=<mercado-pago-access-token>
```

---

## 📡 Kafka Demo Endpoints

When `KAFKA_ENABLED=true`, the following demo endpoints are available for showcasing the event pipeline without real transactions:

```bash
# Simulate a complete sale → triggers stock alerts for Picanha and Contrafilé
POST /api/demo/kafka/venda-completa

# Simulate a PIX payment confirmation
POST /api/demo/kafka/pix-confirmado?vendaId=42&valor=150.00

# Simulate a direct stock alert
POST /api/demo/kafka/alerta-estoque?produto=Picanha&estoqueAtual=0.5&estoqueMinimo=5.0

# List configured Kafka topics and consumer groups
GET  /api/demo/kafka/topicos
```

---

## 📁 Project Structure

```
SisCondo/
├── backend/
│   ├── src/main/java/com/acougue/
│   │   ├── config/          # KafkaConfig, SecurityConfig, KafkaTopicConfig
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   ├── exception/       # BusinessException, GlobalExceptionHandler
│   │   └── modules/
│   │       ├── pdv/         # PDV, Caixa, PIX, Pagamento
│   │       ├── estoque/     # Estoque, Desossa
│   │       ├── financeiro/  # DRE, Relatorios, ContasAPagar
│   │       ├── messaging/   # Kafka events, producers, consumers
│   │       └── demo/        # KafkaDemoController (kafka.enabled=true)
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/    # Flyway V1 → V7
│   └── src/test/            # 97 unit tests
└── frontend/
    └── src/
        ├── modules/         # pdv, estoque, financeiro, desossa
        └── shared/          # components, hooks, auth
```

---

## 👤 Author

**João Gabriel Pereira**  
Full-Stack Developer · Java / Spring Boot · React / TypeScript  
[GitHub](https://github.com/joaomasters) · [LinkedIn](https://linkedin.com/in/)

---

*Built as a real-world ERP solution for the Brazilian butcher shop industry.*
