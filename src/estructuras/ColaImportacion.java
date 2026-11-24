package estructuras;

import modelo.OrdenTrabajo;

/**
 * TAD ColaImportacion: cola dinámica (FIFO) implementada con nodos.
 *
 * El LectorCSV encola cada orden válida en el mismo orden en que la lee.
 * Solo cuando el archivo se leyó completo, las órdenes se desencolan hacia
 * la lista principal. Así, si la lectura falla a mitad de archivo, la lista
 * que el usuario ya tenía cargada no queda a medias.
 */
public class ColaImportacion {
    private NodoOrden frente;
    private NodoOrden fin;
    private int tamaño;

    public ColaImportacion() {
        this.frente = null;
        this.fin = null;
        this.tamaño = 0;
    }

    /** Agrega una orden al final de la cola. O(1). */
    public void encolar(OrdenTrabajo orden) {
        NodoOrden nuevo = new NodoOrden(orden);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        tamaño++;
    }

    /** Retira y devuelve la primera orden de la cola. O(1). */
    public OrdenTrabajo desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola de importación está vacía.");
        }
        OrdenTrabajo dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) {
            fin = null;
        }
        tamaño--;
        return dato;
    }

    public OrdenTrabajo verFrente() {
        return estaVacia() ? null : frente.getDato();
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamaño() {
        return tamaño;
    }

    /** Vacía la cola pasando cada orden, en orden de llegada, al final de la lista. */
    public void transferirA(ListaOrdenes lista) {
        while (!estaVacia()) {
            lista.insertarFinal(desencolar());
        }
    }
}
