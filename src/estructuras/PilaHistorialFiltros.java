package estructuras;

import modelo.ConsultaBusqueda;

/**
 * TAD PilaHistorialFiltros: pila dinámica (LIFO) implementada con nodos.
 *
 * Cada vez que el usuario ejecuta una nueva búsqueda, la consulta anterior
 * se apila. El botón "Consulta anterior" desapila y la vuelve a aplicar.
 * Tiene un límite para no crecer sin fin: al superarlo se descarta la
 * consulta más antigua (la del fondo).
 */
public class PilaHistorialFiltros {
    private static final int LIMITE = 20;

    private NodoConsulta cima;
    private int tamaño;

    /** Nodo propio de la pila: guarda una consulta y el nodo de abajo. */
    private static class NodoConsulta {
        private final ConsultaBusqueda dato;
        private NodoConsulta abajo;

        NodoConsulta(ConsultaBusqueda dato, NodoConsulta abajo) {
            this.dato = dato;
            this.abajo = abajo;
        }
    }

    public PilaHistorialFiltros() {
        this.cima = null;
        this.tamaño = 0;
    }

    /** Coloca una consulta en la cima. O(1), salvo al recortar el fondo. */
    public void apilar(ConsultaBusqueda consulta) {
        cima = new NodoConsulta(consulta, cima);
        tamaño++;
        if (tamaño > LIMITE) {
            eliminarFondo();
        }
    }

    /** Retira y devuelve la consulta de la cima. */
    public ConsultaBusqueda desapilar() {
        if (estaVacia()) {
            throw new IllegalStateException("No hay consultas anteriores.");
        }
        ConsultaBusqueda dato = cima.dato;
        cima = cima.abajo;
        tamaño--;
        return dato;
    }

    /** Devuelve la consulta de la cima sin retirarla. */
    public ConsultaBusqueda verCima() {
        return estaVacia() ? null : cima.dato;
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int getTamaño() {
        return tamaño;
    }

    public void vaciar() {
        cima = null;
        tamaño = 0;
    }

    private void eliminarFondo() {
        NodoConsulta actual = cima;
        while (actual.abajo.abajo != null) {
            actual = actual.abajo;
        }
        actual.abajo = null;
        tamaño--;
    }
}
