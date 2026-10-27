package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ControladoresEntradaNulaTest {
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

    @Test
    void asignacionNulaNoSeGuardaActualizaNiElimina() {
        assertAll(() -> assertEquals(0, asignacionesController.addAsignacion(null)),
                () -> assertEquals(0, asignacionesController.updateAsignacion(null)),
                () -> assertEquals(0, asignacionesController.deleteAsignacion(null)));
        verifyNoInteractions(asignaciones);
    }

    @Test
    void comentarioNuloNoSeGuardaActualizaNiElimina() {
        assertAll(() -> assertEquals(0, comentariosController.addComentario(null)),
                () -> assertEquals(0, comentariosController.updateComentario(null)),
                () -> assertEquals(0, comentariosController.deleteComentario(null)));
        verifyNoInteractions(comentarios);
    }

    @Test
    void proyectoNuloNoSeGuardaActualizaNiElimina() {
        assertAll(() -> assertEquals(0, proyectosController.addProyecto(null)),
                () -> assertEquals(0, proyectosController.updateProyecto(null)),
                () -> assertEquals(0, proyectosController.deleteProyecto(null)));
        verifyNoInteractions(proyectos);
    }

    @Test
    void tareaNulaNoSeGuardaActualizaNiElimina() {
        assertAll(() -> assertEquals(0, tareasController.addTareas(null)),
                () -> assertEquals(0, tareasController.updateTareas(null)),
                () -> assertEquals(0, tareasController.deleteTareas(null)));
        verifyNoInteractions(tareas);
    }

    @Test
    void usuarioNuloNoSeGuardaActualizaNiElimina() {
        assertAll(() -> assertEquals(0, usuarioController.addUsuario(null)),
                () -> assertEquals(0, usuarioController.updateUsuario(null)),
                () -> assertEquals(0, usuarioController.deleteUsuario(null)));
        verifyNoInteractions(usuarios);
    }
}
