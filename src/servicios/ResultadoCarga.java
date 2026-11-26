package servicios;

import estructuras.ColaImportacion;
import estructuras.ColaMensajes;

/**
 * Resultado de leer un CSV: la cola con las órdenes válidas (en el orden del
 * archivo) y el detalle de las líneas que se omitieron por no ser válidas.
 * Los mensajes de error se guardan en una cola con arreglo circular: se
 * muestran en el mismo orden en que se encontraron (FIFO) y, cuando la cola
 * se llena, solo se siguen contando las líneas.
 */
public class ResultadoCarga {
    private static final int MAX_MENSAJES = 15;

    private final ColaImportacion ordenes = new ColaImportacion();
    private final ColaMensajes mensajes = new ColaMensajes(MAX_MENSAJES);
    private int lineasConError;
    private String codificacion;

    public ColaImportacion getOrdenes() { return ordenes; }
    public int getLineasConError() { return lineasConError; }
    public String getCodificacion() { return codificacion; }

    void setCodificacion(String codificacion) { this.codificacion = codificacion; }

    void registrarError(int numeroLinea, String detalle) {
        lineasConError++;
        mensajes.encolar("Línea " + numeroLinea + ": " + detalle); // si está llena, no se guarda
    }

    /** Texto con los primeros problemas encontrados, para mostrar al usuario. */
    public String resumenProblemas() {
        StringBuilder sb = new StringBuilder();
        String[] primeros = mensajes.aArreglo(); // recorre la cola sin vaciarla
        for (int i = 0; i < primeros.length; i++) {
            sb.append(primeros[i]).append('\n');
        }
        int total = lineasConError;
        if (total > primeros.length) {
            sb.append("... y ").append(total - primeros.length).append(" línea(s) más con error.");
        }
        return sb.toString();
    }
}
