package com.invillia.acme.config

import io.github.jhipster.config.JHipsterProperties
import java.nio.charset.StandardCharsets
import javax.servlet.ServletContext
import javax.servlet.ServletException
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.MimeMappings
import org.springframework.boot.web.server.WebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
class WebConfigurer(
    private val env: Environment,
    private val jHipsterProperties: JHipsterProperties
) : ServletContextInitializer, WebServerFactoryCustomizer<WebServerFactory> {

    private val log = LoggerFactory.getLogger(javaClass)

    @Throws(ServletException::class)
    override fun onStartup(servletContext: ServletContext) {
        if (env.activeProfiles.isNotEmpty()) {
            log.info("Web application configuration, using profiles: {}", *env.activeProfiles as Array<*>)
        }
        log.info("Web application fully configured")
    }

    /**
     * Customize the Servlet engine: Mime types, the document root, the cache.
     */
    override fun customize(server: WebServerFactory) {
        setMimeMappings(server)
    }

    private fun setMimeMappings(server: WebServerFactory) {
        if (server is ConfigurableServletWebServerFactory) {
            val mappings = MimeMappings(MimeMappings.DEFAULT)
            // IE issue, see https://github.com/jhipster/generator-jhipster/pull/711
            mappings.add("html", "${MediaType.TEXT_HTML_VALUE};charset=${StandardCharsets.UTF_8.name().toLowerCase()}")
            // CloudFoundry issue, see https://github.com/cloudfoundry/gorouter/issues/64
            mappings.add("json", "${MediaType.TEXT_HTML_VALUE};charset=${StandardCharsets.UTF_8.name().toLowerCase()}")
            server.setMimeMappings(mappings)
        }
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = jHipsterProperties.cors
        if (config.allowedOrigins != null && config.allowedOrigins!!.isNotEmpty()) {
            log.debug("Registering CORS filter")
            source.apply {
                registerCorsConfiguration("/api/**", config)
                registerCorsConfiguration("/management/**", config)
                registerCorsConfiguration("/v2/api-docs", config)
            }
        }
        return CorsFilter(source)
    }
}
