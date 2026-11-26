package estructuras;

import modelo.OrdenTrabajo;

/**
 * TAD PilaEliminaciones: pila (LIFO) implementada con un arreglo de tamaño fijo.
 *
 * Guarda las últimas órdenes eliminadas para poder deshacer la eliminación:
 * la última que se eliminó es la primera que se restaura.
 *
 * Si la pila está llena y se apila otra orden, se descarta la más antigua
 * (posición 0) desplazando el resto una posición a la izquierda; así la
 * pila nunca se desborda.
 */
public class PilaEliminaciones {
    public static final int CAPACIDAD = 10;

    private final OrdenTrabajo[] datos = new OrdenTrabajo[CAPACIDAD];
    private int tope = -1; // índice del elemento de la cima; -1 = pila vacía

    /** push: coloca la orden en la cima. */
    public void apilar(OrdenTrabajo orden) {
        if (estaLlena()) {
            eliminarFondo();
        }
        tope++;
        datos[tope] = orden;
    }

    /** pop: retira y devuelve la orden de la cima. */
    public OrdenTrabajo desapilar() {
        if (estaVacia()) {
            throw new IllegalStateException("No hay eliminaciones para deshacer.");
        }
        OrdenTrabajo dato = datos[tope];
        datos[tope] = null;
        tope--;
        return dato;
    }

    /** peek: devuelve la cima sin retirarla. */
    public OrdenTrabajo verCima() {
        return estaVacia() ? null : datos[tope];
    }

    public boolean estaVacia() { return tope == -1; }
    public boolean estaLlena() { return tope == CAPACIDAD - 1; }
    public int getTamaño() { return tope + 1; }

    public void vaciar() {
        for (int i = 0; i <= tope; i++) {
            datos[i] = null;
        }
        tope = -1;
    }

    // Eliminación en arreglo: quita la posición 0 desplazando los demás a la izquierda
    private void eliminarFondo() {
        for (int i = 0; i < tope; i++) {
            datos[i] = datos[i + 1];
        }
        datos[tope] = null;
        tope--;
    }
}
