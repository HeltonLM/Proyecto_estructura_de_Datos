package estructuras;

import modelo.OrdenTrabajo;

/**
 * TAD ArbolOrdenCodigo: árbol binario de búsqueda por código de orden,
 * balanceado como árbol AVL.
 *
 * Por qué AVL y no un ABB simple: el CSV suele venir ordenado por código
 * (OT-00001, OT-00002, ...). Insertar claves ya ordenadas en un ABB simple
 * lo convierte en una lista (búsqueda O(n)). El AVL se rebalancea con
 * rotaciones después de cada inserción, así su altura se mantiene en
 * O(log n): con 1 500 órdenes basta con unas 11 comparaciones.
 *
 * Los códigos se comparan sin distinguir mayúsculas. Si un código se repite
 * (RN-01), se conserva la primera orden y se cuenta el duplicado.
 */
public class ArbolOrdenCodigo {
    private NodoArbol raiz;
    private int tamaño;
    private int duplicados;

    public ArbolOrdenCodigo() {
        this.raiz = null;
        this.tamaño = 0;
        this.duplicados = 0;
    }

    /** Construye el árbol recorriendo una lista enlazada. */
    public static ArbolOrdenCodigo desdeLista(ListaOrdenes lista) {
        ArbolOrdenCodigo arbol = new ArbolOrdenCodigo();
        NodoOrden actual = lista.getCabeza();
        while (actual != null) {
            arbol.insertar(actual.getDato());
            actual = actual.getSiguiente();
        }
        return arbol;
    }

    public int getTamaño() { return tamaño; }
    public int getDuplicados() { return duplicados; }
    public int getAltura() { return altura(raiz); }
    public boolean estaVacio() { return raiz == null; }

    public void insertar(OrdenTrabajo orden) {
        raiz = insertar(raiz, orden);
    }

    // Inserción recursiva de ABB seguida del balanceo AVL al regresar
    private NodoArbol insertar(NodoArbol nodo, OrdenTrabajo orden) {
        if (nodo == null) {
            tamaño++;
            return new NodoArbol(orden);
        }

        int comparacion = compararCodigos(orden.getCodigoOrden(), nodo.getDato().getCodigoOrden());
        if (comparacion < 0) {
            nodo.setIzquierdo(insertar(nodo.getIzquierdo(), orden));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertar(nodo.getDerecho(), orden));
        } else {
            duplicados++;
            return nodo; // código repetido: no se inserta
        }

