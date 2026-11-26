# AutoService Perú: sistema de búsquedas y estadísticas (Caso 5)

Proyecto final de **Algoritmos y Estructuras de Datos** (2026-2). Es una aplicación de escritorio en Java Swing que importa las órdenes de trabajo de los talleres desde archivos CSV, las guarda en **estructuras de datos propias** y permite buscarlas, filtrarlas y generar las estadísticas que pide el caso.

Restricciones de la consigna: no se usa base de datos y no se usan colecciones de Java (`ArrayList`, `List`, `Map`, `Set`, `LinkedList`...). El código no importa nada de `java.util`.

El guion para el informe y la exposición está en [`docs/GUION_INFORME_Y_EXPOSICION.md`](docs/GUION_INFORME_Y_EXPOSICION.md).

## Cómo ejecutarlo

1. Abre la carpeta en IntelliJ IDEA (JDK 17 o superior; el proyecto está configurado con JDK 25).
2. Ejecuta `Main` (o `vistas.FrmPrincipal`).
3. En el menú **Archivo > Importar CSV...**, elige `datos/ordenes_trabajo.csv`.
4. Abre **Consultas > Búsquedas** o **Consultas > Estadísticas**.

`datos/ordenes_trabajo.csv` contiene datos de prueba: 1 500 órdenes de los tres talleres (enero a noviembre de 2026), con fechas en formato `dd/MM/yyyy`. Si cada taller tiene su propio archivo, usa **Archivo > Agregar CSV de otro taller...** para sumar sus órdenes a las ya cargadas.

## Qué pide el caso y dónde está resuelto

| Requerimiento | Implementación |
|---|---|
| RF-01 Importar CSV | `servicios.LectorCSV`: detecta `,` `;` o tabulación, respeta comillas, reconoce las columnas por nombre y acepta UTF-8 o Windows-1252 (Excel) |
| RF-02 Estructuras propias | `ColaImportacion` → `ListaOrdenes` y los árboles de búsqueda |
| RF-03 Buscar por código | `ArbolOrdenCodigo` (árbol AVL) |
| RF-04 Buscar por placa | `ArbolIndice` por placa (ABB, cada nodo guarda la lista de órdenes de esa placa) |
| RF-05 Buscar por fecha de ingreso | Búsqueda secuencial en `ListaOrdenes` |
| RF-06 Buscar por mecánico | `ArbolIndice` por mecánico; si no hay coincidencia exacta, búsqueda secuencial por parte del nombre |
| RF-07 Filtros (taller, tipo, servicio, estado, rango de fechas) | `FiltroBusqueda` + `ServicioFiltro`, con todos los criterios en un solo recorrido |
| RF-08/09/10 Estadísticas A, B y C | `ServicioEstadistica`: vista en lista con subtotales (`ListaResumen`) o tabla cruzada (`MatrizEstadistica`, casilla "Ver como tabla cruzada") |
| Servicios más solicitados | `ServicioEstadistica.rankingServicios` (ordenamiento por inserción, de mayor a menor) |
| RN-04 Costo no negativo, datos obligatorios, fechas válidas | Validaciones de `LectorCSV`: la línea inválida se omite y se informa su número |
| RN-05 Estadística después de los filtros | `FrmEstadisticas` filtra primero y calcula después |
| RN-06 Importe de órdenes finalizadas | Resumen al pie de `FrmEstadisticas` |
| Volver a la consulta anterior | `PilaHistorialFiltros` (botón "Consulta anterior") |
| Ordenar resultados | `ListaOrdenes.ordenarPorInsercion`, por fecha, costo, estado o código |
| Eliminar una orden (por ejemplo, una importada por error) | Botón "Eliminar orden seleccionada" en Búsquedas: `ServicioBusqueda.eliminarOrden` la quita de la lista, del AVL y de los dos ABB |
| Deshacer las últimas eliminaciones | `PilaEliminaciones` (pila con arreglo, últimas 10), botón "Deshacer eliminación" |
| Ver resultados en orden inverso | `ListaDobleOrdenes`: casilla "Orden inverso" recorre la lista de la cola a la cabeza |
| Estadística por cada taller, por turnos | `ListaCircularTalleres`: botón "Siguiente taller" en Estadísticas |
| Vehículos atendidos en dos talleres | Estadística D, guardada en una `MatrizSimetrica` (triangular inferior) |
| Cola de atención de órdenes no finalizadas | `ColaPrioridadOrdenes`: menú Consultas > Cola de atención (pendientes primero, luego en proceso; a igual prioridad, el ingreso más antiguo) |
| Ver el árbol de códigos | `FrmArbol`: estructura del AVL y recorridos preorden, inorden y postorden |
| Mensajes de líneas inválidas del CSV | `ColaMensajes` (cola con arreglo circular) en `ResultadoCarga` |

