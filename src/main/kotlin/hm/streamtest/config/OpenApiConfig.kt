package hm.streamtest.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for OpenAPI (Swagger) documentation
 */
@Configuration
class OpenApiConfig {

    /**
     * Bean to configure OpenAPI documentation
     * @return OpenAPI configuration
     */
    @Bean
    fun myOpenAPI(): OpenAPI {
        val devServer = Server().apply {
            url = "http://localhost:8080"
            description = "Server URL in Development environment"
        }

        val contact = Contact().apply {
            name = "Stream Test API"
            email = "contact@example.com"
            url = "https://www.example.com"
        }

        val mitLicense = License()
            .name("MIT License")
            .url("https://choosealicense.com/licenses/mit/")

        val info = Info()
            .title("Stream Test API Documentation")
            .version("1.0")
            .contact(contact)
            .description("This API exposes endpoints to manage processor data with different approaches.")
            .license(mitLicense)

        return OpenAPI()
            .info(info)
            .servers(listOf(devServer))
    }
}