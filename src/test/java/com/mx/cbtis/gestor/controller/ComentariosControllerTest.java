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

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@ExtendWith(MockitoExtension.class)
class ComentariosControllerTest {

	@Mock
	private ComentariosRepository comentariostable;

	@InjectMocks
	private ComentariosController controller;

	@Test
	@DisplayName("UT-23 - Obtener comentarios de una tarea devuelve sus comentarios")
	void obtenerComentariosDeTareaDevuelveSusComentarios() {
		Comentarios filtro = new Comentarios();
		filtro.setId_tarea(30);
		Comentarios comentario = new Comentarios();
		comentario.setId_comentario(5);
		comentario.setId_tarea(30);
		when(comentariostable.findByTarea(30)).thenReturn(Collections.singletonList(comentario));

		List<Comentarios> resultado = controller.getComentario(convertir(filtro, TareasId.class));

		assertEquals(1, resultado.size());
		assertSame(comentario, resultado.get(0));
		verify(comentariostable).findByTarea(30);
	}

	@Test
	@DisplayName("UT-24 - Tarea sin comentarios devuelve lista vacia")
	void tareaSinComentariosDevuelveListaVacia() {
		Comentarios filtro = new Comentarios();
		filtro.setId_tarea(30);
		when(comentariostable.findByTarea(30)).thenReturn(Collections.emptyList());

		List<Comentarios> resultado = controller.getComentario(convertir(filtro, TareasId.class));

		assertTrue(resultado.isEmpty());
		verify(comentariostable).findByTarea(30);
	}

	@Test
	@DisplayName("UT-25 - Buscar comentario existente devuelve el comentario encontrado")
	void buscarComentarioExistenteDevuelveComentarioEncontrado() {
		Comentarios esperado = new Comentarios();
		esperado.setId_comentario(5);
		when(comentariostable.findById(5)).thenReturn(Optional.of(esperado));

		Comentarios resultado = controller.getComentario(5);

		assertSame(esperado, resultado);
		verify(comentariostable).findById(5);
	}

	@Test
	@DisplayName("UT-26 - Buscar comentario inexistente devuelve un comentario vacio")
	void buscarComentarioInexistenteDevuelveComentarioVacio() {
		when(comentariostable.findById(404)).thenReturn(Optional.empty());

		Comentarios resultado = controller.getComentario(404);

		assertNull(resultado.getId_comentario());
		verify(comentariostable).findById(404);
	}

	@Test
	@DisplayName("UT-27 - Agregar comentario guarda el registro y devuelve uno")
	void agregarComentarioGuardaYDevuelveUno() {
		Comentarios comentario = new Comentarios();
		comentario.setTexto_comentario("Comentario nuevo");

		int resultado = controller.addComentario(convertir(comentario, ComentariosSolicitud.class));

		assertEquals(1, resultado);
		verify(comentariostable).save(refEq(comentario));
	}

	@Test
	@DisplayName("UT-28 - Actualizar comentario guarda el registro y devuelve uno")
	void actualizarComentarioGuardaYDevuelveUno() {
		Comentarios comentario = new Comentarios();
		comentario.setId_comentario(5);
		comentario.setTexto_comentario("Comentario actualizado");

		int resultado = controller.updateComentario(convertir(comentario, ComentariosSolicitud.class));

		assertEquals(1, resultado);
		verify(comentariostable).save(refEq(comentario));
	}

	@Test
	@DisplayName("UT-29 - Eliminar comentario borra el registro y devuelve uno")
	void eliminarComentarioBorraYDevuelveUno() {
		Comentarios comentario = new Comentarios();
		comentario.setId_comentario(5);

		int resultado = controller.deleteComentario(convertir(comentario, ComentariosId.class));

		assertEquals(1, resultado);
		verify(comentariostable).deleteById(comentario.getId_comentario());
	}

	@Test
	@DisplayName("UT-30 - Listar comentarios devuelve los registros encontrados")
	void listarComentariosDevuelveLosRegistrosEncontrados() {
		Comentarios uno = new Comentarios();
		uno.setId_comentario(1);
		Comentarios dos = new Comentarios();
		dos.setId_comentario(2);
		when(comentariostable.findAll()).thenReturn(Arrays.asList(uno, dos));

		List<Comentarios> resultado = controller.getComentarios();

		assertEquals(2, resultado.size());
		assertSame(uno, resultado.get(0));
		assertSame(dos, resultado.get(1));
		verify(comentariostable).findAll();
	}

	@Test
	@DisplayName("UT-40 - Listar comentarios sin registros devuelve lista vacia")
	void listarComentariosSinRegistrosDevuelveListaVacia() {
		when(comentariostable.findAll()).thenReturn(Collections.emptyList());

		List<Comentarios> resultado = controller.getComentarios();

		assertTrue(resultado.isEmpty());
		verify(comentariostable).findAll();
	}
}
