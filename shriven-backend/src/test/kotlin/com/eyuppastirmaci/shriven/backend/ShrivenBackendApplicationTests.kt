package com.eyuppastirmaci.shriven.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
	// Resolve ${DB_*} placeholders in application.yaml with H2
	"DB_URL=jdbc:h2:mem:testdb",
	"DB_USERNAME=sa",
	"DB_PASSWORD=",
	"spring.datasource.driver-class-name=org.h2.Driver",
	// Let Hibernate manage the H2 schema
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
	// Resolve ${JWT_*} placeholders in application.yaml
	"JWT_SECRET=dGVzdC1qd3Qtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1vbmx5LTI1NmJpdHM=",
	"JWT_EXPIRATION=900000",
	"JWT_REFRESH_EXPIRATION=2592000000",
	// Prevent Kafka listeners from auto-starting (Kafka is not available in tests)
	"spring.kafka.listener.auto-startup=false"
])
class ShrivenBackendApplicationTests {

	@Test
	fun contextLoads() {
		// Verify Spring context loads with H2, no live Kafka/Redis connections
	}
}
