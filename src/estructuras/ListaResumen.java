package estructuras;

import modelo.ResumenEstadistico;

/**
 * TAD ListaResumen: lista enlazada simple de grupos estadísticos.
 * Agrupa cantidades e importes sin usar Map: para cada orden se busca su
 * grupo de forma secuencial; si existe se acumula y, si no, se crea al final.
 * Los grupos son pocos (talleres × servicios, mecánicos × estados, ...),
 * así que la búsqueda secuencial es suficiente.
 */
public class ListaResumen {
    public static final int POR_NOMBRE = 1;              // grupo y subgrupo alfabéticamente
    public static final int POR_CANTIDAD_DESCENDENTE = 2; // del más al menos frecuente

    private NodoResumen cabeza;
    private NodoResumen cola;
    private int tamaño;

    public NodoResumen getCabeza() { return cabeza; }
    public int getTamaño() { return tamaño; }
    public boolean estaVacia() { return cabeza == null; }

    /** Busca secuencialmente el grupo (grupo, subgrupo). Devuelve null si no existe. */
    public ResumenEstadistico buscarGrupo(String grupo, String subgrupo) {
        NodoResumen actual = cabeza;
        while (actual != null) {
            if (actual.getDato().esDelGrupo(grupo, subgrupo)) {
                return actual.getDato();
            }
            actual = actual.getSiguiente();
        }
        return null;
    }

    /** Suma una orden (+1 y su importe) a su grupo, creándolo si no existe. */
    public void acumular(String grupo, String subgrupo, double importe) {
        ResumenEstadistico resumen = buscarGrupo(grupo, subgrupo);
        if (resumen == null) {
            resumen = new ResumenEstadistico(grupo, subgrupo);
            insertarFinal(new NodoResumen(resumen));
        }
        resumen.acumular(importe);
    }

    public int totalCantidad() {
        int total = 0;
        NodoResumen actual = cabeza;
        while (actual != null) {
            total += actual.getDato().getCantidad();
            actual = actual.getSiguiente();
        }
        return total;
    }

    public double totalImporte() {
        double total = 0;
        NodoResumen actual = cabeza;
        while (actual != null) {
            total += actual.getDato().getImporteTotal();
            actual = actual.getSiguiente();
        }
        return total;
    }

    /**
     * Ordena la lista por inserción, reenlazando sus propios nodos
     * (no crea nodos nuevos). Es estable.
     */
    public void ordenar(int criterio) {
        NodoResumen pendiente = cabeza;
        cabeza = null;
        cola = null;
        tamaño = 0;
        while (pendiente != null) {
            NodoResumen siguiente = pendiente.getSiguiente();
            pendiente.setSiguiente(null);
            insertarOrdenado(pendiente, criterio);
            pendiente = siguiente;
        }
    }

    /** Copia los nombres de grupo a un arreglo (útil para llenar combos). */
    public String[] grupos() {
        String[] resultado = new String[tamaño];
        int i = 0;
        NodoResumen actual = cabeza;
        while (actual != null) {
            resultado[i++] = actual.getDato().getGrupo();
            actual = actual.getSiguiente();
        }
        return resultado;
    }

    private void insertarFinal(NodoResumen nuevo) {
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.setSiguiente(nuevo);
        }
        cola = nuevo;
        tamaño++;
    }

    private void insertarOrdenado(NodoResumen nuevo, int criterio) {
        ResumenEstadistico dato = nuevo.getDato();
        if (cabeza == null || comparar(dato, cola.getDato(), criterio) >= 0) {
            insertarFinal(nuevo); // ya suma 1 al tamaño
            return;
        }
        if (comparar(dato, cabeza.getDato(), criterio) < 0) {
            nuevo.setSiguiente(cabeza);
            cabeza = nuevo;
        } else {
            NodoResumen anterior = cabeza;
            while (comparar(dato, anterior.getSiguiente().getDato(), criterio) >= 0) {
                anterior = anterior.getSiguiente();
            }
            nuevo.setSiguiente(anterior.getSiguiente());
            anterior.setSiguiente(nuevo);
        }
        tamaño++;
    }

    private static int comparar(ResumenEstadistico a, ResumenEstadistico b, int criterio) {
        if (criterio == POR_CANTIDAD_DESCENDENTE) {
            int c = Integer.compare(b.getCantidad(), a.getCantidad());
            if (c != 0) return c;
        }
        int c = a.getGrupo().compareToIgnoreCase(b.getGrupo());
        if (c != 0) return c;
        return a.getSubgrupo().compareToIgnoreCase(b.getSubgrupo());
    }
}
