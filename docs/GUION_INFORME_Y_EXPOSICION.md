# Caso 5 · AutoService Perú: guion de informe y exposición

Este documento resume tres cosas: si el proyecto calza con el sílabo y con el caso, cómo funciona el programa y qué escribir y explicar en el informe y en la exposición.

---

## 1. ¿Calza con el sílabo y con el Caso 5?

Sí, con un matiz que conviene decir en voz alta en la exposición.

**Lo que pide el enunciado** (importar el CSV, buscar por código, placa, fecha y mecánico, filtrar, generar las estadísticas A, B y C después de filtrar y no usar base de datos ni colecciones de Java) está resuelto completo y es el núcleo del sistema.

**Los temas del sílabo que el enunciado no exige** se usan en *funciones complementarias* del mismo taller, no en código suelto. Ninguna es un ejercicio aislado. Aun así, su justificación no tiene el mismo peso en todos los casos:

| Función complementaria | Estructura | ¿Tiene sentido en el caso? |
|---|---|---|
| Ver resultados en orden inverso | Lista doble | **Sí.** Recorrer hacia atrás evita volver a ordenar. |
| Mensajes de líneas con error del CSV | Cola con arreglo circular | **Sí.** Los errores se muestran en el orden en que aparecen. |
| Deshacer una eliminación | Pila con arreglo | **Sí**, siempre que exista la eliminación (ver la fila siguiente). |
| Eliminar una orden | Lista, ABB y AVL (eliminación) | **Razonable.** Sirve para quitar de la consulta una orden importada por error. Solo cambia la memoria, no el CSV. |
| Cola de atención | Cola con prioridad | **Razonable.** Usa los estados reales del CSV (Pendiente y En proceso) para decidir qué atender primero. |
| Estadística D: vehículos atendidos en dos talleres | Matriz simétrica (poco densa) | **Razonable.** Mide los clientes compartidos entre sedes. Con los datos de prueba, todas las placas pasan por los tres talleres, así que la matriz sale pareja. Con datos reales sería más informativa. |
| Vista del árbol de códigos | Recorridos preorden, inorden y postorden | **Didáctica.** No es una necesidad del negocio. Sirve para demostrar que el índice AVL está balanceado. Preséntenla así. |
| Botón "Siguiente taller" | Lista circular | **La más débil.** Equivale a elegir cada taller en el filtro por turnos. Se justifica para comparar sedes una tras otra sin volver a elegir. |

**Cómo decirlo en la exposición:** "El enunciado lo resolvemos con la lista, la cola de importación, la pila de consultas, los árboles y las matrices. Para aplicar todos los temas del curso, añadimos funciones complementarias del mismo taller, cada una con una estructura distinta". Así nadie piensa que el caso pedía, por ejemplo, una lista circular.

**Temas de la primera entrega que no están:** ordenamiento por selección y por burbuja. Solo se usa el ordenamiento por inserción. Si el docente los espera, hay que agregarlos o explicar por qué se eligió inserción (ver la sección 4).

---

## 2. Cómo funciona el programa

### 2.1 Importar el CSV (RF-01, RF-02)

1. **Archivo > Importar CSV...** abre `datos/ordenes_trabajo.csv`.
2. `LectorCSV` lee línea por línea. Detecta el separador (`,` `;` o tabulación), respeta las comillas, reconoce las columnas por su nombre y acepta UTF-8 o Windows-1252 (Excel).
3. Cada fila se valida: campos obligatorios, fechas reales y costo numérico no negativo (RN-04). Una fila inválida **no detiene la carga**: se omite, y su mensaje entra en la `ColaMensajes`.
4. Cada orden válida entra en la `ColaImportacion`. Solo cuando el archivo se leyó completo, la cola se vacía hacia la `ListaOrdenes` principal. Si el archivo falla a la mitad, la lista que ya estaba cargada no queda a medias.
5. Con la lista ya armada, `ServicioBusqueda` construye tres índices: el **AVL por código**, el **ABB por placa** y el **ABB por mecánico**.
6. **Agregar CSV de otro taller...** hace lo mismo, pero suma las órdenes a las que ya estaban cargadas.

