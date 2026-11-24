package estructuras;

import modelo.OrdenTrabajo;

/** Nodo del árbol AVL: guarda una orden, sus dos hijos y su altura. */
public class NodoArbol {
    private OrdenTrabajo dato;
    private NodoArbol izquierdo;
    private NodoArbol derecho;
    private int altura;

    public NodoArbol(OrdenTrabajo dato) {
        this.dato = dato;
        this.izquierdo = null;
        this.derecho = null;
        this.altura = 1;
    }

    public OrdenTrabajo getDato() { return dato; }

    public NodoArbol getIzquierdo() { return izquierdo; }
    public void setIzquierdo(NodoArbol izquierdo) { this.izquierdo = izquierdo; }

    public NodoArbol getDerecho() { return derecho; }
    public void setDerecho(NodoArbol derecho) { this.derecho = derecho; }

    public int getAltura() { return altura; }
    public void setAltura(int altura) { this.altura = altura; }
}
