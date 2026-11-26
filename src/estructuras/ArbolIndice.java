package estructuras;

import modelo.OrdenTrabajo;

/**
 * Árbol binario de búsqueda (ABB) que funciona como índice: cada nodo
 * guarda una clave y la lista de todas las órdenes con esa clave.
 * Reemplaza a un Map sin usar colecciones de Java.
 *
 * Se crean dos instancias: una por placa (ArbolPlaca del diseño; RN-02:
 * una placa puede tener varias órdenes) y otra por mecánico (ArbolMecanico;
 * RN-03: un mecánico atiende varias órdenes). Ambas comparten este código
 * porque solo cambia la clave.
 *
 * Aquí basta un ABB simple: las placas y los nombres no llegan ordenados
 * en el CSV, así que el árbol queda razonablemente equilibrado.
 */
public class ArbolIndice {
    public static final int POR_PLACA = 1;
    public static final int POR_MECANICO = 2;

    private final int tipoClave;
    private NodoIndice raiz;
    private int cantidadClaves;

    public ArbolIndice(int tipoClave) {
        this.tipoClave = tipoClave;
        this.raiz = null;
        this.cantidadClaves = 0;
    }

    /** Construye el índice recorriendo la lista principal. */
    public static ArbolIndice desdeLista(ListaOrdenes lista, int tipoClave) {
        ArbolIndice arbol = new ArbolIndice(tipoClave);
        NodoOrden actual = lista.getCabeza();
        while (actual != null) {
            arbol.insertar(actual.getDato());
            actual = actual.getSiguiente();
        }
        return arbol;
    }

    public int getCantidadClaves() { return cantidadClaves; }

    /** Inserta la orden en el nodo de su clave (lo crea si no existe). Iterativo. */
    public void insertar(OrdenTrabajo orden) {
        String clave = claveDe(orden);
        if (raiz == null) {
            raiz = new NodoIndice(clave);
            raiz.getOrdenes().insertarFinal(orden);
            cantidadClaves++;
            return;
        }

        NodoIndice actual = raiz;
        while (true) {
            int comparacion = clave.compareTo(actual.getClave());
            if (comparacion == 0) {
                actual.getOrdenes().insertarFinal(orden);
                return;
            }
            NodoIndice siguiente = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
            if (siguiente == null) {
                NodoIndice nuevo = new NodoIndice(clave);
                nuevo.getOrdenes().insertarFinal(orden);
                if (comparacion < 0) {
                    actual.setIzquierdo(nuevo);
                } else {
                    actual.setDerecho(nuevo);
                }
                cantidadClaves++;
                return;
            }
            actual = siguiente;
        }
    }

    /**
     * Busca una clave exacta. Devuelve la lista de órdenes asociadas o una
     * lista vacía si no existe. La lista devuelta es una copia, para que
     * quien la use no altere el índice.
     */
    public ListaOrdenes buscar(String valor) {
        String clave = normalizar(valor);
        NodoIndice actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.getClave());
            if (comparacion == 0) {
                return actual.getOrdenes().copiar();
            }
            actual = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
        }
        return new ListaOrdenes();
    }

    /**
     * Quita la orden del nodo de su clave. Si esa clave se queda sin
     * órdenes, el nodo se elimina del ABB. Devuelve true si la quitó.
     */
    public boolean eliminar(OrdenTrabajo orden) {
        String clave = claveDe(orden);
        NodoIndice nodo = raiz;
        while (nodo != null && !clave.equals(nodo.getClave())) {
            nodo = clave.compareTo(nodo.getClave()) < 0 ? nodo.getIzquierdo() : nodo.getDerecho();
        }
        if (nodo == null || !nodo.getOrdenes().eliminar(orden)) {
            return false;
        }
        if (nodo.getOrdenes().estaVacia()) {
            raiz = eliminarNodo(raiz, clave);
            cantidadClaves--;
        }
        return true;
    }

    /**
     * Eliminación en ABB por clave. Tres casos:
     *  - hoja: se quita;
     *  - un hijo: el hijo ocupa su lugar;
     *  - dos hijos: se reemplaza por el sucesor inorden (el menor del
     *    subárbol derecho), que luego se elimina de ese subárbol.
     */
    private NodoIndice eliminarNodo(NodoIndice nodo, String clave) {
        if (nodo == null) {
            return null;
        }
        int comparacion = clave.compareTo(nodo.getClave());
        if (comparacion < 0) {
            nodo.setIzquierdo(eliminarNodo(nodo.getIzquierdo(), clave));
            return nodo;
        }
        if (comparacion > 0) {
            nodo.setDerecho(eliminarNodo(nodo.getDerecho(), clave));
            return nodo;
        }
        if (nodo.getIzquierdo() == null) {
            return nodo.getDerecho();
        }
        if (nodo.getDerecho() == null) {
            return nodo.getIzquierdo();
        }
        NodoIndice sucesor = nodo.getDerecho();
        while (sucesor.getIzquierdo() != null) {
            sucesor = sucesor.getIzquierdo();
        }
        sucesor.setDerecho(eliminarNodo(nodo.getDerecho(), sucesor.getClave()));
        sucesor.setIzquierdo(nodo.getIzquierdo());
        return sucesor;
    }

    /**
     * Recorrido inorden que copia la lista de órdenes de cada clave a un
     * arreglo, en orden alfabético de clave.
     */
    public ListaOrdenes[] listasPorClave() {
        ListaOrdenes[] resultado = new ListaOrdenes[cantidadClaves];
        llenarInOrden(raiz, resultado, 0);
        return resultado;
    }

    // Devuelve la siguiente posición libre del arreglo
    private int llenarInOrden(NodoIndice nodo, ListaOrdenes[] destino, int posicion) {
        if (nodo == null) {
            return posicion;
        }
        posicion = llenarInOrden(nodo.getIzquierdo(), destino, posicion);
        destino[posicion++] = nodo.getOrdenes();
        return llenarInOrden(nodo.getDerecho(), destino, posicion);
    }

    private String claveDe(OrdenTrabajo orden) {
        return normalizar(tipoClave == POR_PLACA ? orden.getPlaca() : orden.getMecanicoResponsable());
    }

    private String normalizar(String valor) {
        return tipoClave == POR_PLACA
                ? ListaOrdenes.normalizarPlaca(valor)
                : ListaOrdenes.normalizarTexto(valor);
    }
}
