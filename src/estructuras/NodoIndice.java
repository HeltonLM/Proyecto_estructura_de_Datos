package estructuras;

/** Nodo del ArbolIndice: una clave (placa o mecánico) y sus órdenes. */
public class NodoIndice {
    private final String clave;
    private final ListaOrdenes ordenes;
    private NodoIndice izquierdo;
    private NodoIndice derecho;

    public NodoIndice(String clave) {
        this.clave = clave;
        this.ordenes = new ListaOrdenes();
    }

    public String getClave() { return clave; }
    public ListaOrdenes getOrdenes() { return ordenes; }

    public NodoIndice getIzquierdo() { return izquierdo; }
    public void setIzquierdo(NodoIndice izquierdo) { this.izquierdo = izquierdo; }

    public NodoIndice getDerecho() { return derecho; }
    public void setDerecho(NodoIndice derecho) { this.derecho = derecho; }
}
