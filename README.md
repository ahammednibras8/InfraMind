# InfraMind - The Self-Healing Backend Brain

[![Build Status](https://github.com/ahammednibras8/InfraMind/workflows/CI/badge.svg)](https://github.com/your-org/infra-mind/actions)
[![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)

> **A precision-engineered backend framework designed to eliminate complexity and maximize productivity.**

InfraMind is not just another backend framework—it's a comprehensive solution for building robust, observable, and secure backend systems with minimal configuration overhead.

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose V2
- Java 21+ (for local development)
- 2GB+ RAM recommended

### Local Deployment

```bash
# Clone the repository
git clone https://github.com/ahammednibras8/InfraMind.git
cd InfraMind

# Download OpenTelemetry Java Agent (if not present)
curl -L -o javaagent/opentelemetry-javaagent.jar \
  https://github.com/open-telemetry/opentelemetry-javaagent/releases/latest/download/opentelemetry-javaagent.jar

# Deploy the full stack
docker-compose up --build -d

# Wait for services to start (20-30 seconds)

# Verify deployment
curl http://localhost:8081/api/public/health
```

### Access Points
- **Application**: http://localhost:8081
- **API Documentation**: http://localhost:8081/swagger-ui.html
- **Jaeger Tracing**: http://localhost:16686
- **Prometheus Metrics**: http://localhost:9090

## 🏗️ Architecture

InfraMind is built on a modern, battle-tested stack:

```
InfraMind Stack
├── Spring Boot 3.x          # Core framework
├── Spring Security          # Authentication & authorization  
├── Spring Actuator          # Health checks & metrics
├── Docker + Compose         # Containerization
├── GitHub Actions           # CI/CD pipeline
├── Springdoc OpenAPI        # API documentation
├── Micrometer + Prometheus  # Metrics collection
├── OpenTelemetry            # Distributed tracing
├── OpenTelemetry Collector  # Data processing
└── Jaeger                   # Trace visualization
```

## 📡 API Endpoints

### Core Operations
- `GET /api/public/health` - Public health check
- `GET /api/v1/status` - System status (Admin only)
- `GET /api/admin/report` - Admin report (Admin only)
- `POST /api/user/data` - User data endpoint

### InfraMind Intelligence (Admin Only)
- `GET /infra/api-map` - Live API endpoint mapping
- `GET /infra/access-map` - RBAC visualization

### Observability & Management
- `GET /actuator/health` - Detailed health information
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Available metrics
- `GET /actuator/prometheus` - Prometheus-formatted metrics

## 🛠️ Development

### Local Development
```bash
# Run application directly
./mvnw spring-boot:run

# Run tests
./mvnw test

# Package application
./mvnw clean package

# Build Docker image
docker build -t inframind-app .
```

### Environment Configuration
The application supports multiple profiles and can be configured via environment variables or `application.yml`.

## 🔒 Security

InfraMind implements comprehensive security measures:

- **Spring Security** with role-based access control (RBAC)
- **Two-tier authentication**: ADMIN and USER roles
- **Endpoint-level authorization** using `@PreAuthorize`
- **Secure actuator endpoints** with appropriate access controls

Default credentials for development:
- Admin: `admin/admin123`
- User: `user/user123`

## 📊 Monitoring & Observability

### Metrics
- **JVM metrics**: Memory, threads, garbage collection
- **Custom metrics**: Application-specific measurements
- **Prometheus integration**: `/actuator/prometheus` endpoint

### Tracing
- **OpenTelemetry**: Automatic request tracing
- **Jaeger integration**: Visual trace analysis
- **Distributed tracing**: End-to-end request visibility

### Health Checks
- **Actuator health**: Comprehensive system health
- **Custom health indicators**: Application-specific checks
- **Dependency monitoring**: External service health

## 🚦 CI/CD Pipeline

The GitHub Actions workflow provides:

1. **Automated builds** on every push
2. **Docker image creation** and validation
3. **Full stack deployment testing**
4. **Health check verification**
5. **Metrics validation**
6. **Automatic cleanup**

Pipeline execution time: < 5 minutes

## 📋 System Requirements

### Development
- Java 21+
- Maven 3.8+
- Docker 20.0+
- Docker Compose V2

### Production
- 2GB+ RAM
- Ports: 8081, 9090, 16686, 4317, 4318

## 🎯 Performance Targets

- **Deployment time**: < 2 minutes from clone to running
- **API response time**: < 100ms for core endpoints
- **Pipeline execution**: < 5 minutes build-to-deploy
- **Documentation coverage**: 100% endpoint coverage

## 🔄 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-capability`
3. Commit changes: `git commit -m "feat: add new capability"`
4. Push to branch: `git push origin feature/new-capability`
5. Create a Pull Request

### Development Guidelines
- **MVP First**: Focus on core functionality
- **Pipeline Sacred**: Never break CI/CD
- **Documentation Mandatory**: Document all endpoints and configurations
- **Security Non-negotiable**: No shortcuts on authentication
- **Performance Baseline**: Maintain sub-100ms response times

## 📞 Support & Contact

- **Project Lead**: [@ahammednibras8](https://github.com/ahammednibras8)
- **Repository**: [InfraMind](https://github.com/ahammednibras8/InfraMind)
- **Issues**: [GitHub Issues](https://github.com/ahammednibras8/InfraMind/issues)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏆 Version

**InfraMind v0.1.0-SNAPSHOT** | Built for Backend Excellence

---

*"Precision over Perfection, Deployment over Discussion"*