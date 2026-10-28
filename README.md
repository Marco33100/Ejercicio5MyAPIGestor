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

La compilación más reciente con JDK 21 y Maven 3.9.11 terminó en `BUILD SUCCESS`: **58 pruebas aprobadas**, sin fallos, errores ni pruebas omitidas. Se utilizó la base MySQL 8.4 de pruebas para la verificación.

El plugin Maven de JaCoCo 0.8.15 ya estaba configurado. `clean verify` genera:

- Reporte HTML: `target/site/jacoco/index.html`.
- Reporte XML: `target/site/jacoco/jacoco.xml`.

La suite verificada ahora cubre **271/271 lineas, 103/103 metodos y 42/42 ramas: 100 %** en las tres metricas. Se corrigieron las comprobaciones de nulos de los cinco controladores y se agregaron pruebas de entradas nulas y del arranque real de `main`, ademas de las pruebas HTTP existentes. No se excluyeron clases ni metodos. Consulta la [verificacion actual al 100 %](docs/cobertura-100.md) y el [resultado anterior](docs/cobertura-api.md). Coverage de Jenkins publica el XML generado por JaCoCo; vuelve a construir **CompilarAPIJava21** para actualizar tambien SonarQube.

El POM ahora declara explícitamente Sonar Maven Plugin **5.5.0.6356** y Surefire **3.5.4**, además del JaCoCo **0.8.15** existente. Antes, Surefire se utilizaba con la versión 2.22.2 heredada de Spring Boot.

El análisis SonarQube requiere configurar el servidor y su token y ejecutar `mvn sonar:sonar`. Declarar el plugin en el POM no ejecuta ese análisis automáticamente. Consulta el [detalle de los cambios](docs/actualizacion-java21.md).

## Actualización a Java 21

El POM pasó de Java 11 a Java 21. Se conservaron Spring Boot 2.7.17, Maven Wrapper y las dependencias existentes. Se verificaron además el arranque del ejecutable y el registro y acceso de un usuario desde el cliente.

- [POM original para Java 11](docs/pom-original.xml)
- [POM actualizado para Java 21](pom.xml)
- [Explicación de la actualización](docs/actualizacion-java21.md)

El historial conserva un primer commit con el POM anterior y un segundo commit con la actualización.

## Jenkins

Para el tercer pipeline de la practica, consulta la [configuracion de **Base de Datos MySQL**](docs/jenkins-mysql.md) y su [script independiente](database/Jenkinsfile). Valida el SQL y ejecuta 21 pruebas en la base aislada `dbgestor_pipeline_ci`, utilizando la credencial `mysql-ci`. La prueba directa termino en `BUILD SUCCESS`; el trabajo debe construirse en Jenkins para publicar sus propios resultados.

El repositorio incluye un [Jenkinsfile](Jenkinsfile) para compilar con las herramientas `JDK21` y `Maven3` (Maven 3.9.11), ejecutar las pruebas existentes con MySQL, publicar los resultados y la cobertura, analizar el proyecto con SonarQube y generar un reporte SAST con Semgrep.

Utiliza la base `dbgestor_ci` en `mysql-java21-ci:3306` y obtiene el usuario y la contraseña de una credencial Jenkins con ID `mysql-ci`. Jenkins y MySQL deben compartir la red Docker `jenkins-java21-ci`.

Consulta la [guía de configuración del trabajo, credenciales y MySQL](docs/jenkins-java21.md). La ejecución #1 de `CompilarAPIJava21` terminó en `SUCCESS` con JDK 21.0.12.1 y Maven 3.9.11: 42 pruebas aprobadas, cero fallos y errores, y JAR generado. JUnit publicó los resultados y Coverage procesó el reporte de JaCoCo: 183 de 271 líneas cubiertas (67.53 %). Esta ejecución en Jenkins es adicional a la verificación local indicada arriba.

La ejecución #1 es anterior a la incorporación de Sonar y la declaración explícita de Surefire. Las nuevas etapas requieren el servidor Jenkins `SonarServer` con su token y Semgrep 1.181.0 instalado en el agente. Consulta los requisitos en la guía anterior. Copia el Jenkinsfile completo en **Pipeline script** y vuelve a construir el trabajo para comprobar el pipeline actualizado.
