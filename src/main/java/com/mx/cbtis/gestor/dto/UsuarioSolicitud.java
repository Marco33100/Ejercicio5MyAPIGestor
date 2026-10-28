package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Entrada HTTP independiente de las entidades JPA; ignora campos fuera del contrato. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioSolicitud(
        @JsonProperty("id_usuario") Integer idUsuario,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("contrasena") String contrasena,
        @JsonProperty("imagen_usuario") String imagenUsuario) {
}
