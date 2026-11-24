package servicios;

import estructuras.ColaImportacion;

/**
 * Resultado de leer un CSV: la cola con las órdenes válidas (en el orden del
 * archivo) y el detalle de las líneas que se omitieron por no ser válidas.
 */
public class ResultadoCarga {
    private static final int MAX_MENSAJES = 15;

    private final ColaImportacion ordenes = new ColaImportacion();
    private final String[] mensajes = new String[MAX_MENSAJES];
    private int cantidadMensajes;
    private int lineasConError;
    private String codificacion;

    public ColaImportacion getOrdenes() { return ordenes; }
    public int getLineasConError() { return lineasConError; }
    public String getCodificacion() { return codificacion; }

    void setCodificacion(String codificacion) { this.codificacion = codificacion; }

    void registrarError(int numeroLinea, String detalle) {
        lineasConError++;
        guardarMensaje("Línea " + numeroLinea + ": " + detalle);
    }

    private void guardarMensaje(String mensaje) {
        if (cantidadMensajes < MAX_MENSAJES) {
            mensajes[cantidadMensajes] = mensaje;
            cantidadMensajes++;
        }
    }

    /** Texto con los primeros problemas encontrados, para mostrar al usuario. */
    public String resumenProblemas() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cantidadMensajes; i++) {
            sb.append(mensajes[i]).append('\n');
        }
        int total = lineasConError;
        if (total > cantidadMensajes) {
            sb.append("... y ").append(total - cantidadMensajes).append(" línea(s) más con error.");
        }
        return sb.toString();
    }
}
