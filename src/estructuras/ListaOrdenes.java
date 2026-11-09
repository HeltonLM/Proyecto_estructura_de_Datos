package estructuras;

import modelo.OrdenTrabajo;

public class ListaOrdenes {
    private NodoOrden cabeza;
    private int tamaño;

    public ListaOrdenes() {
        this.cabeza = null;
        this.tamaño = 0;
    }

    // Inserta una orden al final de la lista enlazada
    public void insertarFinal(OrdenTrabajo orden) {
        NodoOrden nuevo = new NodoOrden(orden);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            NodoOrden actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevo);
        }
        tamaño++;
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

    // Búsqueda secuencial nodo por nodo por código de orden
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
}