# Transaction Service

这是一个基于Spring Boot的微服务项目，用于处理银行交易相关业务。

## 项目依赖说明

### Spring Boot Starters
- `spring-boot-starter-web`: 提供Web应用开发所需的核心功能，包括RESTful API支持、Spring MVC等
- `spring-boot-starter-data-jpa`: 提供JPA（Java Persistence API）支持，用于数据库操作
- `spring-boot-starter-validation`: 提供数据验证功能，支持Bean Validation规范

### 开发工具
- `lombok`: 通过注解简化Java代码，减少样板代码的编写

### 测试相关
- `spring-boot-starter-test`: 提供Spring Boot应用测试所需的工具和框架
- `h2`: 内存数据库，用于开发和测试环境

### API文档
- `springdoc-openapi-starter-webmvc-ui`: 提供Swagger UI界面，用于API文档的自动生成和可视化

### 监控
- `spring-boot-starter-actuator`: 提供应用监控和管理端点，支持健康检查、指标收集等功能

## 技术栈
- Java 21
- Spring Boot 3.3.4
- Spring Data JPA
- H2 Database
- OpenAPI 3.0

# 构建Docker镜像
docker build -t transaction:latest .