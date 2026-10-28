package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** El login solo acepta las credenciales, sin propiedades persistentes adicionales. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginSolicitud(
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("contrasena") String contrasena) {
}
