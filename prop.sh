# ========================
# Application
# ========================
spring.application.name=sv-service-checker

# Server
server.port=8083
server.servlet.session.timeout=30m

# ========================
# Database (MySQL)
# ========================
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/sv_truck_fats?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password

# ========================
# JPA / Hibernate
# ========================
# ❗ Use 'update' only for dev. For production, consider 'validate' or 'none'.
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# ========================
# Thymeleaf
# ========================
spring.thymeleaf.cache=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# ========================
# JWT
# ========================
jwt.secret=0123456789ABCDEF0123456789ABCDEF
jwt.expiration=86400000

# ========================
# MVC
# ========================
spring.mvc.hiddenmethod.filter.enabled=true

# ========================
# Docker (disable if not using Docker)
# ========================
spring.docker.compose.enabled=false