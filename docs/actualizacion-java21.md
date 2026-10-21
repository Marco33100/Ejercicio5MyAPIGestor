# Actualización de la API a Java 21

Fecha: 20 de octubre de 2026.

En `pom.xml`, la propiedad `<java.version>` cambió de `11` a `21`. Se conservaron Spring Boot 2.7.17, Maven Wrapper 3.9.11 y las dependencias existentes. El conector MySQL y JaCoCo 0.8.15 ya figuraban en el POM original guardado antes de esta migración.

Spring Boot 2.7.17 admite Java 21 según sus [requisitos oficiales](https://docs.spring.io/spring-boot/docs/2.7.17/reference/html/getting-started.html). Las pruebas confirmaron que no era necesario cambiar los modelos, controladores ni repositorios para esta actualización.

## Evidencia antes y después

- Antes: [docs/pom-original.xml](pom-original.xml), con Java 11.
- Después: [pom.xml](../pom.xml), con Java 21.

El primer commit del repositorio conserva el POM anterior; el segundo aplica Java 21 y añade la documentación de ejecución.

## Verificación

- Entorno local: Eclipse Temurin JDK 21.0.11 y Maven 3.9.11, en Windows.
- Base de prueba: MySQL 8.4 temporal.
- `clean verify`: `BUILD SUCCESS`.
- Pruebas existentes: 42 aprobadas, 0 fallos, 0 errores, 0 omitidas.
- Desglose: 41 pruebas unitarias de controladores y 1 prueba de arranque con MySQL.
- Clases de la aplicación: bytecode 65, correspondiente a Java 21.
- JAR ejecutable generado y arrancado correctamente.
- JaCoCo: reporte generado; 183 líneas cubiertas de 271, equivalentes al 67.53 %.
- Comprobación conjunta con el cliente: registro, acceso válido, respuesta con contraseña incorrecta y consulta del usuario guardado.

Se utilizaron datos ficticios. Al terminar se detuvieron las aplicaciones y se eliminó la base temporal. Posteriormente, `CompilarAPIJava21 #1` terminó en `SUCCESS` con JDK 21.0.12.1, Maven 3.9.11 y 42 pruebas aprobadas. Coverage publicó 67.53 % de cobertura de líneas.

## Plugins Maven solicitados para la práctica

Además del cambio de Java, el POM declara explícitamente estos plugins:

| Plugin | Versión | Cambio |
|---|---|---|
| sonar-maven-plugin | 5.5.0.6356 | Agregado con la versión solicitada |
| jacoco-maven-plugin | 0.8.15 | Conservado con sus ejecuciones prepare-agent y report |
| maven-surefire-plugin | 3.5.4 | Declarado explícitamente; antes se utilizaba la versión 2.22.2 heredada |

`mvn clean verify` ejecuta las pruebas con Surefire y genera el reporte HTML/XML de JaCoCo. La prueba de arranque sigue necesitando MySQL y las variables de conexión configuradas.

El plugin de Sonar queda disponible en Maven. Su incorporación no ejecuta automáticamente un análisis: todavía debe configurarse el servidor SonarQube, su URL y el token, y ejecutar `mvn sonar:sonar`. El token se debe proporcionar mediante una credencial de Jenkins o una variable de entorno, fuera del POM.

El POM original de Java 11 conserva JaCoCo tal como estaba y no se modifica para agregar Sonar ni Surefire.

La comprobación de estos plugins en una copia de validación dentro del contenedor Jenkins, con JDK 21.0.12.1, Maven 3.9.11 y MySQL, terminó en `BUILD SUCCESS`: 42 pruebas aprobadas y cobertura de 183/271 líneas (67.53 %). Maven resolvió Sonar 5.5.0.6356 mediante `help:describe`. Esta validación es independiente de `CompilarAPIJava21 #1`; vuelve a construir el trabajo para registrar el commit actualizado con los plugins.
