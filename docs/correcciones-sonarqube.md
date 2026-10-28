# Atención de las 23 incidencias de SonarQube

Fecha: 28 de octubre de 2026.

## Evidencia inicial y clasificación

Las capturas del análisis de la API muestran 23 incidencias abiertas: 18 de la
regla `java:S4684` (seguridad, alta) y 5 de `java:S6813` (fiabilidad y
mantenibilidad, media). Una misma incidencia puede afectar varias cualidades;
los contadores de cualidades no se suman para obtener el total.

| Controlador | S4684: operaciones señaladas | Cantidad | S6813 |
| --- | --- | ---: | ---: |
| Asignaciones | Agregar, actualizar, eliminar | 3 | 1 |
| Comentarios | Consultar por tarea, agregar, actualizar, eliminar | 4 | 1 |
| Proyectos | Agregar, actualizar, eliminar | 3 | 1 |
| Tareas | Consultar por proyecto, agregar, actualizar, eliminar | 4 | 1 |
| Usuario | Login, agregar, actualizar, eliminar | 4 | 1 |
| **Total** | | **18** | **5** |

`S4684` detecta que una entidad JPA se recibe directamente como entrada HTTP.
En las altas y actualizaciones, guardar sin un mapeo explícito permite que los
campos enviados por el cliente lleguen a la persistencia. Es especialmente
relevante para IDs de altas y para el rol de usuario.

Las consultas, el login y los borrados se clasifican como endurecimiento del
contrato de entrada: la recepción de una entidad por sí sola no demuestra una
modificación arbitraria de campos, y esas operaciones no guardaban el objeto
recibido. Se corrigen igualmente con entradas limitadas a lo que necesitan.
No se marcaron alertas como falsos positivos ni se desactivaron reglas.

`S6813` es una mejora de diseño y comprobabilidad: los repositorios se
inyectaban en campos con `@Autowired`. No es una vulnerabilidad de asignación
masiva.

## Cambios aplicados

1. Los 18 parámetros `@RequestBody` usan DTO independientes de JPA en `dto/`.
   Se implementaron como records de Java 21, con nombres JSON explícitos para
   conservar los campos que utiliza el cliente.
2. Las altas y actualizaciones construyen la entidad mediante una lista
   explícita de campos; no se usa copia genérica de propiedades del request.
3. Las cinco altas ignoran el ID recibido y dejan su generación a JPA.
4. El registro de usuario asigna el rol **Estudiante** desde el servidor. El
   JSON ya no puede elegir un rol, aunque el formulario antiguo lo envíe.
5. La actualización de perfil conserva el rol que existe en la base. Si el
   usuario no existe, devuelve `0` sin crear otra cuenta. El cambio de roles
   requiere un flujo administrativo autorizado, fuera de estas operaciones.
6. Los borrados reciben DTO de identificador y llaman a `deleteById`. Los
   campos adicionales no participan en el borrado. Para IDs inexistentes,
   se aplica el comportamiento de `deleteById` de Spring Data JPA.
7. La consulta de tareas recibe solo `id_proyecto`; la de comentarios,
   `id_tarea`; el login, correo y contraseña. Se ignoran campos adicionales
   para mantener compatibilidad con el JSON completo del cliente anterior.
8. Los cinco repositorios son campos `private final`, recibidos por el único
   constructor del controlador. Se retiró la inyección en campos.

Las rutas, métodos HTTP y nombres de campos existentes se conservan. Los
cambios deliberados de comportamiento son la protección de IDs en altas,
el control del rol en servidor, la actualización de usuarios inexistentes y
el uso de `deleteById` descritos arriba. No se modificó el pipeline de MySQL.

## Verificación realizada

Se compiló con Java 21 y Maven 3.9.11 en una carpeta de validación del contenedor
Jenkins, conectada a MySQL de CI, mediante:

```sh
mvn --batch-mode --no-transfer-progress clean verify -Dsurefire.useFile=false
```

- **BUILD SUCCESS: 77 pruebas, 0 fallos, 0 errores y 0 omitidas.**
- JaCoCo: **114/114 métodos, 296/296 líneas, 44/44 ramas y 22/22 clases**.
- Cobertura Java de métodos, líneas y ramas: **100 %**, sin exclusiones.
- Se conservaron las pruebas existentes, adaptándolas a los DTO y a la
  comparación de los datos guardados.
- Se añadieron 19 casos: 5 altas con IDs impuestos, 5 borrados con campos
  adicionales, 5 contratos JSON de modelos, protección del rol al actualizar,
  actualización de usuario inexistente, filtros y login con campos adicionales.
- Semgrep: **166 reglas, 29 archivos analizados, 0 hallazgos, 0 errores**;
  se excluyeron explícitamente `target/**` y `**/target/**`.

La cobertura mide ejecución de Java, no la calidad completa de cada aserción
ni la cobertura visual del navegador. Los records tienen métodos generados
que JaCoCo filtra automáticamente; no se añadieron exclusiones manuales.

## Comprobación pendiente en el trabajo de Jenkins

Las correcciones están implementadas y probadas, pero el resultado de Semgrep
no sustituye a SonarQube: son analizadores diferentes. Ejecutar nuevamente el
trabajo **CompilarAPIJava21** que descarga `main`, manteniendo su Pipeline
script actual. No es necesario cambiar el script para estas correcciones.

Después de que termine el procesamiento del análisis en SonarQube:

1. Comprobar la revisión Git analizada y los resultados de las 77 pruebas.
2. Guardar el reporte JaCoCo y su cobertura de métodos.
3. Revisar `Issues`, incluidas las incidencias cerradas, y confirmar el estado
   de las 18 `S4684` y las 5 `S6813`.
4. Adjuntar la captura inicial y el resultado posterior. No afirmar que todas
   las incidencias están cerradas antes de comprobar ese nuevo análisis.

Esta corrección no implementa autenticación/autorización completa ni cambia
el almacenamiento o exposición de contraseñas del sistema heredado. Los DTO
limitan la entrada; no sustituyen esos controles ni garantizan ausencia de
otras vulnerabilidades.

## Referencias

- [SonarSource: java:S4684](https://rules.sonarsource.com/java/RSPEC-4684/)
- [SonarSource: java:S6813](https://rules.sonarsource.com/java/RSPEC-6813/)
- [OWASP: Mass Assignment Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Mass_Assignment_Cheat_Sheet.html)
