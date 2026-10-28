package com.mx.cbtis.gestor.controller;

import com.mx.cbtis.gestor.dto.UsuarioSolicitud;
import com.mx.cbtis.gestor.dto.UsuarioId;
import com.mx.cbtis.gestor.dto.LoginSolicitud;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {
	
	private final UsuarioRepository usuariotable;

	public UsuarioController(UsuarioRepository usuariotable) {
		this.usuariotable = usuariotable;
	}
	
	@PostMapping("/getUsuarioLogin")
	public Usuario getUsuarioLogin(@RequestBody LoginSolicitud userParam) {
		
		Optional<Usuario> optionaluser = usuariotable.findByCorreoPass(
				userParam.correoElectronico(),
				userParam.contrasena());
		
		Usuario user = new Usuario();
		
		if(optionaluser.isPresent()) {
			user = optionaluser.get();
		}else {
			user.setNombre("000");
		}
		
		return user;
	}
	
	@GetMapping("/buscarUsuario")
	public Usuario getUsuario(@RequestParam int id) {
		
		Usuario buscar = new Usuario();
		Optional<Usuario> usuario = usuariotable.findById(id);
		if(usuario.isPresent())
		{
			buscar = usuario.get();
		}
		
		return buscar;
	}
	
	@PostMapping("/addUsuario")
	public int addUsuario(@RequestBody UsuarioSolicitud usuario) {
		int flag = 0;
		if (usuario != null) {
			usuariotable.save(new Usuario(null, usuario.nombre(), usuario.correoElectronico(), usuario.contrasena(), "Estudiante", usuario.imagenUsuario()));
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/updateUsuario")
	public int updateUsuario(@RequestBody UsuarioSolicitud usuario) {
		int flag = 0;
		if (usuario != null) {
			Optional<Usuario> encontrado = usuariotable.findById(usuario.idUsuario());
            if (encontrado.isEmpty()) {
                return 0;
            }
            Usuario existente = encontrado.get();
            usuariotable.save(new Usuario(usuario.idUsuario(), usuario.nombre(), usuario.correoElectronico(), usuario.contrasena(), existente.getRol(), usuario.imagenUsuario()));
			flag = 1;
		}
		
		return flag;
	}
	
	@DeleteMapping("/deleteUsuario")
	public int deleteUsuario(@RequestBody UsuarioId usuario) {
		int flag = 0;
		if (usuario != null) {
			usuariotable.deleteById(usuario.idUsuario());
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/buscarUsuarios")
	public List<Usuario> getUsuarios(){
		List<Usuario> listaUsuarios = new ArrayList<>(); 
		usuariotable.findAll().forEach(listaUsuarios::add);
		return listaUsuarios;
	}		
}