### 2.2 Búsquedas y filtros (RF-03 a RF-07)

| Búsqueda | Cómo se resuelve |
|---|---|
| Por código | En el AVL: unas 11 comparaciones para 1 500 órdenes |
| Por placa | En el ABB por placa: cada nodo guarda la lista de órdenes de esa placa (RN-02) |
| Por mecánico | En el ABB por mecánico. Si no hay coincidencia exacta, se hace una búsqueda secuencial por parte del nombre ("perez" encuentra "Luis Pérez") |
| Por fecha de ingreso | Búsqueda secuencial en la lista |

Después de la búsqueda se aplican los filtros (taller, tipo de vehículo, servicio, estado y rango de fechas) en **un solo recorrido**, y por último el ordenamiento por inserción. El resultado es una lista nueva, de modo que la lista principal nunca se modifica.

- **Consulta anterior:** cada consulta nueva apila la anterior en la `PilaHistorialFiltros`, y el botón la desapila.
- **Orden inverso:** el resultado se guarda en una `ListaDobleOrdenes`, que se recorre de la cola a la cabeza.
- **Eliminar y deshacer:** la orden se quita de la lista y de los tres árboles, y entra en la `PilaEliminaciones`. "Deshacer" la restaura.

### 2.3 Estadísticas (RF-08 a RF-10, RN-05, RN-06)

1. Primero se eligen los filtros, y la estadística se calcula **solo con las órdenes filtradas** (RN-05).
2. **A, B y C** tienen dos vistas:
   - **En lista con subtotales** (`ListaResumen`): una fila por combinación, por ejemplo "Taller Surco – Frenos – 55".
   - **Como tabla cruzada** (`MatrizEstadistica`): filas × columnas, con totales por fila y por columna.
3. **D** muestra los vehículos atendidos en dos talleres (`MatrizSimetrica`).
4. **Servicios más solicitados** es un ranking ordenado por inserción.
5. Al pie se muestran el importe total y el importe de las órdenes finalizadas (RN-06).
6. **Siguiente taller** recorre la `ListaCircularTalleres` y genera la misma estadística para cada sede.

### 2.4 Otros menús

- **Consultas > Cola de atención:** la `ColaPrioridadOrdenes` muestra primero las pendientes y luego las que están en proceso; dentro de cada grupo, la de ingreso más antiguo. "Atender siguiente" desencola la más urgente.
- **Consultas > Árbol de códigos (AVL):** dibuja el árbol (en preorden), muestra los tres recorridos e indica la altura y la cantidad de hojas (contadas en postorden).

---

## 3. Qué escribir en el informe

Esta estructura sigue lo que suele pedirse en el proyecto final. Ajústenla a la rúbrica del docente si les dio otra.

1. **Carátula:** curso (Algoritmos y Estructuras de Datos, 100000SI42), caso, integrantes, docente y fecha.
2. **Introducción:** el problema de AutoService Perú. Tres talleres generan órdenes en CSV y la empresa necesita consultarlas y obtener estadísticas sin una base de datos.
3. **Objetivos:** uno general y tres o cuatro específicos, como importar, buscar, filtrar, generar estadísticas y aplicar estructuras propias.
4. **Requerimientos:** la tabla de RF y RN del enunciado, con la clase que resuelve cada uno (está en el `README.md`).
5. **Restricciones:** sin base de datos y sin colecciones de Java (`ArrayList`, `Map`...), con todas las estructuras hechas a mano.
6. **Diseño:**
   - Los paquetes `modelo`, `estructuras`, `servicios`, `util` y `vistas`, con lo que hace cada uno.
   - Un diagrama de clases sencillo con `OrdenTrabajo`, las estructuras y los servicios.
   - El flujo de la sección 2.1.
