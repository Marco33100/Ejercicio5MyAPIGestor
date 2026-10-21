# Ejercicio5MyAPIGestor

API REST del gestor de proyectos, desarrollada con Java 21, Spring Boot 2.7.17, Spring Data JPA y MySQL. Se ejecuta en el puerto 8081 y es consumida por [Ejercicio5Cliente](https://github.com/Marco33100/Ejercicio5Cliente).

## Requisitos

- JDK 21 y `JAVA_HOME` configurado con ese JDK.
- MySQL en ejecución y una base `dbgestor` accesible.
- Conexión a Internet en el primer uso para descargar Maven y las dependencias.

El proyecto incluye Maven Wrapper 3.9.11; no es necesario instalar Maven globalmente.

## Preparar MySQL

En MySQL Workbench o en una consola SQL, crea la base:

```sql
CREATE DATABASE IF NOT EXISTS dbgestor
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

El usuario utilizado por la API debe tener permisos sobre esa base. Hibernate tiene `ddl-auto=update`, por lo que crea o actualiza sus tablas al arrancar.

La conexión se configura con las variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD`. Sustituye los valores de usuario y contraseña del ejemplo por los de tu instalación.

## Compilar y ejecutar en Windows

Desde la carpeta de este repositorio, en PowerShell:

```powershell
$env:DB_HOST = 'localhost'
$env:DB_PORT = '3306'
$env:DB_NAME = 'dbgestor'
$env:DB_USER = 'TU_USUARIO_MYSQL'
$env:DB_PASSWORD = 'TU_CONTRASENA_MYSQL'

java -version
.\mvnw.cmd --version
.\mvnw.cmd clean verify
java -jar target/Ejercicio5MyAPIGestor-0.0.1-SNAPSHOT.jar
```

La API queda disponible en `http://localhost:8081`. La raíz no tiene una página web; el cliente proporciona la interfaz. La compilación ejecuta una prueba de arranque que necesita la conexión a MySQL configurada.

En Linux o macOS utiliza `./mvnw` en lugar de `.\mvnw.cmd` y configura las mismas variables en tu terminal.

## Pruebas y cobertura

La compilación con JDK 21 y Maven 3.9.11 terminó en `BUILD SUCCESS`: 42 pruebas aprobadas, sin fallos, errores ni pruebas omitidas. Se utilizó una base MySQL 8.4 temporal para la verificación.

El plugin Maven de JaCoCo 0.8.15 ya estaba configurado. `clean verify` genera:

- Reporte HTML: `target/site/jacoco/index.html`.
- Reporte XML: `target/site/jacoco/jacoco.xml`.

La suite verificada cubrió 183 de 271 líneas, equivalentes al 67.53 %. Coverage de Jenkins puede publicar el XML una vez configurado el trabajo.

## Actualización a Java 21

El POM pasó de Java 11 a Java 21. Se conservaron Spring Boot 2.7.17, Maven Wrapper y las dependencias existentes. Se verificaron además el arranque del ejecutable y el registro y acceso de un usuario desde el cliente.

- [POM original para Java 11](docs/pom-original.xml)
- [POM actualizado para Java 21](pom.xml)
- [Explicación de la actualización](docs/actualizacion-java21.md)

El historial conserva un primer commit con el POM anterior y un segundo commit con la actualización.

## Jenkins

El repositorio incluye un [Jenkinsfile](Jenkinsfile) para compilar con las herramientas `JDK21` y `Maven3` (Maven 3.9.11), ejecutar las pruebas existentes con MySQL y publicar los resultados y la cobertura.

Utiliza la base `dbgestor_ci` en `mysql-java21-ci:3306` y obtiene el usuario y la contraseña de una credencial Jenkins con ID `mysql-ci`. Jenkins y MySQL deben compartir la red Docker `jenkins-java21-ci`.

Consulta la [guía de configuración del trabajo, credenciales y MySQL](docs/jenkins-java21.md). La primera ejecución del trabajo `CompilarAPIJava21` está pendiente de comprobarse en Jenkins. Los resultados de pruebas indicados arriba corresponden a la verificación local.
