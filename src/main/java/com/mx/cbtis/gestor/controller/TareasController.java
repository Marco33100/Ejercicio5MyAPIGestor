package com.mx.cbtis.gestor.controller;

import com.mx.cbtis.gestor.dto.TareasSolicitud;
import com.mx.cbtis.gestor.dto.TareasId;
import com.mx.cbtis.gestor.dto.ProyectosId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

@RestController
public class TareasController {
	
	private final TareasRepository tareastable;

	public TareasController(TareasRepository tareastable) {
		this.tareastable = tareastable;
	}
	
	@PostMapping("/getTarea")
	public List<Tareas> getTarea(@RequestBody ProyectosId id_proyecto) {
		List<Tareas> listaTareas = tareastable.findByIdProyecto(id_proyecto.idProyecto());
		return listaTareas;
	}
	
	@GetMapping("/buscarTarea")
	public Tareas getTarea(@RequestParam int id_tarea) {
		
		Tareas buscar = new Tareas();
		Optional<Tareas> tarea = tareastable.findById(id_tarea);
		if(tarea.isPresent()) {
			buscar = tarea.get();
		}
		
		return buscar;
	}
	
	@PostMapping("/addTareas")
	public int addTareas(@RequestBody TareasSolicitud tareas) {
		
		int flag = 0;
		if (tareas != null) {
			tareastable.save(new Tareas(null, tareas.nombre(), tareas.descripcion(), tareas.fechaVencimiento(), tareas.lista(), tareas.estado(), tareas.idProyecto()));
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/updateTareas")
	public int updateTareas(@RequestBody TareasSolicitud tareas) {
		
		int flag = 0;
		if (tareas != null) {
			tareastable.save(new Tareas(tareas.idTarea(), tareas.nombre(), tareas.descripcion(), tareas.fechaVencimiento(), tareas.lista(), tareas.estado(), tareas.idProyecto()));
			flag = 1;
		}
		
		return flag;
	}
	
	@DeleteMapping("/deleteTareas")
	public int deleteTareas(@RequestBody TareasId tareas) {
		
		int flag = 0;
		if (tareas != null) {
			tareastable.deleteById(tareas.idTarea());
			flag = 1;
		}
		
		return flag;
	}
	
	@PostMapping("/buscarTareas")
	public List<Tareas> getTareas(){
		
		List<Tareas> listaTareas = new ArrayList<>();
		tareastable.findAll().forEach(listaTareas :: add);
		return listaTareas;
	}

}
