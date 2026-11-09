package estructuras;

import modelo.OrdenTrabajo;

public class NodoOrden {
    private OrdenTrabajo dato;
    private NodoOrden siguiente;

    public NodoOrden(OrdenTrabajo dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    public OrdenTrabajo getDato() { return dato; }
    public void setDato(OrdenTrabajo dato) { this.dato = dato; }

    public NodoOrden getSiguiente() { return siguiente; }
    public void setSiguiente(NodoOrden siguiente) { this.siguiente = siguiente; }
}