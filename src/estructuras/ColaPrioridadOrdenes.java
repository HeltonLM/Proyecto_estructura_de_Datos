package estructuras;

import modelo.OrdenTrabajo;

/**
 * TAD ColaPrioridadOrdenes: cola con prioridad implementada con nodos.
 *
 * Arma la cola de atención del taller con las órdenes que aún no terminan:
 *   prioridad 1: Pendiente   (el vehículo todavía no se empieza a atender)
 *   prioridad 2: En proceso
 * Las finalizadas y canceladas no entran en la cola.
 *
 * Al encolar, la orden se inserta detrás de todas las de igual o mayor
 * prioridad (menor número) y, dentro de la misma prioridad, por fecha de
 * ingreso: la más antigua sale primero. Desencolar siempre retira el frente,
 * es decir, la orden más urgente, en O(1).
 */
public class ColaPrioridadOrdenes {
    public static final int SIN_PRIORIDAD = 0;
    public static final int PRIORIDAD_PENDIENTE = 1;
    public static final int PRIORIDAD_EN_PROCESO = 2;

    private NodoOrden frente;
    private int tamaño;

    /** Arma la cola recorriendo la lista y encolando solo las órdenes por atender. */
    public static ColaPrioridadOrdenes desdeLista(ListaOrdenes lista) {
        ColaPrioridadOrdenes cola = new ColaPrioridadOrdenes();
        NodoOrden actual = lista.getCabeza();
        while (actual != null) {
            cola.encolar(actual.getDato());
            actual = actual.getSiguiente();
        }
        return cola;
    }

    /** Encola según su prioridad. Devuelve false si la orden ya no requiere atención. */
    public boolean encolar(OrdenTrabajo orden) {
        if (prioridadDe(orden) == SIN_PRIORIDAD) {
            return false;
        }
        NodoOrden nuevo = new NodoOrden(orden);
        if (frente == null || vaAntes(orden, frente.getDato())) {
            nuevo.setSiguiente(frente);
            frente = nuevo;
        } else {
            NodoOrden anterior = frente;
            while (anterior.getSiguiente() != null && !vaAntes(orden, anterior.getSiguiente().getDato())) {
                anterior = anterior.getSiguiente();
            }
            nuevo.setSiguiente(anterior.getSiguiente());
            anterior.setSiguiente(nuevo);
        }
        tamaño++;
        return true;
    }

    /** Retira y devuelve la orden más urgente. */
    public OrdenTrabajo desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("No hay órdenes por atender.");
        }
        OrdenTrabajo dato = frente.getDato();
        frente = frente.getSiguiente();
        tamaño--;
        return dato;
    }

    public OrdenTrabajo verFrente() {
        return estaVacia() ? null : frente.getDato();
    }

    public boolean estaVacia() { return frente == null; }
    public int getTamaño() { return tamaño; }

    /** Filas para la tabla, en el orden en que se atenderían. Primera columna: prioridad. */
    public Object[][] aFilasTabla() {
        Object[][] filas = new Object[tamaño][];
        int i = 0;
        NodoOrden actual = frente;
        while (actual != null) {
            Object[] datos = actual.getDato().toTableRow();
            Object[] fila = new Object[datos.length + 1];
            fila[0] = i + 1;
            for (int j = 0; j < datos.length; j++) {
                fila[j + 1] = datos[j];
            }
            filas[i++] = fila;
            actual = actual.getSiguiente();
        }
        return filas;
    }

    /** Prioridad según el estado: 1 = más urgente; 0 = no entra en la cola. */
    public static int prioridadDe(OrdenTrabajo orden) {
        String e = orden.getEstado() == null ? "" : orden.getEstado().trim().toLowerCase();
        if (e.startsWith("pendiente")) {
            return PRIORIDAD_PENDIENTE;
        }
        if (e.startsWith("en proceso") || e.startsWith("en curso")) {
            return PRIORIDAD_EN_PROCESO;
        }
        return SIN_PRIORIDAD;
    }

    // true si a debe atenderse antes que b (estricto: con empate, a va detrás)
    private static boolean vaAntes(OrdenTrabajo a, OrdenTrabajo b) {
        int pa = prioridadDe(a);
        int pb = prioridadDe(b);
        if (pa != pb) {
            return pa < pb;
        }
        return a.getFechaIngresoNumero() < b.getFechaIngresoNumero();
    }
}
