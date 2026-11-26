package estructuras;

import modelo.OrdenTrabajo;

/**
 * TAD ListaDobleOrdenes: lista doblemente enlazada con cabeza y cola.
 *
 * Guarda el resultado de la última búsqueda. Como cada nodo conoce a su
 * anterior, la lista se recorre en los dos sentidos sin volver a ordenar:
 * de la cabeza a la cola muestra el orden elegido (por ejemplo, fecha
 * ascendente) y de la cola a la cabeza, el orden inverso (descendente).
 */
public class ListaDobleOrdenes {
    private NodoDoble cabeza;
    private NodoDoble cola;
    private int tamaño;

    /** Copia una lista simple conservando su orden. */
    public static ListaDobleOrdenes desdeLista(ListaOrdenes lista) {
        ListaDobleOrdenes doble = new ListaDobleOrdenes();
        NodoOrden actual = lista.getCabeza();
        while (actual != null) {
            doble.insertarFinal(actual.getDato());
            actual = actual.getSiguiente();
        }
        return doble;
    }

    public void insertarInicio(OrdenTrabajo orden) {
        NodoDoble nuevo = new NodoDoble(orden);
        if (cabeza == null) {
            cola = nuevo;
        } else {
            nuevo.setSiguiente(cabeza);
            cabeza.setAnterior(nuevo);
        }
        cabeza = nuevo;
        tamaño++;
    }

    public void insertarFinal(OrdenTrabajo orden) {
        NodoDoble nuevo = new NodoDoble(orden);
        if (cola == null) {
            cabeza = nuevo;
        } else {
            nuevo.setAnterior(cola);
            cola.setSiguiente(nuevo);
        }
        cola = nuevo;
        tamaño++;
    }

    /**
     * Elimina el nodo que guarda esa orden. Con el puntero al anterior no
     * hace falta recorrer buscando el nodo previo: se enlazan directamente
     * el anterior y el siguiente del nodo eliminado.
     */
    public boolean eliminar(OrdenTrabajo orden) {
        NodoDoble nodo = buscarNodo(orden);
        if (nodo == null) {
            return false;
        }
        if (nodo.getAnterior() == null) {
            cabeza = nodo.getSiguiente();
        } else {
            nodo.getAnterior().setSiguiente(nodo.getSiguiente());
        }
        if (nodo.getSiguiente() == null) {
            cola = nodo.getAnterior();
        } else {
            nodo.getSiguiente().setAnterior(nodo.getAnterior());
        }
        tamaño--;
        return true;
    }

    /** Búsqueda secuencial por código desde la cabeza. */
    public OrdenTrabajo buscarPorCodigo(String codigo) {
        NodoDoble actual = cabeza;
        while (actual != null) {
            if (actual.getDato().getCodigoOrden().equalsIgnoreCase(codigo)) {
                return actual.getDato();
            }
            actual = actual.getSiguiente();
        }
        return null;
    }

    public NodoDoble getCabeza() { return cabeza; }
    public NodoDoble getCola() { return cola; }
    public int getTamaño() { return tamaño; }
    public boolean estaVacia() { return cabeza == null; }

    public double calcularImporteTotal() {
        double total = 0;
        NodoDoble actual = cabeza;
        while (actual != null) {
            total += actual.getDato().getCostoTotal();
            actual = actual.getSiguiente();
        }
        return total;
    }

    /**
     * Filas para la tabla. Con inverso = false recorre hacia adelante
     * (siguiente); con inverso = true recorre hacia atrás (anterior).
     */
    public Object[][] aFilasTabla(boolean inverso) {
        Object[][] filas = new Object[tamaño][];
        int i = 0;
        NodoDoble actual = inverso ? cola : cabeza;
        while (actual != null) {
            filas[i++] = actual.getDato().toTableRow();
            actual = inverso ? actual.getAnterior() : actual.getSiguiente();
        }
        return filas;
    }

    private NodoDoble buscarNodo(OrdenTrabajo orden) {
        NodoDoble actual = cabeza;
        while (actual != null && actual.getDato() != orden) {
            actual = actual.getSiguiente();
        }
        return actual;
    }
}
