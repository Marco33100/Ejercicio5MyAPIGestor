package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Entrada HTTP independiente de las entidades JPA; ignora campos fuera del contrato. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ComentariosSolicitud(
        @JsonProperty("id_comentario") Integer idComentario,
        @JsonProperty("id_usuario") String idUsuario,
        @JsonProperty("id_tarea") Integer idTarea,
        @JsonProperty("texto_comentario") String textoComentario,
        @JsonProperty("fecha_hora") String fechaHora) {
}
