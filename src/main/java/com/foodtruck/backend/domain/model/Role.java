package com.foodtruck.backend.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Roles disponibles en el sistema.")
public enum Role {
    @Schema(description = "Rol básico asignado a usuarios estándar.")
    ROLE_USER,

    @Schema(description = "Rol con privilegios administrativos.")
    ROLE_ADMIN,

    @Schema(description = "Rol asignado a empleados con permisos limitados.")
    ROLE_EMPLOYEE
}
