# Compilar la API con Java 21 en Jenkins

## Entorno preparado para esta práctica

- Jenkins se ejecuta en el contenedor `jenkins`.
- MySQL 8.4 se ejecuta en `mysql-java21-ci`.
- Ambos están conectados a la red Docker `jenkins-java21-ci`.
- La base de pruebas es `dbgestor_ci`, con usuario `gestor_ci`.
- Los datos de MySQL se conservan en el volumen `mysql-java21-ci-data`. El contenedor tiene reinicio `unless-stopped`; debe permanecer encendido para construir la API.
- El puerto MySQL está disponible dentro de la red Docker, sin publicar un puerto en Windows.

La contraseña se guarda localmente y debe registrarse como credencial de Jenkins; no forma parte del repositorio.

## Credencial de Jenkins

En **Administrar Jenkins > Credentials > System > Global credentials (unrestricted) > Add Credentials**, registra:

| Campo | Valor |
|---|---|
| Kind | Username with password |
| Scope | Global |
| Username | gestor_ci |
| Password | Contraseña local del usuario de pruebas |
| ID | mysql-ci |
| Description | MySQL de pruebas para API Java 21 |

El ID debe coincidir exactamente con `mysql-ci`. El pipeline utiliza Credentials Binding para proporcionar `DB_USER` y `DB_PASSWORD` al proceso de Maven.

## Trabajo de Jenkins

Requiere JDK con nombre `JDK21` y Maven con nombre `Maven3` (3.9.11), además de los complementos Git, Pipeline, Credentials Binding, JUnit y Coverage.

1. Crea una **Nueva tarea** de tipo **Pipeline** llamada `CompilarAPIJava21`.
2. En Definition selecciona **Pipeline script**, como solicita la práctica.
3. Copia todo el contenido del [Jenkinsfile](../Jenkinsfile) y pégalo en el campo Script.
4. Mantén activada la opción **Use Groovy Sandbox**. El script contiene la URL del repositorio y la rama `main`. La credencial `mysql-ci` se utiliza para la base durante las pruebas.
5. Guarda, pulsa **Construir ahora** y revisa la consola.

La etapa **Ver Directorio** muestra `pwd`, `ls -la` y el contenido de `pom.xml` en consola. **Build & Test** ejecuta `mvn --batch-mode --no-transfer-progress clean verify -Dsurefire.useFile=false`: compila, ejecuta las pruebas, genera el JAR y el reporte de JaCoCo. La opción de Surefire muestra el resultado de las pruebas en consola. El POM está en la raíz de este repositorio.

El pipeline descarga el proyecto, muestra las versiones de herramientas, ejecuta `mvn --batch-mode --no-transfer-progress clean verify`, publica los resultados JUnit y muestra el XML de JaCoCo mediante Coverage. No omite las pruebas. La ejecución #1 de `CompilarAPIJava21` terminó en `SUCCESS`: JDK 21.0.12.1, Maven 3.9.11, 42 pruebas aprobadas sin fallos, errores ni omisiones, y cobertura de líneas de 67.53 % (183/271) publicada correctamente por Coverage.

## Crear el entorno en otro equipo

Con Docker Desktop funcionando y un contenedor Jenkins llamado `jenkins`, estos comandos de PowerShell crean el entorno cuando sus nombres todavía no existen:

```powershell
docker network create jenkins-java21-ci
docker network connect jenkins-java21-ci jenkins
docker volume create mysql-java21-ci-data

$env:MYSQL_DATABASE = 'dbgestor_ci'
$env:MYSQL_USER = 'gestor_ci'
$env:MYSQL_PASSWORD = [guid]::NewGuid().ToString('N')
$env:MYSQL_ROOT_PASSWORD = [guid]::NewGuid().ToString('N')

docker run --detach --name mysql-java21-ci --restart unless-stopped --network jenkins-java21-ci --mount type=volume,source=mysql-java21-ci-data,target=/var/lib/mysql --env MYSQL_DATABASE --env MYSQL_USER --env MYSQL_PASSWORD --env MYSQL_ROOT_PASSWORD mysql:8.4

# Copiar este valor al campo Password de la credencial mysql-ci.
$env:MYSQL_PASSWORD
```

Espera a que MySQL termine de inicializarse antes de construir el trabajo. Para ver su estado utiliza `docker logs mysql-java21-ci`. El contenedor crea la base y el usuario en el primer arranque con un volumen vacío; Hibernate crea las tablas durante la prueba de arranque.

Si el contenedor ya existe en el equipo de la práctica, basta con iniciarlo desde Docker Desktop o ejecutar `docker start mysql-java21-ci` y usar la contraseña local guardada. No vuelvas a ejecutar el bloque de creación con otras contraseñas sobre el volumen existente.

## Referencias

- [Credenciales en pipelines de Jenkins](https://www.jenkins.io/doc/pipeline/steps/credentials-binding/).
- [Publicación de cobertura](https://www.jenkins.io/doc/pipeline/steps/coverage/).
- [Imagen oficial de MySQL](https://hub.docker.com/_/mysql).
