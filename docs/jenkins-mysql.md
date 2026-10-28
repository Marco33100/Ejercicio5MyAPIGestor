# Pipeline de base de datos MySQL

El trabajo **Base de Datos MySQL** usa el script [database/Jenkinsfile](../database/Jenkinsfile), independiente del pipeline del backend. Las pruebas y el esquema estan en `database/` dentro de este mismo repositorio.

## Preparacion del entorno de la practica

- Servidor: contenedor `mysql-java21-ci`, MySQL 8.4, en la red `jenkins-java21-ci` compartida con Jenkins.
- Base exclusiva del pipeline: **dbgestor_pipeline_ci**.
- Credencial de Jenkins: **mysql-ci**, tipo Username with password, ya utilizada en las pruebas del backend.
- Herramientas: JDK **JDK21**, Maven **Maven3** y Semgrep 1.181.0 en `/var/jenkins_home/tools/semgrep-1.181.0/bin/semgrep`.
- Plugins: Pipeline, Git, Credentials Binding y JUnit.

En el equipo de esta practica la base nueva ya se creo y el usuario de CI tiene permisos sobre ella. Se reutiliza el servidor existente, con un esquema separado de `dbgestor_ci` y de la base de la aplicacion. No se publican puertos nuevos ni se necesita el comando Docker dentro de Jenkins.

## Configurar el trabajo

1. Abre **Base de Datos MySQL > Configurar > Pipeline**.
2. Selecciona **Pipeline script** y pega todo el contenido de [database/Jenkinsfile](../database/Jenkinsfile).
3. Mantiene activado **Use Groovy Sandbox**, guarda y pulsa **Construir ahora**.
4. Revisa la consola, los resultados JUnit y los artefactos del trabajo.

## Etapas y evidencias

1. **Descargar proyecto**: limpia exclusivamente el workspace de este trabajo y descarga `main`; asi no se publican reportes viejos si una construccion falla antes de las pruebas.
2. **Verificar herramientas y SQL**: muestra Java, Maven y el esquema; prueba las seis reglas de seguridad con casos sinteticos.
3. **Analisis de seguridad SQL**: analiza todos los SQL de `database/src/main/resources`, excluye expresamente `target`, y falla si las reglas detectan una operacion prohibida. Usa configuracion local y `--metrics=off`.
4. **Build & Test MySQL**: ejecuta `mvn clean verify` en `dir('database')`, inyectando las credenciales con Credentials Binding.
5. **Post**: publica resultados JUnit y archiva con fingerprint los JSON, el resumen de MySQL y el esquema aplicado.

La verificacion directa en el contenedor Jenkins aprobo **21 pruebas**, sin fallos, errores ni omisiones. Se comprobaron columnas, claves primarias, InnoDB, utf8mb4, idempotencia, secuencia Hibernate, CRUD de las cinco entidades, unicidad del correo, cuatro relaciones referenciales, proteccion ante borrado de un proyecto con tareas, rollback y consultas parametrizadas con un valor que contiene sintaxis SQL.

Los datos de cada prueba se revierten con rollback; tambien se comprueba que las cinco tablas de negocio esten vacias al inicio y al final. Las pruebas rechazan cualquier `DB_NAME` distinto de `dbgestor_pipeline_ci` antes de conectar. El servidor valida realmente la sintaxis del DDL al ejecutarlo. Los casos sinteticos de `database/security/reglas.sql` sirven para probar el detector y nunca se ejecutan en MySQL.

## Archivos publicados por Jenkins

- `reportes/database-rules-test.json`: resultado de las pruebas de las seis reglas.
- `reportes/database-semgrep.json`: hallazgos del esquema, rutas analizadas y exclusiones.
- `database/target/database-summary.txt`: motor, version, base y comprobacion de datos revertidos.
- `database/src/main/resources/schema.sql`: esquema versionado.
- Resultados JUnit de `database/target/surefire-reports/`.

El esquema conserva las cinco entidades y `hibernate_sequence`. Sus campos de texto y fechas siguen los modelos Java actuales; el autor de un comentario se conserva como texto. Agrega utf8mb4, correo unico y cuatro claves foraneas para validar calidad de datos. Se crea solo en la base de CI: no se ejecuta una migracion sobre los datos existentes de la aplicacion ni se restaura el dump antiguo con datos personales.

La cobertura Java al 100 % corresponde a frontend y backend. La calidad de este modulo SQL se acredita mediante las 21 pruebas contra MySQL y el escaneo de sus seis reglas. Estas reglas buscan operaciones concretas; cero hallazgos no acredita ausencia de cualquier vulnerabilidad o problema posible.

## Prepararlo en otro equipo

Un administrador del servidor MySQL debe crear `dbgestor_pipeline_ci` con utf8mb4 y conceder al usuario de la credencial `mysql-ci` permisos limitados a ese esquema. Usa cuentas y contrasenas propias; no guardes claves en el repositorio. Jenkins debe poder resolver y alcanzar `mysql-java21-ci:3306`.

La prueba directa documentada no es una ejecucion del trabajo de Jenkins. Tras pegar el script hay que construirlo para obtener su numero de ejecucion, estado y artefactos como evidencia de la rubrica.
