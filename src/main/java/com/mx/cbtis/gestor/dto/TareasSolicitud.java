package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Entrada HTTP independiente de las entidades JPA; ignora campos fuera del contrato. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TareasSolicitud(
        @JsonProperty("id_tarea") Integer idTarea,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("descripcion") String descripcion,
        @JsonProperty("fecha_vencimiento") String fechaVencimiento,
        @JsonProperty("lista") String lista,
        @JsonProperty("estado") String estado,
        @JsonProperty("id_proyecto") Integer idProyecto) {
}
