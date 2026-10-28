package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ContratoJsonApiTest {

    private static final String PROYECTO_JSON = """
        {"id_proyecto":12,"nombre":"Entrega Java 21","descripcion":"Migracion del gestor",
         "fecha_inicio":"2026-10-21","fecha_finalizacion":"2026-10-30",
         "estado_proyecto":"En progreso","fondo":"proyecto.png"}
        """;

    private static final String TAREA_JSON = """
        {"id_tarea":23,"nombre":"Compilar API","descripcion":"Verificar las pruebas",
         "fecha_vencimiento":"2026-10-25","lista":"Sprint 1","estado":"Pendiente",
         "id_proyecto":12}
        """;

    private static final String ASIGNACION_JSON = """
        {"id_asignacion":34,"id_usuario":45,"id_tarea":23,"fecha_asignacion":"2026-10-21"}
        """;

    private static final String COMENTARIO_JSON = """
        {"id_comentario":56,"id_usuario":"45","id_tarea":23,
         "texto_comentario":"Pruebas de la API listas","fecha_hora":"2026-10-21T17:00:00"}
        """;

    private static final String USUARIO_JSON = """
        {"id_usuario":45,"nombre":"Usuario de prueba","correo_electronico":"prueba@example.test",
         "contrasena":"valor-de-prueba","rol":"estudiante","imagen_usuario":"perfil.png"}
        """;

    @Mock private ProtectosRepository proyectos;
    @Mock private TareasRepository tareas;
    @Mock private AsignacionesRepository asignaciones;
    @Mock private ComentariosRepository comentarios;
    @Mock private UsuarioRepository usuarios;

    @InjectMocks private ProyectosController proyectosController;
    @InjectMocks private TareasController tareasController;
    @InjectMocks private AsignacionesController asignacionesController;
    @InjectMocks private ComentariosController comentariosController;
    @InjectMocks private UsuarioController usuarioController;

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;

    @BeforeEach
    void prepararApi() {
        mvc = MockMvcBuilders.standaloneSetup(proyectosController, tareasController,
                asignacionesController, comentariosController, usuarioController).build();
    }

    @Test
    void actualizarProyectoConservaTodosLosCamposEnviadosPorElCliente() throws Exception {
        comprobarActualizacion("/updateProyecto", PROYECTO_JSON, proyectos, Proyectos.class);
    }

    @Test
    void buscarProyectoDevuelveElContratoJsonCompleto() throws Exception {
        Proyectos proyecto = new Proyectos(12, "Entrega Java 21", "Migracion del gestor",
                "2026-10-21", "2026-10-30", "En progreso", "proyecto.png");
        when(proyectos.findById(12)).thenReturn(Optional.of(proyecto));

        mvc.perform(get("/buscarProyecto").param("id_proyecto", "12"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(PROYECTO_JSON, true));
        verify(proyectos).findById(12);
    }

    @Test
    void actualizarTareaConservaSuProyectoFechasYEstado() throws Exception {
        comprobarActualizacion("/updateTareas", TAREA_JSON, tareas, Tareas.class);
    }

    @Test
    void buscarTareaDevuelveElContratoJsonCompleto() throws Exception {
        Tareas tarea = new Tareas(23, "Compilar API", "Verificar las pruebas",
                "2026-10-25", "Sprint 1", "Pendiente", 12);
        when(tareas.findById(23)).thenReturn(Optional.of(tarea));

        mvc.perform(get("/buscarTarea").param("id_tarea", "23"))
                .andExpect(status().isOk())
                .andExpect(content().json(TAREA_JSON, true));
        verify(tareas).findById(23);
    }

    @Test
    void actualizarAsignacionConservaLasReferenciasAUsuarioYTarea() throws Exception {
        comprobarActualizacion("/updateAsignacion", ASIGNACION_JSON,
                asignaciones, Asignaciones.class);
    }

    @Test
    void buscarAsignacionDevuelveReferenciasYFecha() throws Exception {
        Asignaciones asignacion = new Asignaciones(34, 45, 23, "2026-10-21");
        when(asignaciones.findById(34)).thenReturn(Optional.of(asignacion));

        mvc.perform(get("/buscarAsignacion").param("id", "34"))
                .andExpect(status().isOk())
                .andExpect(content().json(ASIGNACION_JSON, true));
        verify(asignaciones).findById(34);
    }

    @Test
    void actualizarComentarioConservaTextoAutorTareaYFecha() throws Exception {
        comprobarActualizacion("/updateComentario", COMENTARIO_JSON,
                comentarios, Comentarios.class);
    }

    @Test
    void buscarComentarioDevuelveElContratoJsonCompleto() throws Exception {
        Comentarios comentario = new Comentarios(56, "45", 23,
                "Pruebas de la API listas", "2026-10-21T17:00:00");
        when(comentarios.findById(56)).thenReturn(Optional.of(comentario));

        mvc.perform(get("/buscarComentario").param("id", "56"))
                .andExpect(status().isOk())
                .andExpect(content().json(COMENTARIO_JSON, true));
        verify(comentarios).findById(56);
    }

    @Test
    void actualizarUsuarioConservaLosDatosDelPerfil() throws Exception {
        when(usuarios.findById(45)).thenReturn(Optional.of(new Usuario(45, "Anterior", "anterior@example.test",
                "anterior", "estudiante", "anterior.png")));
        comprobarActualizacion("/updateUsuario", USUARIO_JSON, usuarios, Usuario.class);
    }

    @Test
    void buscarUsuarioDevuelveLosDatosPublicosDelPerfil() throws Exception {
        Usuario usuario = new Usuario(45, "Usuario de prueba", "prueba@example.test",
                "valor-de-prueba", "estudiante", "perfil.png");
        when(usuarios.findById(45)).thenReturn(Optional.of(usuario));

        // Este contrato comprueba los campos del perfil sin exigir la contrasena en la respuesta.
        String perfil = """
            {"id_usuario":45,"nombre":"Usuario de prueba","correo_electronico":"prueba@example.test",
             "rol":"estudiante","imagen_usuario":"perfil.png"}
            """;
        mvc.perform(get("/buscarUsuario").param("id", "45"))
                .andExpect(status().isOk())
                .andExpect(content().json(perfil, false));
        verify(usuarios).findById(45);
    }

    private <T> void comprobarActualizacion(String endpoint, String json,
            CrudRepository<T, Integer> repository, Class<T> tipo) throws Exception {
        mvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        ArgumentCaptor<T> registro = ArgumentCaptor.forClass(tipo);
        verify(repository).save(registro.capture());
        // Verifica los datos entregados al repositorio, no solamente la respuesta de exito.
        assertEquals(mapper.readTree(json), mapper.valueToTree(registro.getValue()));
    }
}
