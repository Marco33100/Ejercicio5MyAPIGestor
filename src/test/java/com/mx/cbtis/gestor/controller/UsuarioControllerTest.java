package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

	@Mock
	private UsuarioRepository usuariotable;

	@InjectMocks
	private UsuarioController controller;

	@Test
	@DisplayName("UT-01 - Login con credenciales validas devuelve el usuario encontrado")
	void loginConCredencialesValidasDevuelveUsuarioEncontrado() {
		String correo = "ana@example.com";
		String contrasena = "clave-segura";

		Usuario datosLogin = new Usuario();
		datosLogin.setCorreo_electronico(correo);
		datosLogin.setContrasena(contrasena);

		Usuario usuarioEncontrado = new Usuario();
		usuarioEncontrado.setId_usuario(1);
		usuarioEncontrado.setNombre("Ana");
		usuarioEncontrado.setCorreo_electronico(correo);

		when(usuariotable.findByCorreoPass(correo, contrasena))
				.thenReturn(Optional.of(usuarioEncontrado));

		Usuario resultado = controller.getUsuarioLogin(convertir(datosLogin, LoginSolicitud.class));

		assertSame(usuarioEncontrado, resultado);
		verify(usuariotable).findByCorreoPass(correo, contrasena);
	}

	@Test
	@DisplayName("UT-02 - Login sin coincidencia devuelve el usuario marcador")
	void loginSinCoincidenciaDevuelveUsuarioMarcador() {
		String correo = "desconocido@example.com";
		String contrasena = "incorrecta";
		Usuario datosLogin = new Usuario();
		datosLogin.setCorreo_electronico(correo);
		datosLogin.setContrasena(contrasena);
		when(usuariotable.findByCorreoPass(correo, contrasena)).thenReturn(Optional.empty());

		Usuario resultado = controller.getUsuarioLogin(convertir(datosLogin, LoginSolicitud.class));

		assertEquals("000", resultado.getNombre());
		verify(usuariotable).findByCorreoPass(correo, contrasena);
	}

	@Test
	@DisplayName("UT-03 - Buscar usuario existente devuelve el usuario encontrado")
	void buscarUsuarioExistenteDevuelveUsuarioEncontrado() {
		Usuario esperado = new Usuario();
		esperado.setId_usuario(7);
		when(usuariotable.findById(7)).thenReturn(Optional.of(esperado));

		Usuario resultado = controller.getUsuario(7);

		assertSame(esperado, resultado);
		verify(usuariotable).findById(7);
	}

	@Test
	@DisplayName("UT-04 - Buscar usuario inexistente devuelve un usuario vacio")
	void buscarUsuarioInexistenteDevuelveUsuarioVacio() {
		when(usuariotable.findById(404)).thenReturn(Optional.empty());

		Usuario resultado = controller.getUsuario(404);

		assertNull(resultado.getId_usuario());
		verify(usuariotable).findById(404);
	}

	@Test
	@DisplayName("UT-05 - Agregar usuario guarda el registro y devuelve uno")
	void agregarUsuarioGuardaYDevuelveUno() {
		Usuario usuario = new Usuario();
		usuario.setNombre("Ana");
		usuario.setRol("Estudiante");

		int resultado = controller.addUsuario(convertir(usuario, UsuarioSolicitud.class));

		assertEquals(1, resultado);
		verify(usuariotable).save(refEq(usuario));
	}

	@Test
	@DisplayName("UT-06 - Actualizar usuario guarda el registro y devuelve uno")
	void actualizarUsuarioGuardaYDevuelveUno() {
		Usuario usuario = new Usuario();
		usuario.setId_usuario(7);
		usuario.setNombre("Ana actualizada");
		when(usuariotable.findById(7)).thenReturn(Optional.of(usuario));

		int resultado = controller.updateUsuario(convertir(usuario, UsuarioSolicitud.class));

		assertEquals(1, resultado);
		verify(usuariotable).save(refEq(usuario));
	}

	@Test
	@DisplayName("UT-07 - Eliminar usuario borra el registro y devuelve uno")
	void eliminarUsuarioBorraYDevuelveUno() {
		Usuario usuario = new Usuario();
		usuario.setId_usuario(7);

		int resultado = controller.deleteUsuario(convertir(usuario, UsuarioId.class));

		assertEquals(1, resultado);
		verify(usuariotable).deleteById(usuario.getId_usuario());
	}

	@Test
	@DisplayName("UT-08 - Listar usuarios devuelve los registros encontrados")
	void listarUsuariosDevuelveLosRegistrosEncontrados() {
		Usuario ana = new Usuario();
		ana.setId_usuario(1);
		Usuario luis = new Usuario();
		luis.setId_usuario(2);
		when(usuariotable.findAll()).thenReturn(Arrays.asList(ana, luis));

		List<Usuario> resultado = controller.getUsuarios();

		assertEquals(2, resultado.size());
		assertSame(ana, resultado.get(0));
		assertSame(luis, resultado.get(1));
		verify(usuariotable).findAll();
	}

	@Test
	@DisplayName("UT-37 - Listar usuarios sin registros devuelve lista vacia")
	void listarUsuariosSinRegistrosDevuelveListaVacia() {
		when(usuariotable.findAll()).thenReturn(Collections.emptyList());

		List<Usuario> resultado = controller.getUsuarios();

		assertTrue(resultado.isEmpty());
		verify(usuariotable).findAll();
	}
}
