package estructuras;

import modelo.ResumenEstadistico;

/** Nodo de la ListaResumen: un grupo estadístico y el siguiente nodo. */
public class NodoResumen {
    private final ResumenEstadistico dato;
    private NodoResumen siguiente;

    public NodoResumen(ResumenEstadistico dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    public ResumenEstadistico getDato() { return dato; }

    public NodoResumen getSiguiente() { return siguiente; }
    public void setSiguiente(NodoResumen siguiente) { this.siguiente = siguiente; }
}
