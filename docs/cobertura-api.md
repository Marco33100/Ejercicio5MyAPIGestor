# Cobertura de la API superior al 80 %

Se agregaron diez pruebas HTTP en `ContratoJsonApiTest`, ademas de las 42 pruebas existentes. Usan MockMvc con los controladores reales y repositorios simulados para comprobar el contrato JSON que consume el cliente.

Para proyectos, tareas, asignaciones, comentarios y usuarios se verifica que las actualizaciones entreguen al repositorio todos los datos recibidos. Tambien se comprueba que las consultas por identificador usen el parametro HTTP correcto y devuelvan los campos esperados. La prueba del perfil de usuario no exige devolver la contrasena en la respuesta.

## Resultado verificado

Se ejecuto `mvn --batch-mode --no-transfer-progress clean verify -Dsurefire.useFile=false` dentro del contenedor Jenkins, con JDK 21, Maven 3.9.11 y la base MySQL de pruebas.

| Medida | Antes | Despues |
|---|---:|---:|
| Pruebas aprobadas | 42 | 52 |
| Lineas cubiertas | 183/271 | 269/271 |
| Cobertura de lineas JaCoCo | 67.53 % | 99.26 % |
| Ramas cubiertas | 27/42 | 27/42 |
| Cobertura de ramas | 64.29 % | 64.29 % |
| Cobertura combinada calculada | 67.09 % | 94.57 % |

La compilacion termino en `BUILD SUCCESS`, con cero fallos, errores y pruebas omitidas. Las dos lineas sin cubrir corresponden al metodo `main`. No se agregaron exclusiones de cobertura.

SonarQube distingue la cobertura de lineas de la cobertura general, que combina lineas y condiciones: consulta la [definicion oficial de metricas](https://docs.sonarsource.com/sonarqube-server/user-guide/code-metrics/metrics-definition). El 94.57 % se calcula a partir de los contadores de JaCoCo: `(269 + 27) / (271 + 42) * 100`. Es el resultado esperado para Overall Code tras importar este reporte; todavia debe confirmarse ejecutando el analisis en Jenkins. La cobertura de ramas por separado sigue en 64.29 %.

## Actualizar Jenkins y SonarQube

1. En Jenkins abre `CompilarAPIJava21` y pulsa **Construir ahora**. El pipeline ya descarga la rama `main`; no es necesario sustituir su script por este cambio de pruebas.
2. Comprueba que aparezcan 52 pruebas aprobadas y una cobertura de lineas de 99.26 %.
3. Espera que termine `SonarQube Analysis` y que el servidor procese el reporte. Actualiza la vista **Overall Code** de `Ejercicio5MyAPIGestor` en SonarQube.

Esta mejora corresponde al backend. El proyecto cliente tiene su propio reporte de cobertura.
