package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.cbtis.gestor.modelo.*;
import com.mx.cbtis.gestor.repository.*;

@ExtendWith(MockitoExtension.class)
class SeguridadEntradasApiTest {
    @Mock private AsignacionesRepository asignaciones;
    @Mock private ComentariosRepository comentarios;
    @Mock private ProtectosRepository proyectos;
    @Mock private TareasRepository tareas;
    @Mock private UsuarioRepository usuarios;
    @InjectMocks private AsignacionesController asignacionesController;
    @InjectMocks private ComentariosController comentariosController;
    @InjectMocks private ProyectosController proyectosController;
    @InjectMocks private TareasController tareasController;
    @InjectMocks private UsuarioController usuarioController;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void prepararApi() {
        mvc = MockMvcBuilders.standaloneSetup(asignacionesController, comentariosController,
                proyectosController, tareasController, usuarioController).build();
    }

    static Stream<Arguments> altas() {
        return Stream.of(
            Arguments.of("/addAsignacion", "id_asignacion", """
                {"id_asignacion":999,"id_usuario":4,"id_tarea":3,"fecha_asignacion":"2026-10-28"}
                """, Asignaciones.class),
            Arguments.of("/addComentario", "id_comentario", """
                {"id_comentario":999,"id_usuario":"Ana","id_tarea":3,
                 "texto_comentario":"Entrega lista","fecha_hora":"2026-10-28T13:00"}
                """, Comentarios.class),
            Arguments.of("/addProyecto", "id_proyecto", """
                {"id_proyecto":999,"nombre":"Proyecto","descripcion":"Descripcion",
                 "fecha_inicio":"2026-10-28","fecha_finalizacion":"2026-11-01",
                 "estado_proyecto":"En progreso","fondo":"azul"}
                """, Proyectos.class),
            Arguments.of("/addTareas", "id_tarea", """
                {"id_tarea":999,"nombre":"Tarea","descripcion":"Descripcion",
                 "fecha_vencimiento":"2026-11-01","lista":"Sprint","estado":"Pendiente","id_proyecto":2}
                """, Tareas.class),
            Arguments.of("/addUsuario", "id_usuario", """
                {"id_usuario":999,"nombre":"Ana","correo_electronico":"ana@example.test",
                 "contrasena":"valor-prueba","rol":"Administrador","imagen_usuario":"perfil.png"}
                """, Usuario.class)
        );
    }

    @ParameterizedTest
    @MethodSource("altas")
    <T> void modelosConservanElContratoJsonDeRespuesta(String endpoint, String campoId,
            String json, Class<T> tipo) throws Exception {
        T modelo = mapper.readValue(json, tipo);
        assertEquals(mapper.readTree(json), mapper.valueToTree(modelo));
    }

    @ParameterizedTest
    @MethodSource("altas")
    <T> void altaIgnoraIdImpuestoYCopiaSoloCamposPermitidos(String endpoint, String campoId,
            String json, Class<T> tipo) throws Exception {
        mvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content().string("1"));
        var esperado = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(json);
        esperado.putNull(campoId);
        if (tipo == Usuario.class) esperado.put("rol", "Estudiante");
        ArgumentCaptor<T> captor = ArgumentCaptor.forClass(tipo);
        verify(repository(tipo)).save(captor.capture());
        assertEquals(esperado, mapper.valueToTree(captor.getValue()));
    }

    @SuppressWarnings("unchecked")
    private <T> CrudRepository<T, Integer> repository(Class<T> tipo) {
        if (tipo == Asignaciones.class) return (CrudRepository<T, Integer>) asignaciones;
        if (tipo == Comentarios.class) return (CrudRepository<T, Integer>) comentarios;
        if (tipo == Proyectos.class) return (CrudRepository<T, Integer>) proyectos;
        if (tipo == Tareas.class) return (CrudRepository<T, Integer>) tareas;
        return (CrudRepository<T, Integer>) usuarios;
    }

    @ParameterizedTest
    @MethodSource("altas")
    <T> void borrarSoloUsaElIdAunqueSeEnvienOtrosCampos(String endpoint, String campoId,
            String json, Class<T> tipo) throws Exception {
        mvc.perform(delete(endpoint.replace("/add", "/delete"))
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(content().string("1"));
        verify(repository(tipo)).deleteById(999);
        verifyNoMoreInteractions(repository(tipo));
    }

    @Test
    void actualizarPerfilConservaRolPersistidoAunqueElClienteEnvieOtro() throws Exception {
        when(usuarios.findById(7)).thenReturn(Optional.of(
                new Usuario(7, "Antes", "antes@example.test", "anterior", "Docente", "antes.png")));
        mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON).content("""
            {"id_usuario":7,"nombre":"Ana","correo_electronico":"ana@example.test",
             "contrasena":"nueva","rol":"Administrador","imagen_usuario":"perfil.png"}
            """))
                .andExpect(status().isOk()).andExpect(content().string("1"));
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertEquals("Docente", captor.getValue().getRol());
        assertEquals("Ana", captor.getValue().getNombre());
        assertEquals("nueva", captor.getValue().getContrasena());
        assertEquals(7, captor.getValue().getId_usuario());
    }

    @Test
    void actualizarUsuarioInexistenteNoCreaUnaCuenta() throws Exception {
        when(usuarios.findById(404)).thenReturn(Optional.empty());
        mvc.perform(post("/updateUsuario").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_usuario\":404,\"rol\":\"Administrador\"}"))
                .andExpect(status().isOk()).andExpect(content().string("0"));
        verify(usuarios, never()).save(any());
    }

    @Test
    void filtrosSoloUsanLaReferenciaSolicitada() throws Exception {
        when(comentarios.findByTarea(3)).thenReturn(Collections.emptyList());
        when(tareas.findByIdProyecto(2)).thenReturn(Collections.emptyList());
        mvc.perform(post("/getComentario").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_tarea\":3,\"texto_comentario\":\"No guardar\"}"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(post("/getTarea").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id_proyecto\":2,\"estado\":\"No guardar\"}"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(comentarios).findByTarea(3);
        verify(tareas).findByIdProyecto(2);
        verifyNoMoreInteractions(comentarios, tareas);
    }

    @Test
    void loginSoloConsultaPorCredencialesAunqueSeEnvieUnRol() throws Exception {
        when(usuarios.findByCorreoPass("ana@example.test", "clave"))
                .thenReturn(Optional.of(new Usuario(7, "Ana", "ana@example.test", "clave", "Estudiante", null)));
        mvc.perform(post("/getUsuarioLogin").contentType(MediaType.APPLICATION_JSON).content("""
            {"correo_electronico":"ana@example.test","contrasena":"clave",
             "rol":"Administrador","id_usuario":999}
            """))
                .andExpect(status().isOk());
        verify(usuarios).findByCorreoPass("ana@example.test", "clave");
        verifyNoMoreInteractions(usuarios);
    }
}
