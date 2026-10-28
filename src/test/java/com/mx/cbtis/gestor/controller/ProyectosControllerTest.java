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

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProtectosRepository;

@ExtendWith(MockitoExtension.class)
class ProyectosControllerTest {

	@Mock
	private ProtectosRepository proyectostable;

	@InjectMocks
	private ProyectosController controller;

	@Test
	@DisplayName("UT-09 - Buscar proyecto existente devuelve el proyecto encontrado")
	void buscarProyectoExistenteDevuelveProyectoEncontrado() {
		Proyectos esperado = new Proyectos();
		esperado.setId_proyecto(12);
		when(proyectostable.findById(12)).thenReturn(Optional.of(esperado));

		Proyectos resultado = controller.getProyecto(12);

		assertSame(esperado, resultado);
		verify(proyectostable).findById(12);
	}

	@Test
	@DisplayName("UT-10 - Buscar proyecto inexistente devuelve un proyecto vacio")
	void buscarProyectoInexistenteDevuelveProyectoVacio() {
		when(proyectostable.findById(404)).thenReturn(Optional.empty());

		Proyectos resultado = controller.getProyecto(404);

		assertNull(resultado.getId_proyecto());
		verify(proyectostable).findById(404);
	}

	@Test
	@DisplayName("UT-11 - Agregar proyecto guarda el registro y devuelve uno")
	void agregarProyectoGuardaYDevuelveUno() {
		Proyectos proyecto = new Proyectos();
		proyecto.setNombre("Proyecto nuevo");

		int resultado = controller.addProyecto(convertir(proyecto, ProyectosSolicitud.class));

		assertEquals(1, resultado);
		verify(proyectostable).save(refEq(proyecto));
	}

	@Test
	@DisplayName("UT-12 - Actualizar proyecto guarda el registro y devuelve uno")
	void actualizarProyectoGuardaYDevuelveUno() {
		Proyectos proyecto = new Proyectos();
		proyecto.setId_proyecto(12);
		proyecto.setNombre("Proyecto actualizado");

		int resultado = controller.updateProyecto(convertir(proyecto, ProyectosSolicitud.class));

		assertEquals(1, resultado);
		verify(proyectostable).save(refEq(proyecto));
	}

	@Test
	@DisplayName("UT-13 - Eliminar proyecto borra el registro y devuelve uno")
	void eliminarProyectoBorraYDevuelveUno() {
		Proyectos proyecto = new Proyectos();
		proyecto.setId_proyecto(12);

		int resultado = controller.deleteProyecto(convertir(proyecto, ProyectosId.class));

		assertEquals(1, resultado);
		verify(proyectostable).deleteById(proyecto.getId_proyecto());
	}

	@Test
	@DisplayName("UT-14 - Listar proyectos devuelve los registros encontrados")
	void listarProyectosDevuelveLosRegistrosEncontrados() {
		Proyectos uno = new Proyectos();
		uno.setId_proyecto(1);
		Proyectos dos = new Proyectos();
		dos.setId_proyecto(2);
		when(proyectostable.findAll()).thenReturn(Arrays.asList(uno, dos));

		List<Proyectos> resultado = controller.getProyectos();

		assertEquals(2, resultado.size());
		assertSame(uno, resultado.get(0));
		assertSame(dos, resultado.get(1));
		verify(proyectostable).findAll();
	}

	@Test
	@DisplayName("UT-38 - Listar proyectos sin registros devuelve lista vacia")
	void listarProyectosSinRegistrosDevuelveListaVacia() {
		when(proyectostable.findAll()).thenReturn(Collections.emptyList());

		List<Proyectos> resultado = controller.getProyectos();

		assertTrue(resultado.isEmpty());
		verify(proyectostable).findAll();
	}
}