## Estructura del código

```
src/
├── Main.java
├── modelo/       OrdenTrabajo, FiltroBusqueda, ConsultaBusqueda, ResumenEstadistico, Campo
├── estructuras/  NodoOrden, ListaOrdenes, ColaImportacion, PilaHistorialFiltros,
│                 NodoArbol, ArbolOrdenCodigo (AVL), NodoIndice, ArbolIndice (ABB),
│                 NodoResumen, ListaResumen, MatrizEstadistica, MatrizSimetrica,
│                 PilaEliminaciones, ColaMensajes, ColaPrioridadOrdenes,
│                 NodoDoble, ListaDobleOrdenes, NodoTexto, ListaCircularTalleres
├── servicios/    LectorCSV, ResultadoCarga, ServicioBusqueda, ServicioFiltro, ServicioEstadistica
├── util/         ValidadorFecha
└── vistas/       FrmPrincipal, FrmBusqueda, FrmEstadisticas, FrmColaAtencion, FrmArbol, PnlFiltros
```

## Relación con el sílabo

| Unidad | Tema | Dónde se aplica |
|---|---|---|
| 1 | Arreglos unidimensionales: inserción ordenada con desplazamiento, búsqueda, copia, recorrido | `MatrizEstadistica` (etiquetas de filas y columnas), `LectorCSV.dividirLinea`, `ListaResumen.grupos` |
| 1 | Arreglos paralelos y bidimensionales (recorrido por filas y por columnas, totales) | `MatrizEstadistica`: `etiquetasFila`/`etiquetasColumna` + `cantidades[][]` e `importes[][]` |
| 1 | Matrices poco densas: simétrica guardada como triangular inferior en un arreglo de n(n+1)/2 | `MatrizSimetrica` (estadística D) |
| 2 | TAD y lista enlazada simple: inserción, eliminación, recorrido, búsqueda, ordenamiento | `ListaOrdenes` (`eliminar` cubre cabeza, medio y cola), `ListaResumen` |
| 2 | Lista doblemente enlazada: inserción al inicio y al final, eliminación, búsqueda, recorrido en ambos sentidos | `ListaDobleOrdenes` |
| 2 | Lista circular: inserción, eliminación, búsqueda, recorrido de una vuelta, rotación | `ListaCircularTalleres` |
| 3 | Pila con arreglo (push/pop/peek, llena y vacía) | `PilaEliminaciones` |
| 3 | Pila dinámica (push/pop) | `PilaHistorialFiltros` |
| 3 | Cola con arreglo circular (enqueue/dequeue) | `ColaMensajes` |
| 3 | Cola dinámica (enqueue/dequeue) | `ColaImportacion` |
| 3 | Cola con prioridad | `ColaPrioridadOrdenes` |
| 4 | ABB: inserción, búsqueda y eliminación (hoja, un hijo, dos hijos con sucesor inorden) | `ArbolIndice` (placa y mecánico) |
| 4 | Árbol binario: recorridos preorden, inorden y postorden | `ArbolOrdenCodigo` + `FrmArbol` (el dibujo del árbol usa preorden y el conteo de hojas, postorden) |
| 4 | AVL: inserción con rotaciones, borrado con rebalanceo | `ArbolOrdenCodigo` |

## Decisiones respecto a la primera entrega

- **Árbol AVL para el código, en lugar de un ABB simple.** El CSV viene ordenado por código (OT-00001, OT-00002, ...). Con claves ordenadas, un ABB simple degenera en una lista. El AVL mantiene la altura en O(log n): con 1 500 órdenes, la altura es 11.
- **`ArbolPlaca` y `ArbolMecanico` comparten una sola clase, `ArbolIndice`**, porque solo cambia la clave. Se crean dos instancias.
- **La "ListaResultado" es una `ListaOrdenes`.** Los resultados de búsquedas y filtros son listas nuevas, así que la lista principal nunca se modifica.
- **Todos los temas del sílabo están aplicados.** Los que el enunciado no exige (lista doble, lista circular, pila y cola con arreglo, cola con prioridad, matriz simétrica, recorridos) se usan en funciones complementarias del mismo caso.
- **Autenticación y exportación a CSV o PDF** no están incluidas: el documento las deja para avances posteriores y el caso no las pide.
