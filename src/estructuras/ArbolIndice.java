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

    private String claveDe(OrdenTrabajo orden) {
        return normalizar(tipoClave == POR_PLACA ? orden.getPlaca() : orden.getMecanicoResponsable());
    }

    private String normalizar(String valor) {
        return tipoClave == POR_PLACA
                ? ListaOrdenes.normalizarPlaca(valor)
                : ListaOrdenes.normalizarTexto(valor);
    }
}