7. **Estructuras de datos, por unidad del sílabo:** usen la tabla de la sección 4. Para cada estructura, indiquen el problema que resuelve, por qué se eligió esa y su complejidad.
8. **Algoritmos clave**, con su explicación y un fragmento de código o pseudocódigo:
   - la inserción en el AVL con las cuatro rotaciones (II, DD, ID, DI);
   - el borrado en el AVL (sucesor inorden y rebalanceo);
   - el ordenamiento por inserción sobre nodos;
   - el índice de la matriz simétrica, `i(i+1)/2 + j`;
   - la cola circular, con `(i + 1) % capacidad`.
9. **Pruebas:**
   - una tabla de casos con entrada, resultado esperado y resultado obtenido;
   - capturas de las ventanas;
   - el dato de altura 11 del AVL con 1 500 órdenes.
10. **Conclusiones:** qué aprendieron y qué mejorarían, como exportar resultados o agregar autenticación (que la primera entrega deja para después).
11. **Bibliografía (la del sílabo):**
    - Cairó, O. (2006). *Estructuras de datos*. Grupo Editorial Patria.
    - Joyanes Aguilar, L. y Zahonero Martínez, I. *Estructuras de datos en Java*. McGraw-Hill España.

---

## 4. Qué explicar de cada estructura

*n* = número de órdenes (1 500 en los datos de prueba).

| Estructura (unidad) | Problema del caso que resuelve | ¿Por qué esta y no otra? | Complejidad |
|---|---|---|---|
| `ListaOrdenes`: lista simple (U2) | Guardar todas las órdenes y los resultados | El tamaño del CSV no se conoce de antemano, y un arreglo tendría que redimensionarse. Con el puntero a la cola, insertar al final cuesta O(1) | Insertar al final: O(1). Búsqueda, filtro y eliminación: O(n). Inserción ordenada: O(n²) en el peor caso y O(n) si ya viene ordenada |
| `ColaImportacion`: cola dinámica (U3) | Pasar las órdenes del CSV a la lista | Es FIFO, así que conserva el orden del archivo, y la lista solo cambia si la lectura termina bien | Encolar y desencolar: O(1) |
| `ColaMensajes`: cola con arreglo circular (U3) | Errores de importación | Tiene un tope fijo de 15 mensajes, y el arreglo circular reutiliza las posiciones sin desplazar | O(1) |
| `PilaHistorialFiltros`: pila dinámica (U3) | Volver a la consulta anterior | La última consulta vista es la primera a la que se regresa (LIFO) | Apilar y desapilar: O(1) |
| `PilaEliminaciones`: pila con arreglo (U3) | Deshacer eliminaciones | Solo interesan las 10 últimas, y un arreglo de tamaño fijo basta | O(1). Al llenarse, descartar la más antigua cuesta O(10) |
| `ColaPrioridadOrdenes` (U3) | Qué orden atender primero | No sale la que llegó primero, sino la más urgente | Encolar: O(k) (inserción ordenada). Desencolar: O(1) |
| `ListaDobleOrdenes` (U2) | Ver resultados en orden inverso | El puntero al anterior permite recorrer hacia atrás sin volver a ordenar | Recorrido: O(n). Eliminar un nodo conocido: O(1) |
| `ListaCircularTalleres` (U2) | Recorrer los talleres por turnos | Después del último sigue el primero sin casos especiales | `siguiente()`: O(1) |
| `MatrizEstadistica`: arreglos paralelos y matriz 2D (U1) | Estadísticas A, B y C cruzadas | Una estadística "por taller y servicio" es una tabla de doble entrada. Las etiquetas van en arreglos paralelos a los índices | Construcción: O(n·(f+c)). Celda: O(1). Totales: O(f·c) |
| `MatrizSimetrica`: matriz poco densa (U1) | Estadística D | Como (i, j) = (j, i), solo se guarda la triangular inferior: n(n+1)/2 celdas en vez de n² | Acceso: O(1) |
| `ListaResumen`: lista simple (U2) | Estadísticas con subtotales | Los grupos son pocos y se acumulan sin `Map` | Acumular: O(g), con g = número de grupos |
| `ArbolIndice`: ABB (U4) | Buscar por placa y por mecánico | Placas y nombres llegan desordenados, así que el ABB queda razonablemente equilibrado. Cada nodo guarda todas las órdenes de su clave | O(h): O(log n) en promedio y O(n) en el peor caso |
| `ArbolOrdenCodigo`: AVL (U4) | Buscar por código | El CSV viene **ordenado por código**. En un ABB simple, insertar claves ordenadas lo vuelve una lista de altura 1 500. El AVL se rebalancea y queda con altura 11 | Búsqueda, inserción y borrado: O(log n) garantizado |
| Recorridos pre, in y postorden (U4) | Vista del árbol | Inorden lista los códigos ordenados, preorden dibuja la estructura y postorden cuenta las hojas | O(n) |

