package com.mx.cbtis.database;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class BaseDatosMySqlTest {
    private static final String SCHEMA = "dbgestor_pipeline_ci";
    private static final Map<String, Set<String>> COLUMNAS = Map.of(
            "usuario", Set.of("id_usuario", "nombre", "correo_electronico", "contrasena", "rol", "imagen_usuario"),
            "proyectos", Set.of("id_proyecto", "nombre", "descripcion", "fecha_inicio", "fecha_finalizacion", "estado_proyecto", "fondo"),
            "tareas", Set.of("id_tarea", "nombre", "descripcion", "fecha_vencimiento", "lista", "estado", "id_proyecto"),
            "asignaciones", Set.of("id_asignacion", "id_usuario", "id_tarea", "fecha_asignacion"),
            "comentarios", Set.of("id_comentario", "id_usuario", "id_tarea", "texto_comentario", "fecha_hora"));
    private Connection connection;

    private static String variable(String nombre) {
        String value = System.getenv(nombre);
        if (value == null || value.isBlank()) throw new IllegalStateException("Falta la variable " + nombre);
        return value;
    }

    private static Connection conectar() throws SQLException {
        if (!SCHEMA.equals(variable("DB_NAME"))) {
            throw new IllegalStateException("Estas pruebas solo pueden usar " + SCHEMA);
        }
        Connection result = DriverManager.getConnection("jdbc:mysql://" + variable("DB_HOST") + ":"
                + variable("DB_PORT") + "/" + SCHEMA + "?allowPublicKeyRetrieval=true&serverTimezone=UTC",
                variable("DB_USER"), variable("DB_PASSWORD"));
        if (!SCHEMA.equals(result.getCatalog())) {
            result.close();
            throw new IllegalStateException("El catalogo conectado no es el de CI");
        }
        return result;
    }

    @BeforeAll
    static void aplicarEsquemaSoloEnLaBaseAislada() throws Exception {
        try (Connection c = conectar()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("schema.sql"));
            for (String tabla : COLUMNAS.keySet()) assertEquals(0, contar(c, tabla), "La base CI debe estar vacia");
        }
    }

    @BeforeEach
    void comenzarTransaccion() throws Exception {
        connection = conectar();
        connection.setAutoCommit(false);
    }

    @AfterEach
    void revertirDatosDePrueba() throws Exception {
        if (connection != null) {
            try { connection.rollback(); } finally { connection.close(); }
        }
    }

    @AfterAll
    static void verificarLimpiezaYGuardarEvidencia() throws Exception {
        try (Connection c = conectar()) {
            for (String tabla : COLUMNAS.keySet()) assertEquals(0, contar(c, tabla), "Hay datos sin revertir en " + tabla);
            Files.createDirectories(Path.of("target"));
            Files.writeString(Path.of("target/database-summary.txt"),
                    "Engine=" + c.getMetaData().getDatabaseProductName() + "\nVersion="
                    + c.getMetaData().getDatabaseProductVersion() + "\nSchema=" + c.getCatalog()
                    + "\nBusinessTables=5\nForeignKeys=4\nBusinessRowsAfterTests=0\n");
        }
    }

    private static int contar(Connection c, String tabla) throws SQLException {
        // Los nombres provienen exclusivamente de la lista fija COLUMNAS.
        if (!COLUMNAS.containsKey(tabla)) throw new IllegalArgumentException("Tabla no permitida");
        try (Statement statement = c.createStatement(); ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rows.next();
            return rows.getInt(1);
        }
    }

    private int ejecutar(String sql, Object... valores) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) statement.setObject(i + 1, valores[i]);
            return statement.executeUpdate();
        }
    }

    private int insertar(String sql, Object... valores) throws SQLException {
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) statement.setObject(i + 1, valores[i]);
            assertEquals(1, statement.executeUpdate());
            try (ResultSet keys = statement.getGeneratedKeys()) { assertTrue(keys.next()); return keys.getInt(1); }
        }
    }

    private int usuario(String correo) throws SQLException {
        return insertar("INSERT INTO usuario(nombre,correo_electronico,contrasena,rol) VALUES (?,?,?,?)",
                "Usuario sintetico", correo, "hash-de-prueba", "estudiante");
    }

    private int proyecto(String nombre) throws SQLException {
        return insertar("INSERT INTO proyectos(nombre,descripcion,fecha_inicio,estado_proyecto) VALUES (?,?,?,?)",
                nombre, "Validacion de CI", "2026-10-27", "Activo");
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuario", "proyectos", "tareas", "asignaciones", "comentarios"})
    void tablasTienenLasColumnasEsperadas(String tabla) throws Exception {
        Set<String> nombres = new HashSet<>();
        try (ResultSet columnas = connection.getMetaData().getColumns(SCHEMA, null, tabla, null)) {
            while (columnas.next()) nombres.add(columnas.getString("COLUMN_NAME"));
        }
        assertEquals(COLUMNAS.get(tabla), nombres);
    }

    @ParameterizedTest
    @CsvSource({"usuario,id_usuario", "proyectos,id_proyecto", "tareas,id_tarea",
            "asignaciones,id_asignacion", "comentarios,id_comentario"})
    void tablasTienenClavePrimaria(String tabla, String clave) throws Exception {
        try (ResultSet keys = connection.getMetaData().getPrimaryKeys(SCHEMA, null, tabla)) {
            assertTrue(keys.next()); assertEquals(clave, keys.getString("COLUMN_NAME")); assertFalse(keys.next());
        }
    }

    @Test
    void motorTransaccionalYUnicodeEstanHabilitados() throws Exception {
        try (var statement = connection.prepareStatement(
                "SELECT ENGINE,TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA=?")) {
            statement.setString(1, SCHEMA);
            int cantidad = 0;
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    assertEquals("InnoDB", rows.getString(1));
                    assertTrue(rows.getString(2).startsWith("utf8mb4_")); cantidad++;
                }
            }
            assertEquals(6, cantidad);
        }
    }

    @Test
    void secuenciaEsCompatibleConHibernateYElEsquemaEsIdempotente() throws Exception {
        ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
        try (var statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT next_val FROM hibernate_sequence")) {
            assertTrue(rows.next()); assertTrue(rows.getLong(1) > 0); assertFalse(rows.next());
        }
    }

    @Test
    void crudRelacionaLasCincoEntidades() throws Exception {
        int u = usuario("crud@example.test");
        int p = proyecto("Planificacion 🚀");
        int t = insertar("INSERT INTO tareas(nombre,id_proyecto,estado) VALUES (?,?,?)", "Probar SQL", p, "Pendiente");
        int a = insertar("INSERT INTO asignaciones(id_usuario,id_tarea) VALUES (?,?)", u, t);
        int c = insertar("INSERT INTO comentarios(id_usuario,id_tarea,texto_comentario) VALUES (?,?,?)", "Usuario sintetico", t, "Verificado");
        try (var statement = connection.prepareStatement(
                "SELECT u.correo_electronico,p.nombre FROM asignaciones a JOIN usuario u ON u.id_usuario=a.id_usuario "
                + "JOIN tareas t ON t.id_tarea=a.id_tarea JOIN proyectos p ON p.id_proyecto=t.id_proyecto WHERE a.id_asignacion=?")) {
            statement.setInt(1, a);
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next()); assertEquals("crud@example.test", rows.getString(1));
                assertEquals("Planificacion 🚀", rows.getString(2)); assertFalse(rows.next());
            }
        }
        assertEquals(1, ejecutar("UPDATE tareas SET estado=? WHERE id_tarea=?", "Terminada", t));
        try (var statement = connection.prepareStatement("SELECT estado FROM tareas WHERE id_tarea=?")) {
            statement.setInt(1, t);
            try (ResultSet rows = statement.executeQuery()) { assertTrue(rows.next()); assertEquals("Terminada", rows.getString(1)); }
        }
        assertEquals(1, ejecutar("DELETE FROM comentarios WHERE id_comentario=?", c));
        assertEquals(1, ejecutar("DELETE FROM asignaciones WHERE id_asignacion=?", a));
        assertEquals(1, ejecutar("DELETE FROM tareas WHERE id_tarea=?", t));
        assertEquals(1, ejecutar("DELETE FROM proyectos WHERE id_proyecto=?", p));
        assertEquals(1, ejecutar("DELETE FROM usuario WHERE id_usuario=?", u));
        for (String tabla : COLUMNAS.keySet()) assertEquals(0, contar(connection, tabla));
    }

    @Test
    void correoDuplicadoEsRechazado() throws Exception {
        usuario("unico@example.test");
        SQLException error = assertThrows(SQLException.class, () -> usuario("unico@example.test"));
        assertEquals("23000", error.getSQLState());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INSERT INTO tareas(id_proyecto) VALUES (999999999)",
            "INSERT INTO asignaciones(id_usuario) VALUES (999999999)",
            "INSERT INTO asignaciones(id_tarea) VALUES (999999999)",
            "INSERT INTO comentarios(id_tarea) VALUES (999999999)"})
    void referenciasInexistentesSonRechazadas(String sql) {
        SQLException error = assertThrows(SQLException.class, () -> ejecutar(sql));
        assertEquals("23000", error.getSQLState());
    }

    @Test
    void noSePuedeBorrarUnProyectoConTareas() throws Exception {
        int p = proyecto("Con tareas");
        insertar("INSERT INTO tareas(nombre,id_proyecto) VALUES (?,?)", "Tarea", p);
        SQLException error = assertThrows(SQLException.class, () -> ejecutar("DELETE FROM proyectos WHERE id_proyecto=?", p));
        assertEquals("23000", error.getSQLState());
    }

    @Test
    void rollbackRevierteLosDatosInsertados() throws Exception {
        usuario("rollback@example.test");
        assertEquals(1, contar(connection, "usuario")); connection.rollback();
        assertEquals(0, contar(connection, "usuario"));
    }

    @Test
    void valoresConSintaxisSqlSeGuardanComoDatos() throws Exception {
        String nombre = "x'); DROP TABLE usuario; --";
        int id = proyecto(nombre);
        try (var statement = connection.prepareStatement("SELECT nombre FROM proyectos WHERE id_proyecto=?")) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) { assertTrue(rows.next()); assertEquals(nombre, rows.getString(1)); }
        }
        assertEquals(0, contar(connection, "usuario"));
    }
}
