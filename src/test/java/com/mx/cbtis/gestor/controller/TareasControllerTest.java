package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.refEq;
import static com.mx.cbtis.gestor.controller.SolicitudesFixture.convertir;
import com.mx.cbtis.gestor.dto.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

@ExtendWith(MockitoExtension.class)
class TareasControllerTest {

	@Mock
	private TareasRepository tareastable;

	@InjectMocks
	private TareasController controller;

	@Test
	@DisplayName("UT-15 - Obtener tareas de un proyecto devuelve sus tareas")
	void obtenerTareasDeProyectoDevuelveSusTareas() {
		Tareas filtro = new Tareas();
		filtro.setId_proyecto(20);
		Tareas tarea = new Tareas();
		tarea.setId_tarea(3);
		tarea.setId_proyecto(20);
		when(tareastable.findByIdProyecto(20)).thenReturn(Collections.singletonList(tarea));

		List<Tareas> resultado = controller.getTarea(convertir(filtro, ProyectosId.class));

		assertEquals(1, resultado.size());
		assertSame(tarea, resultado.get(0));
		verify(tareastable).findByIdProyecto(20);
	}

	@Test
	@DisplayName("UT-16 - Proyecto sin tareas devuelve lista vacia")
	void proyectoSinTareasDevuelveListaVacia() {
		Tareas filtro = new Tareas();
		filtro.setId_proyecto(20);
		when(tareastable.findByIdProyecto(20)).thenReturn(Collections.emptyList());

		List<Tareas> resultado = controller.getTarea(convertir(filtro, ProyectosId.class));

		assertTrue(resultado.isEmpty());
		verify(tareastable).findByIdProyecto(20);
	}

	@Test
	@DisplayName("UT-17 - Buscar tarea existente devuelve la tarea encontrada")
	void buscarTareaExistenteDevuelveTareaEncontrada() {
		Tareas esperado = new Tareas();
		esperado.setId_tarea(3);
		when(tareastable.findById(3)).thenReturn(Optional.of(esperado));

		Tareas resultado = controller.getTarea(3);

		assertSame(esperado, resultado);
		verify(tareastable).findById(3);
	}

	@Test
	@DisplayName("UT-18 - Buscar tarea inexistente devuelve una tarea vacia")
	void buscarTareaInexistenteDevuelveTareaVacia() {
		when(tareastable.findById(404)).thenReturn(Optional.empty());

		Tareas resultado = controller.getTarea(404);

		assertNull(resultado.getId_tarea());
		verify(tareastable).findById(404);
	}

	@Test
	@DisplayName("UT-19 - Agregar tarea guarda el registro y devuelve uno")
	void agregarTareaGuardaYDevuelveUno() {
		Tareas tarea = new Tareas();
		tarea.setNombre("Tarea nueva");

		int resultado = controller.addTareas(convertir(tarea, TareasSolicitud.class));

		assertEquals(1, resultado);
		verify(tareastable).save(refEq(tarea));
	}

	@Test
	@DisplayName("UT-20 - Actualizar tarea guarda el registro y devuelve uno")
	void actualizarTareaGuardaYDevuelveUno() {
		Tareas tarea = new Tareas();
		tarea.setId_tarea(3);
		tarea.setNombre("Tarea actualizada");

		int resultado = controller.updateTareas(convertir(tarea, TareasSolicitud.class));

		assertEquals(1, resultado);
		verify(tareastable).save(refEq(tarea));
	}

	@Test
	@DisplayName("UT-21 - Eliminar tarea borra el registro y devuelve uno")
	void eliminarTareaBorraYDevuelveUno() {
		Tareas tarea = new Tareas();
		tarea.setId_tarea(3);

		int resultado = controller.deleteTareas(convertir(tarea, TareasId.class));

		assertEquals(1, resultado);
		verify(tareastable).deleteById(tarea.getId_tarea());
	}

	@Test
	@DisplayName("UT-22 - Listar tareas devuelve los registros encontrados")
	void listarTareasDevuelveLosRegistrosEncontrados() {
		Tareas una = new Tareas();
		una.setId_tarea(1);
		Tareas dos = new Tareas();
		dos.setId_tarea(2);
		when(tareastable.findAll()).thenReturn(Arrays.asList(una, dos));

		List<Tareas> resultado = controller.getTareas();

		assertEquals(2, resultado.size());
		assertSame(una, resultado.get(0));
		assertSame(dos, resultado.get(1));
		verify(tareastable).findAll();
	}

	@Test
	@DisplayName("UT-39 - Listar tareas sin registros devuelve lista vacia")
	void listarTareasSinRegistrosDevuelveListaVacia() {
		when(tareastable.findAll()).thenReturn(Collections.emptyList());

		List<Tareas> resultado = controller.getTareas();

		assertTrue(resultado.isEmpty());
		verify(tareastable).findAll();
	}
}
