# Cobertura Java del backend: 100 %

Verificacion realizada el 28 de octubre de 2026 con JDK 21, Maven 3.9.11, Surefire 3.5.4, JaCoCo 0.8.15 y la base MySQL de CI, dentro del contenedor Jenkins.

| Metrica | Cubiertos | Sin cubrir | Cobertura |
|---|---:|---:|---:|
| Metodos | 114 | 0 | 100 % |
| Lineas | 296 | 0 | 100 % |
| Ramas | 44 | 0 | 100 % |
| Instrucciones | 1047 | 0 | 100 % |
| Clases | 22 | 0 | 100 % |

La suite tiene **77 pruebas**, todas aprobadas, sin fallos, errores ni omisiones. No se excluyeron clases ni metodos, incluido `main`.

La verificacion incluye las [correcciones de las 23 incidencias de SonarQube](correcciones-sonarqube.md), DTO y 19 casos adicionales de contratos y seguridad.

## Correccion y pruebas

Los cinco controladores comparaban objetos mediante `objeto.equals(null)` antes de guardar, actualizar o eliminar. Con una entrada nula esa comprobacion lanzaba NullPointerException, por lo que no se alcanzaba la respuesta de rechazo. Se reemplazaron las quince comprobaciones por `objeto != null`.

`ControladoresEntradaNulaTest` comprueba que guardar, actualizar y eliminar entradas nulas devuelve 0 sin invocar ningun repositorio. Se conservan las pruebas existentes de registros validos, busquedas, login y contrato HTTP/JSON, que verifican la otra rama de las condiciones y las respuestas.

`Ejercicio5MyApiGestorMainTest` ejecuta el `main` real con la base MySQL de pruebas, inicia Spring Boot sin abrir un puerto HTTP, verifica que el controlador de usuarios exista y cierra el contexto y sus conexiones. El punto de entrada queda cubierto sin excluirlo ni simular SpringApplication.run.

## Reproducir

Configura `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` para la base de pruebas. Ejecuta `mvn --batch-mode --no-transfer-progress clean verify -Dsurefire.useFile=false`. Los reportes quedan en `target/site/jacoco/index.html` y `target/site/jacoco/jacoco.xml`.

En Jenkins, vuelve a construir **CompilarAPIJava21**. El pipeline existente descarga `main`, ejecuta las pruebas con la credencial `mysql-ci`, publica JaCoCo y envia el XML a SonarQube. La medicion anterior de SonarQube se actualiza cuando termina y se procesa ese nuevo analisis; no se cambia modificando el reporte a mano.

La verificacion directa en el contenedor Jenkins no sustituye el resultado del trabajo. Esta cobertura del 100 % tampoco significa que todos los hallazgos de seguridad, mantenibilidad o fiabilidad de SonarQube esten resueltos.
