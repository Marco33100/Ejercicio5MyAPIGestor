package com.mx.cbtis.gestor.controller;

import com.mx.cbtis.gestor.dto.ComentariosSolicitud;
import com.mx.cbtis.gestor.dto.ComentariosId;
import com.mx.cbtis.gestor.dto.TareasId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@RestController
public class ComentariosController {
	
	private final ComentariosRepository comentariostable;

	public ComentariosController(ComentariosRepository comentariostable) {
		this.comentariostable = comentariostable;
	}
	
	@PostMapping("/getComentario")

	public List<Comentarios> getComentario(@RequestBody TareasId idComentario){
		List<Comentarios> listaComentarios = comentariostable.findByTarea(idComentario.idTarea());
		return listaComentarios;
	}
	
	@GetMapping("/buscarComentario")
	public Comentarios getComentario(@RequestParam int id) {
		
		Comentarios buscar = new Comentarios();
		Optional<Comentarios> comentario = comentariostable.findById(id);
		if(comentario.isPresent()) {
			buscar = comentario.get();
		}
		
		return buscar;
	}
	
	@PostMapping("/addComentario")
	public int addComentario(@RequestBody ComentariosSolicitud comentario) {
		
		int flag = 0;
		if (comentario != null) {
			comentariostable.save(new Comentarios(null, comentario.idUsuario(), comentario.idTarea(), comentario.textoComentario(), comentario.fechaHora()));
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/updateComentario")
	public int updateComentario(@RequestBody ComentariosSolicitud comentario) {
		
		int flag = 0;
		if (comentario != null) {
			comentariostable.save(new Comentarios(comentario.idComentario(), comentario.idUsuario(), comentario.idTarea(), comentario.textoComentario(), comentario.fechaHora()));
			flag = 1;
		}
		
		return flag;
	}
	
	@DeleteMapping("/deleteComentario")
	public int deleteComentario(@RequestBody ComentariosId comentario) {
		
		int flag = 0;
		if (comentario != null) {
			comentariostable.deleteById(comentario.idComentario());
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/buscarComentarios")
	public List<Comentarios> getComentarios(){
		
		List<Comentarios> listaComentarios = new ArrayList<>();
		comentariostable.findAll().forEach(listaComentarios :: add);
		return listaComentarios;
	}

}
