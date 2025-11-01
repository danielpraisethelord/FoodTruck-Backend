package com.foodtruck.backend.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Food Truck API", version = "1.0", description = "API para la gestión de food trucks"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer", description = "Ingresa el token JWT en el formato: Bearer <token>")
public class OpenApiConfig {

    // ...existing code...

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("FoodTruck API")
                        .version("1.0")
                        .description(
                                """
                                        # FoodTruck Backend API

                                        API REST para la gestión completa de un foodtruck, incluyendo:
                                        - Autenticación y autorización con JWT
                                        - Gestión de usuarios (clientes, empleados, administradores)
                                        - Catálogo de productos y categorías
                                        - Gestión de promociones
                                        - Sistema completo de órdenes
                                        - **Notificaciones en tiempo real vía WebSocket**

                                        ## Notificaciones WebSocket

                                        La aplicación incluye notificaciones en tiempo real usando WebSocket con STOMP:
                                        - **Empleados** reciben notificaciones cuando usuarios crean/modifican órdenes
                                        - **Usuarios** reciben notificaciones cuando empleados cambian el estado o tiempo estimado de sus órdenes

                                        **Endpoint WebSocket:** `ws://localhost:8081/ws`

                                        Para más información sobre la conexión WebSocket, consulta el endpoint `/api/websocket/info`
                                        """)
                        .contact(new Contact()
                                .name("FoodTruck Team")
                                .email("support@foodtruck.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "JWT token de autenticación. Obtén el token desde /api/auth/login")));
    }

}