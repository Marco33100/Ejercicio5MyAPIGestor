-- Esquema de CI separado de la base que utiliza la aplicacion.
-- Los datos de usuarios del dump original no se incluyen.
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    correo_electronico VARCHAR(255),
    contrasena VARCHAR(255),
    rol VARCHAR(255),
    imagen_usuario VARCHAR(255),
    CONSTRAINT uq_usuario_correo UNIQUE (correo_electronico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS proyectos (
    id_proyecto INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    descripcion VARCHAR(255),
    fecha_inicio VARCHAR(255),
    fecha_finalizacion VARCHAR(255),
    estado_proyecto VARCHAR(255),
    fondo VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tareas (
    id_tarea INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    descripcion VARCHAR(255),
    fecha_vencimiento VARCHAR(255),
    lista VARCHAR(255),
    estado VARCHAR(255),
    id_proyecto INT,
    CONSTRAINT fk_tarea_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS asignaciones (
    id_asignacion INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    id_tarea INT,
    fecha_asignacion VARCHAR(255),
    CONSTRAINT fk_asignacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_asignacion_tarea FOREIGN KEY (id_tarea) REFERENCES tareas(id_tarea)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comentarios (
    id_comentario INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_usuario VARCHAR(255),
    id_tarea INT,
    texto_comentario VARCHAR(255),
    fecha_hora VARCHAR(255),
    CONSTRAINT fk_comentario_tarea FOREIGN KEY (id_tarea) REFERENCES tareas(id_tarea)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hibernate_sequence (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO hibernate_sequence (next_val)
SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM hibernate_sequence);