        actualizarAltura(nodo);
        return balancear(nodo);
    }

    /**
     * Elimina la orden con ese código y rebalancea el árbol.
     * Devuelve la orden eliminada o null si el código no existe.
     */
    public OrdenTrabajo eliminar(String codigo) {
        OrdenTrabajo encontrada = buscar(codigo);
        if (encontrada == null) {
            return null;
        }
        raiz = eliminar(raiz, codigo);
        tamaño--;
        return encontrada;
    }

    /**
     * Borrado en un árbol equilibrado: primero se borra como en un ABB
     * (hoja, un hijo o dos hijos) y, al regresar de la recursión, cada
     * ancestro actualiza su altura y se rebalancea con las mismas rotaciones
     * de la inserción. A diferencia de la inserción, puede necesitarse más
     * de una rotación en el camino hacia la raíz.
     */
    private NodoArbol eliminar(NodoArbol nodo, String codigo) {
        if (nodo == null) {
            return null;
        }
        int comparacion = compararCodigos(codigo, nodo.getDato().getCodigoOrden());
        if (comparacion < 0) {
            nodo.setIzquierdo(eliminar(nodo.getIzquierdo(), codigo));
        } else if (comparacion > 0) {
            nodo.setDerecho(eliminar(nodo.getDerecho(), codigo));
        } else if (nodo.getIzquierdo() == null) {     // hoja o solo hijo derecho
            return nodo.getDerecho();
        } else if (nodo.getDerecho() == null) {       // solo hijo izquierdo
            return nodo.getIzquierdo();
        } else {
            // Dos hijos: el sucesor inorden (menor del subárbol derecho)
            // ocupa su lugar y se borra de ese subárbol.
            NodoArbol sucesor = nodo.getDerecho();
            while (sucesor.getIzquierdo() != null) {
                sucesor = sucesor.getIzquierdo();
            }
            sucesor.setDerecho(eliminar(nodo.getDerecho(), sucesor.getDato().getCodigoOrden()));
            sucesor.setIzquierdo(nodo.getIzquierdo());
            nodo = sucesor;
        }

        actualizarAltura(nodo);
        return balancear(nodo);
    }

    /**
     * Inserta una orden cuyo código estaba repetido en el CSV, después de
     * eliminar la que ocupaba ese código. Ya no cuenta como duplicado.
     */
    public void reemplazarDuplicado(OrdenTrabajo orden) {
        if (buscar(orden.getCodigoOrden()) == null) {
            insertar(orden);
            duplicados--;
        }
    }

    /** Búsqueda en el ABB: en cada nodo se descarta un subárbol completo. */
    public OrdenTrabajo buscar(String codigo) {
        NodoArbol actual = raiz;
        while (actual != null) {
            int comparacion = compararCodigos(codigo, actual.getDato().getCodigoOrden());
            if (comparacion == 0) {
                return actual.getDato();
            }
            actual = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
        }
        return null;
    }

    /** Recorrido inorden: devuelve las órdenes ordenadas por código. */
    public ListaOrdenes recorrerInOrden() {
        ListaOrdenes resultado = new ListaOrdenes();
        inOrden(raiz, resultado);
        return resultado;
    }

    private void inOrden(NodoArbol nodo, ListaOrdenes resultado) {
        if (nodo == null) {
            return;
        }
        inOrden(nodo.getIzquierdo(), resultado);
        resultado.insertarFinal(nodo.getDato());
        inOrden(nodo.getDerecho(), resultado);
    }

    // ----- Balanceo AVL -----

    private NodoArbol balancear(NodoArbol nodo) {
        int factor = factorEquilibrio(nodo);

        if (factor > 1) {                                    // cargado a la izquierda
            if (factorEquilibrio(nodo.getIzquierdo()) < 0) { // caso izquierda-derecha
                nodo.setIzquierdo(rotarIzquierda(nodo.getIzquierdo()));
            }
            return rotarDerecha(nodo);                       // caso izquierda-izquierda
        }
        if (factor < -1) {                                   // cargado a la derecha
            if (factorEquilibrio(nodo.getDerecho()) > 0) {   // caso derecha-izquierda
                nodo.setDerecho(rotarDerecha(nodo.getDerecho()));
            }
            return rotarIzquierda(nodo);                     // caso derecha-derecha
        }
        return nodo;
    }

    private NodoArbol rotarDerecha(NodoArbol y) {
        NodoArbol x = y.getIzquierdo();
        NodoArbol subarbol = x.getDerecho();
        x.setDerecho(y);
        y.setIzquierdo(subarbol);
        actualizarAltura(y);
        actualizarAltura(x);
        return x;
    }

    private NodoArbol rotarIzquierda(NodoArbol x) {
        NodoArbol y = x.getDerecho();
        NodoArbol subarbol = y.getIzquierdo();
        y.setIzquierdo(x);
        x.setDerecho(subarbol);
        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    private int factorEquilibrio(NodoArbol nodo) {
        return nodo == null ? 0 : altura(nodo.getIzquierdo()) - altura(nodo.getDerecho());
    }

    private void actualizarAltura(NodoArbol nodo) {
        nodo.setAltura(1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho())));
    }

    private int altura(NodoArbol nodo) {
        return nodo == null ? 0 : nodo.getAltura();
    }

    private static int compararCodigos(String a, String b) {
        return a.trim().compareToIgnoreCase(b.trim());
    }
}
