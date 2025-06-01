package hm.streamtest.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web configuration class to handle CORS settings
 */
@Configuration
class WebConfig : WebMvcConfigurer {

    /**
     * Configure CORS mappings to allow cross-origin requests
     * @param registry The CORS registry
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600)
    }
}