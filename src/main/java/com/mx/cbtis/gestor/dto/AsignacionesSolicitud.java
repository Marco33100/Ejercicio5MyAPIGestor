package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Entrada HTTP independiente de las entidades JPA; ignora campos fuera del contrato. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AsignacionesSolicitud(
        @JsonProperty("id_asignacion") Integer idAsignacion,
        @JsonProperty("id_usuario") Integer idUsuario,
        @JsonProperty("id_tarea") Integer idTarea,
        @JsonProperty("fecha_asignacion") String fechaAsignacion) {
}
