package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Entrada HTTP independiente de las entidades JPA; ignora campos fuera del contrato. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProyectosId(
        @JsonProperty("id_proyecto") Integer idProyecto) {
}
