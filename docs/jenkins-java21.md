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

Requiere JDK con nombre `JDK21` y Maven con nombre `Maven3` (3.9.11), además de los complementos Git, Pipeline, Credentials Binding, JUnit, Coverage y SonarQube Scanner for Jenkins.

1. Crea una **Nueva tarea** de tipo **Pipeline** llamada `CompilarAPIJava21`.
2. En Definition selecciona **Pipeline script**, como solicita la práctica.
3. Copia todo el contenido del [Jenkinsfile](../Jenkinsfile) y pégalo en el campo Script.
4. Mantén activada la opción **Use Groovy Sandbox**. El script contiene la URL del repositorio y la rama `main`. La credencial `mysql-ci` se utiliza para la base durante las pruebas.
5. Guarda, pulsa **Construir ahora** y revisa la consola.

La etapa **Ver Directorio** muestra `pwd`, `ls -la` y el contenido de `pom.xml` en consola. **Build & Test** ejecuta `mvn --batch-mode --no-transfer-progress clean verify -Dsurefire.useFile=false`: compila, ejecuta las pruebas, genera el JAR y el reporte de JaCoCo. La opción de Surefire muestra el resultado de las pruebas en consola. El POM está en la raíz de este repositorio.

El bloque **Build & Test** utiliza `steps { dir('.') { ... } }`. Dentro de `dir('.')` se conservan las credenciales MySQL y la ejecución `sh`. El punto selecciona el directorio de trabajo que contiene el POM.

El pipeline descarga el proyecto, muestra las versiones de herramientas, ejecuta `mvn --batch-mode --no-transfer-progress clean verify`, publica los resultados JUnit y muestra el XML de JaCoCo mediante Coverage. No omite las pruebas. La ejecución #1 de `CompilarAPIJava21` terminó en `SUCCESS`: JDK 21.0.12.1, Maven 3.9.11, 42 pruebas aprobadas sin fallos, errores ni omisiones, y cobertura de líneas de 67.53 % (183/271) publicada correctamente por Coverage.

## SonarQube Analysis

La suite actual tiene 58 pruebas aprobadas y 100 % de cobertura de metodos, lineas y ramas en la verificacion directa dentro del contenedor Jenkins. La ejecucion #1 documentada arriba conserva el resultado anterior de 42 pruebas y 67.53 %. Vuelve a construir el trabajo para importar la nueva cobertura en SonarQube; consulta la [medicion actual](cobertura-100.md) y el [detalle anterior](cobertura-api.md).

Después de compilar y publicar la cobertura, el pipeline ejecuta Sonar Maven Plugin **5.5.0.6356**. El proyecto se identifica como `Ejercicio5MyAPIGestor` y recibe el XML `target/site/jacoco/jacoco.xml` generado por `clean verify`.

En **Administrar Jenkins > System > SonarQube servers**, configura una instalación con nombre exacto **SonarServer**. Guarda un token válido de SonarQube como credencial de tipo **Secret text** y selecciónalo en esa instalación. No escribas el token en el script ni en el repositorio.

Para esta instalación con Docker Desktop, el script utiliza `http://host.docker.internal:9000`, accesible desde el contenedor Jenkins. La dirección `http://localhost:9000` dentro de Jenkins apunta al propio contenedor Jenkins. Configura también la URL del servidor en Jenkins como `http://host.docker.internal:9000`. Si cambias de entorno, actualiza tanto la URL configurada como el argumento `-Dsonar.host.url` del pipeline.

`withSonarQubeEnv('SonarServer')` proporciona la autenticación; el comando la recibe mediante la variable de entorno `SONAR_TOKEN`. El análisis requiere que SonarQube esté encendido y que el token permita analizar el proyecto o crearlo si todavía no existe. Esta etapa envía el análisis, pero no espera un Quality Gate.

## Security Scan (SAST) - Semgrep

La siguiente etapa ejecuta Semgrep **1.181.0** con `semgrep scan --config=auto`, guarda `reportes/semgrep.json` y lo publica como artefacto de la construcción. Los reportes están excluidos de Git. La configuración automática necesita conexión al registro de reglas de Semgrep y utiliza las métricas predeterminadas: esta versión rechaza combinar `--config=auto` con `--metrics=off`. Consulta los [datos de métricas de Semgrep](https://docs.semgrep.dev/metrics).

La imagen estándar de Jenkins no incluye Docker CLI ni acceso al motor Docker. Por eso se utiliza Semgrep instalado en un entorno virtual dentro de Jenkins, en lugar del `docker run` de la fotografía. El análisis se ejecuta sobre el mismo directorio que contiene el POM, usando `steps { dir('.') { sh ... } }`.

Para preparar esa herramienta en un contenedor Jenkins nuevo, ejecuta desde PowerShell:

```powershell
docker exec --user root jenkins sh -c 'apt-get update && apt-get install -y --no-install-recommends python3-venv'
docker exec jenkins python3 -m venv /var/jenkins_home/tools/semgrep-1.181.0
docker exec jenkins /var/jenkins_home/tools/semgrep-1.181.0/bin/pip install semgrep==1.181.0
docker exec jenkins /var/jenkins_home/tools/semgrep-1.181.0/bin/semgrep --version
```

El entorno virtual se conserva en `jenkins_home`. Python se instala en el contenedor: si lo recreas, instala Python nuevamente o inclúyelo en una imagen propia de Jenkins. El agente que ejecute el trabajo debe tener disponible esa misma herramienta.

`semgrep scan` genera el informe sin marcar la construcción como fallida únicamente por encontrar hallazgos; los errores de ejecución sí hacen fallar la etapa. Revisa el JSON publicado. La etapa Semgrep se ejecuta después de SonarQube, por lo que un error anterior impide llegar a ella.

Se verificó el comando directamente dentro del contenedor Jenkins sobre una copia de la API: Semgrep 1.181.0 terminó con código de salida 0, analizó 29 archivos con 169 reglas y reportó 0 hallazgos. El JSON conserva una advertencia de análisis parcial en `mvnw`, el script de Maven Wrapper. Esta prueba directa no equivale a haber ejecutado el pipeline completo con SonarQube.

Las ejecuciones exitosas de compilación documentadas arriba preceden a estas dos etapas. Una compilación aprobada por sí sola no demuestra que SonarQube o Semgrep hayan terminado correctamente. Después de actualizar el script pegado en Jenkins, vuelve a construir y revisa ambas etapas.

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
- [Configuración global de SonarQube en Jenkins](https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/ci-integration/jenkins-integration/global-setup).
- [Semgrep Community Edition e instalación](https://github.com/semgrep/semgrep).
- [Opciones de Semgrep CLI](https://docs.semgrep.dev/cli-reference).
