package com.eyuppastirmaci.shriven.backend

import com.eyuppastirmaci.shriven.backend.properties.AppProperties
import com.eyuppastirmaci.shriven.backend.properties.CorsProperties
import com.eyuppastirmaci.shriven.backend.properties.Base62Properties
import com.eyuppastirmaci.shriven.backend.properties.CacheProperties
import com.eyuppastirmaci.shriven.backend.properties.SnowflakeProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
	SnowflakeProperties::class,
	Base62Properties::class,
	AppProperties::class,
	CorsProperties::class,
	CacheProperties::class
)
class ShrivenBackendApplication

fun main(args: Array<String>) {
	runApplication<ShrivenBackendApplication>(*args)
}