**¿Por qué ordenamiento por inserción?** Tiene tres ventajas:

- Es estable: dos órdenes iguales conservan su orden.
- Se adapta bien a una lista enlazada: cada nodo se enlaza en su lugar sin intercambiar datos.
- Los datos suelen llegar casi ordenados por fecha, así que la mayoría de las órdenes va al final en O(1).

Selección siempre hace O(n²) comparaciones, aunque los datos ya estén ordenados. Burbuja con corte temprano también llega a O(n) con datos ordenados, pero necesita varias pasadas e intercambios.

---

## 5. Guion de la exposición (unos 15 minutos para 5 integrantes)

| Integrante | Tema | Demostración en vivo |
|---|---|---|
| 1 | El problema, las restricciones y la arquitectura en paquetes | Abrir el programa e importar el CSV. Mostrar el mensaje con el total, las placas y los mecánicos |
| 2 | Unidad 2: lista simple, ordenamiento por inserción, lista doble y lista circular | Buscar por mecánico, ordenar por costo, marcar "Orden inverso" y usar "Siguiente taller" en Estadísticas |
| 3 | Unidad 3: cola de importación, cola circular, pila de consultas, pila de eliminaciones y cola con prioridad | Hacer dos búsquedas y pulsar "Consulta anterior". Eliminar y deshacer. Abrir la Cola de atención y pulsar "Atender siguiente" |
| 4 | Unidad 4: ABB por placa y mecánico y AVL por código, con rotaciones y borrado | Buscar por código y por placa. Abrir "Árbol de códigos" y mostrar la estructura y la altura 11 |
| 5 | Unidad 1: matrices y arreglos paralelos, y matriz simétrica. Estadísticas A, B, C y D con filtros | Generar A con un filtro, luego B y C como tabla cruzada, luego D. Cerrar con las conclusiones |

### Preguntas probables del docente

- **¿Por qué AVL y no un ABB simple para el código?** Porque los códigos llegan ordenados y el ABB simple degeneraría en una lista. Con el AVL, buscar cuesta unas 11 comparaciones en vez de hasta 1 500.
- **¿Y por qué ABB simple para la placa?** Porque las placas llegan desordenadas, así que el árbol se mantiene bajo sin necesidad de rotaciones.
- **¿Qué pasa si un código se repite en el CSV?** El AVL guarda la primera orden y cuenta el duplicado (RN-01). Si esa orden se elimina, la siguiente con el mismo código pasa a ser la que se encuentra.
- **¿Por qué una cola para importar y no insertar directo en la lista?** Para no dejar la lista a medias si el archivo falla, y para conservar el orden del archivo.
- **¿Cómo se borra un nodo con dos hijos en el AVL?** Se reemplaza por el sucesor inorden (el menor del subárbol derecho), se borra el sucesor de ese subárbol y, al volver de la recursión, cada ancestro actualiza su altura y rota si queda desbalanceado.
- **¿Usan alguna colección de Java?** No. Ningún archivo importa `java.util`.
