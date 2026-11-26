package estructuras;

/** Nodo de la lista circular: un texto y el siguiente nodo. */
public class NodoTexto {
    private final String dato;
    private NodoTexto siguiente;

    public NodoTexto(String dato) {
        this.dato = dato;
    }

    public String getDato() { return dato; }

    public NodoTexto getSiguiente() { return siguiente; }
    public void setSiguiente(NodoTexto siguiente) { this.siguiente = siguiente; }
}
