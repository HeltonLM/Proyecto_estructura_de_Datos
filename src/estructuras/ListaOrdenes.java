package estructuras;

import modelo.FiltroBusqueda;
import modelo.OrdenTrabajo;

/**
 * TAD ListaOrdenes: lista enlazada simple de órdenes de trabajo.
 * Se usa como lista principal (todas las órdenes del CSV) y como lista
 * de resultados de búsquedas y filtros, sin modificar la principal.
 *
 * Además de la cabeza guarda un puntero a la cola (último nodo). Con él,
 * insertar al final cuesta O(1) en lugar de recorrer toda la lista, lo que
 * importa cuando el CSV tiene muchos registros.
 */
public class ListaOrdenes {
    // Criterios de ordenamiento disponibles en el formulario de búsquedas
    public static final int SIN_ORDEN = 0;
    public static final int POR_FECHA_INGRESO = 1;
    public static final int POR_COSTO_DESCENDENTE = 2;
    public static final int POR_ESTADO = 3;
    public static final int POR_CODIGO = 4;

    private NodoOrden cabeza;
    private NodoOrden cola;
    private int tamaño;

    public ListaOrdenes() {
        this.cabeza = null;
        this.cola = null;
        this.tamaño = 0;
    }

    // Inserta una orden al final de la lista enlazada en O(1) usando la cola
    public void insertarFinal(OrdenTrabajo orden) {
        NodoOrden nuevo = new NodoOrden(orden);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.setSiguiente(nuevo);
        }
        cola = nuevo;
        tamaño++;
    }

    /**
     * Enlaza al final todos los nodos de otra lista en O(1).
     * La otra lista queda vacía para que ambas no compartan nodos.
     */
    public void concatenar(ListaOrdenes otra) {
        if (otra == null || otra == this || otra.estaVacia()) {
            return;
        }
        if (estaVacia()) {
            cabeza = otra.cabeza;
        } else {
            cola.setSiguiente(otra.cabeza);
        }
        cola = otra.cola;
        tamaño += otra.tamaño;

        otra.cabeza = null;
        otra.cola = null;
        otra.tamaño = 0;
    }

    /**
     * Elimina de la lista el nodo que guarda esa orden (la misma instancia).
     * Casos: lista vacía, nodo en la cabeza, nodo en medio y nodo en la
     * cola; en este último se actualiza también el puntero a la cola.
     * Devuelve true si la encontró y la quitó.
     */
    public boolean eliminar(OrdenTrabajo orden) {
        if (cabeza == null || orden == null) {
            return false;
        }
        if (cabeza.getDato() == orden) {           // está en la cabeza
            cabeza = cabeza.getSiguiente();
            if (cabeza == null) {
                cola = null;
            }
            tamaño--;
            return true;
        }
        NodoOrden anterior = cabeza;               // busca el nodo previo
        while (anterior.getSiguiente() != null && anterior.getSiguiente().getDato() != orden) {
            anterior = anterior.getSiguiente();
        }
        if (anterior.getSiguiente() == null) {
            return false;                          // no está en la lista
        }
        NodoOrden eliminado = anterior.getSiguiente();
        anterior.setSiguiente(eliminado.getSiguiente());
        if (eliminado == cola) {
            cola = anterior;
        }
        tamaño--;
        return true;
    }

    /** Devuelve una lista nueva con los mismos datos (nodos nuevos). */
    public ListaOrdenes copiar() {
        ListaOrdenes copia = new ListaOrdenes();
        NodoOrden actual = cabeza;
        while (actual != null) {
            copia.insertarFinal(actual.getDato());
            actual = actual.getSiguiente();
        }
        return copia;
    }

    public NodoOrden getCabeza() {
        return cabeza;
    }

    public int getTamaño() {
        return tamaño;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    // Búsqueda secuencial nodo por nodo por código de orden (devuelve la primera coincidencia)
    public OrdenTrabajo buscarPorCodigo(String codigo) {
        NodoOrden actual = cabeza;
        while (actual != null) {
            if (actual.getDato().getCodigoOrden().equalsIgnoreCase(codigo)) {
                return actual.getDato();
            }
            actual = actual.getSiguiente();
        }
        return null;
    }

    /**
     * Búsqueda secuencial por placa. Un vehículo puede tener varias órdenes,
     * así que devuelve una nueva lista con todas las coincidencias.
     * Ignora mayúsculas, espacios y guiones: "abc 123" encuentra "ABC-123".
     */
    public ListaOrdenes buscarPorPlaca(String placa) {
        String buscada = normalizarPlaca(placa);
        ListaOrdenes resultado = new ListaOrdenes();
        NodoOrden actual = cabeza;
        while (actual != null) {
            if (normalizarPlaca(actual.getDato().getPlaca()).equals(buscada)) {
                resultado.insertarFinal(actual.getDato());
            }
            actual = actual.getSiguiente();
        }
        return resultado;
    }

    /** Búsqueda secuencial de todas las órdenes ingresadas en una fecha (AAAAMMDD). */
    public ListaOrdenes buscarPorFechaIngreso(int fechaNumero) {
        ListaOrdenes resultado = new ListaOrdenes();
        NodoOrden actual = cabeza;
        while (actual != null) {
            if (actual.getDato().getFechaIngresoNumero() == fechaNumero) {
                resultado.insertarFinal(actual.getDato());
            }
            actual = actual.getSiguiente();
        }
        return resultado;
    }

    /**
     * Búsqueda secuencial por mecánico. Acepta parte del nombre
     * ("perez" encuentra "Luis Pérez") sin distinguir mayúsculas ni tildes.
     */
    public ListaOrdenes buscarPorMecanico(String nombre) {
        String buscado = normalizarTexto(nombre);
        ListaOrdenes resultado = new ListaOrdenes();
        NodoOrden actual = cabeza;
        while (actual != null) {
            if (normalizarTexto(actual.getDato().getMecanicoResponsable()).contains(buscado)) {
                resultado.insertarFinal(actual.getDato());
            }
            actual = actual.getSiguiente();
        }
        return resultado;
    }

    /** Recorre la lista y devuelve una nueva con las órdenes que cumplen el filtro. */
    public ListaOrdenes filtrar(FiltroBusqueda filtro) {
        ListaOrdenes resultado = new ListaOrdenes();
        NodoOrden actual = cabeza;
        while (actual != null) {
            if (filtro.cumple(actual.getDato())) {
                resultado.insertarFinal(actual.getDato());
            }
            actual = actual.getSiguiente();
        }
        return resultado;
    }

    /** Recorre la lista sumando el costo total de todas las órdenes. */
    public double calcularImporteTotal() {
        double total = 0;
        NodoOrden actual = cabeza;
        while (actual != null) {
            total += actual.getDato().getCostoTotal();
            actual = actual.getSiguiente();
        }
        return total;
    }

    /** Copia los datos a una matriz de filas para mostrarlos en una JTable. */
    public Object[][] aFilasTabla() {
        Object[][] filas = new Object[tamaño][];
        int i = 0;
        NodoOrden actual = cabeza;
        while (actual != null) {
            filas[i] = actual.getDato().toTableRow();
            i++;
            actual = actual.getSiguiente();
        }
        return filas;
    }

    /**
     * Ordenamiento por inserción sobre nodos. Devuelve una lista nueva
     * ordenada y no modifica la original (la lista principal conserva el
     * orden del CSV).
     *
     * Cada orden se inserta en su lugar dentro de la lista ordenada. Si
     * va al final (caso frecuente: el CSV ya viene por fecha), se enlaza
     * directamente en la cola sin recorrer. Es estable: dos órdenes
     * iguales conservan su orden original.
     */
    public ListaOrdenes ordenarPorInsercion(int criterio) {
        ListaOrdenes ordenada = new ListaOrdenes();
        NodoOrden actual = cabeza;
        while (actual != null) {
            ordenada.insertarOrdenado(actual.getDato(), criterio);
            actual = actual.getSiguiente();
        }
        return ordenada;
    }

    private void insertarOrdenado(OrdenTrabajo orden, int criterio) {
        NodoOrden nuevo = new NodoOrden(orden);
        if (cabeza == null) {                                     // lista vacía
            cabeza = nuevo;
            cola = nuevo;
        } else if (comparar(orden, cola.getDato(), criterio) >= 0) { // va al final
            cola.setSiguiente(nuevo);
            cola = nuevo;
        } else if (comparar(orden, cabeza.getDato(), criterio) < 0) { // va al inicio
            nuevo.setSiguiente(cabeza);
            cabeza = nuevo;
        } else {                                                   // va en medio
            NodoOrden anterior = cabeza;
            while (comparar(orden, anterior.getSiguiente().getDato(), criterio) >= 0) {
                anterior = anterior.getSiguiente();
            }
            nuevo.setSiguiente(anterior.getSiguiente());
            anterior.setSiguiente(nuevo);
        }
        tamaño++;
    }

    // Negativo si a va antes que b según el criterio
    private static int comparar(OrdenTrabajo a, OrdenTrabajo b, int criterio) {
        switch (criterio) {
            case POR_FECHA_INGRESO:
                return Integer.compare(a.getFechaIngresoNumero(), b.getFechaIngresoNumero());
            case POR_COSTO_DESCENDENTE:
                return Double.compare(b.getCostoTotal(), a.getCostoTotal());
            case POR_ESTADO:
                return a.getEstado().compareToIgnoreCase(b.getEstado());
            case POR_CODIGO:
                return a.getCodigoOrden().compareToIgnoreCase(b.getCodigoOrden());
            default:
                return 0; // SIN_ORDEN: conserva el orden original
        }
    }

    static String normalizarPlaca(String placa) {
        if (placa == null) {
            return "";
        }
        return placa.replace("-", "").replace(" ", "").toUpperCase();
    }

    static String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String t = texto.trim().toLowerCase();
        t = t.replace('á', 'a').replace('é', 'e').replace('í', 'i')
             .replace('ó', 'o').replace('ú', 'u').replace('ü', 'u');
        return t;
    }
}
