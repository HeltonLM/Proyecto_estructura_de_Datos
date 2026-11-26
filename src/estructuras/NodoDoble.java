package estructuras;

import modelo.OrdenTrabajo;

/** Nodo de la lista doblemente enlazada: apunta al anterior y al siguiente. */
public class NodoDoble {
    private final OrdenTrabajo dato;
    private NodoDoble anterior;
    private NodoDoble siguiente;

    public NodoDoble(OrdenTrabajo dato) {
        this.dato = dato;
    }

    public OrdenTrabajo getDato() { return dato; }

    public NodoDoble getAnterior() { return anterior; }
    public void setAnterior(NodoDoble anterior) { this.anterior = anterior; }

    public NodoDoble getSiguiente() { return siguiente; }
    public void setSiguiente(NodoDoble siguiente) { this.siguiente = siguiente; }
}
