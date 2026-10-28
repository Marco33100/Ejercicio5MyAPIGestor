package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Entrada HTTP independiente de las entidades JPA; ignora campos fuera del contrato. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProyectosSolicitud(
        @JsonProperty("id_proyecto") Integer idProyecto,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("descripcion") String descripcion,
        @JsonProperty("fecha_inicio") String fechaInicio,
        @JsonProperty("fecha_finalizacion") String fechaFinalizacion,
        @JsonProperty("estado_proyecto") String estadoProyecto,
        @JsonProperty("fondo") String fondo) {
}
