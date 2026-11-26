package estructuras;

/**
 * TAD ListaCircularTalleres: lista enlazada simple circular de nombres.
 *
 * El último nodo apunta otra vez al primero, así que no hay un "final":
 * el botón "Siguiente taller" de Estadísticas avanza un nodo cada vez y,
 * después del último taller, vuelve al primero sin casos especiales.
 *
 * Solo se guarda el puntero al último nodo; el primero es ultimo.siguiente.
 * Con eso se inserta al inicio o al final en O(1).
 */
public class ListaCircularTalleres {
    private NodoTexto ultimo;
    private NodoTexto actual; // posición de la rotación
    private int tamaño;

    /** Crea la lista con los valores del arreglo, en el mismo orden. */
    public static ListaCircularTalleres desdeArreglo(String[] valores) {
        ListaCircularTalleres lista = new ListaCircularTalleres();
        for (int i = 0; i < valores.length; i++) {
            lista.insertarFinal(valores[i]);
        }
        return lista;
    }

    public void insertarFinal(String valor) {
        NodoTexto nuevo = new NodoTexto(valor);
        if (ultimo == null) {
            nuevo.setSiguiente(nuevo); // un solo nodo: se apunta a sí mismo
        } else {
            nuevo.setSiguiente(ultimo.getSiguiente());
            ultimo.setSiguiente(nuevo);
        }
        ultimo = nuevo;
        tamaño++;
    }

    public void insertarInicio(String valor) {
        if (ultimo == null) {
            insertarFinal(valor);
            return;
        }
        NodoTexto nuevo = new NodoTexto(valor);
        nuevo.setSiguiente(ultimo.getSiguiente()); // el nuevo apunta al que era primero
        ultimo.setSiguiente(nuevo);                // y el último apunta al nuevo
        tamaño++;
    }

    /** Elimina el primer nodo con ese valor. Devuelve true si lo encontró. */
    public boolean eliminar(String valor) {
        if (ultimo == null) {
            return false;
        }
        NodoTexto anterior = ultimo;
        for (int i = 0; i < tamaño; i++) {
            NodoTexto nodo = anterior.getSiguiente();
            if (nodo.getDato().equalsIgnoreCase(valor)) {
                if (tamaño == 1) {
                    ultimo = null;
                    actual = null;
                } else {
                    anterior.setSiguiente(nodo.getSiguiente());
                    if (nodo == ultimo) {
                        ultimo = anterior;
                    }
                    if (nodo == actual) {
                        actual = anterior;
                    }
                }
                tamaño--;
                return true;
            }
            anterior = nodo;
        }
        return false;
    }

    /** Búsqueda: da exactamente una vuelta (tamaño nodos) para no ciclar. */
    public boolean contiene(String valor) {
        if (ultimo == null) {
            return false;
        }
        NodoTexto nodo = ultimo.getSiguiente();
        for (int i = 0; i < tamaño; i++) {
            if (nodo.getDato().equalsIgnoreCase(valor)) {
                return true;
            }
            nodo = nodo.getSiguiente();
        }
        return false;
    }

    /**
     * Avanza la rotación y devuelve el taller al que llegó. La primera vez
     * devuelve el primero; después del último, vuelve al primero.
     */
    public String siguiente() {
        if (ultimo == null) {
            return null;
        }
        actual = (actual == null) ? ultimo.getSiguiente() : actual.getSiguiente();
        return actual.getDato();
    }

    /** Recorrido de una vuelta completa, desde el primero. */
    public String[] aArreglo() {
        String[] resultado = new String[tamaño];
        if (ultimo == null) {
            return resultado;
        }
        NodoTexto nodo = ultimo.getSiguiente();
        for (int i = 0; i < tamaño; i++) {
            resultado[i] = nodo.getDato();
            nodo = nodo.getSiguiente();
        }
        return resultado;
    }

    public int getTamaño() { return tamaño; }
    public boolean estaVacia() { return ultimo == null; }
}
