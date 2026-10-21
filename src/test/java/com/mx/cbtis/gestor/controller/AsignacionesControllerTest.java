package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
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

import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

@ExtendWith(MockitoExtension.class)
class AsignacionesControllerTest {

	@Mock
	private AsignacionesRepository asignacionestable;

	@InjectMocks
	private AsignacionesController controller;

	@Test
	@DisplayName("UT-31 - Buscar asignacion existente devuelve la asignacion encontrada")
	void buscarAsignacionExistenteDevuelveAsignacionEncontrada() {
		Asignaciones esperado = new Asignaciones();
		esperado.setId_asignacion(8);
		when(asignacionestable.findById(8)).thenReturn(Optional.of(esperado));

		Asignaciones resultado = controller.getAsignacion(8);

		assertSame(esperado, resultado);
		verify(asignacionestable).findById(8);
	}

	@Test
	@DisplayName("UT-32 - Buscar asignacion inexistente devuelve una asignacion vacia")
	void buscarAsignacionInexistenteDevuelveAsignacionVacia() {
		when(asignacionestable.findById(404)).thenReturn(Optional.empty());

		Asignaciones resultado = controller.getAsignacion(404);

		assertNull(resultado.getId_asignacion());
		verify(asignacionestable).findById(404);
	}

	@Test
	@DisplayName("UT-33 - Agregar asignacion guarda el registro y devuelve uno")
	void agregarAsignacionGuardaYDevuelveUno() {
		Asignaciones asignacion = new Asignaciones();
		asignacion.setId_usuario(4);
		asignacion.setId_tarea(3);

		int resultado = controller.addAsignacion(asignacion);

		assertEquals(1, resultado);
		verify(asignacionestable).save(asignacion);
	}

	@Test
	@DisplayName("UT-34 - Actualizar asignacion guarda el registro y devuelve uno")
	void actualizarAsignacionGuardaYDevuelveUno() {
		Asignaciones asignacion = new Asignaciones();
		asignacion.setId_asignacion(8);
		asignacion.setId_usuario(4);
		asignacion.setId_tarea(3);

		int resultado = controller.updateAsignacion(asignacion);

		assertEquals(1, resultado);
		verify(asignacionestable).save(asignacion);
	}

	@Test
	@DisplayName("UT-35 - Eliminar asignacion borra el registro y devuelve uno")
	void eliminarAsignacionBorraYDevuelveUno() {
		Asignaciones asignacion = new Asignaciones();
		asignacion.setId_asignacion(8);

		int resultado = controller.deleteAsignacion(asignacion);

		assertEquals(1, resultado);
		verify(asignacionestable).delete(asignacion);
	}

	@Test
	@DisplayName("UT-36 - Listar asignaciones devuelve los registros encontrados")
	void listarAsignacionesDevuelveLosRegistrosEncontrados() {
		Asignaciones una = new Asignaciones();
		una.setId_asignacion(1);
		Asignaciones dos = new Asignaciones();
		dos.setId_asignacion(2);
		when(asignacionestable.findAll()).thenReturn(Arrays.asList(una, dos));

		List<Asignaciones> resultado = controller.getAsignaciones();

		assertEquals(2, resultado.size());
		assertSame(una, resultado.get(0));
		assertSame(dos, resultado.get(1));
		verify(asignacionestable).findAll();
	}

	@Test
	@DisplayName("UT-41 - Listar asignaciones sin registros devuelve lista vacia")
	void listarAsignacionesSinRegistrosDevuelveListaVacia() {
		when(asignacionestable.findAll()).thenReturn(Collections.emptyList());

		List<Asignaciones> resultado = controller.getAsignaciones();

		assertTrue(resultado.isEmpty());
		verify(asignacionestable).findAll();
	}
}
