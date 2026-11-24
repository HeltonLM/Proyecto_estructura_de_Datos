package modelo;

/**
 * Criterios de filtrado (RF-07): taller, tipo de vehículo, servicio, estado
 * y rango de fechas de ingreso. Un criterio vacío (null o "") significa
 * "no filtrar por ese campo". Las fechas van en formato AAAAMMDD; 0 = sin límite.
 */
public class FiltroBusqueda {
    private final String taller;
    private final String tipoVehiculo;
    private final String servicio;
    private final String estado;
    private final int fechaDesde;
    private final int fechaHasta;

    public FiltroBusqueda(String taller, String tipoVehiculo, String servicio,
                          String estado, int fechaDesde, int fechaHasta) {
        this.taller = taller;
        this.tipoVehiculo = tipoVehiculo;
        this.servicio = servicio;
        this.estado = estado;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    /** Filtro que deja pasar todas las órdenes. */
    public static FiltroBusqueda sinFiltro() {
        return new FiltroBusqueda(null, null, null, null, 0, 0);
    }

    public String getTaller() { return taller; }
    public String getTipoVehiculo() { return tipoVehiculo; }
    public String getServicio() { return servicio; }
    public String getEstado() { return estado; }
    public int getFechaDesde() { return fechaDesde; }
    public int getFechaHasta() { return fechaHasta; }

    /** Indica si al menos un criterio está activo. */
    public boolean estaActivo() {
        return tieneValor(taller) || tieneValor(tipoVehiculo) || tieneValor(servicio)
                || tieneValor(estado) || fechaDesde > 0 || fechaHasta > 0;
    }

    /** Devuelve true si la orden cumple todos los criterios activos. */
    public boolean cumple(OrdenTrabajo orden) {
        if (!coincide(taller, orden.getTaller())) return false;
        if (!coincide(tipoVehiculo, orden.getTipoVehiculo())) return false;
        if (!coincide(servicio, orden.getServicioPrincipal())) return false;
        if (!coincide(estado, orden.getEstado())) return false;

        if (fechaDesde > 0 || fechaHasta > 0) {
            if (!orden.tieneFechaIngresoValida()) return false;
            int fecha = orden.getFechaIngresoNumero();
            if (fechaDesde > 0 && fecha < fechaDesde) return false;
            if (fechaHasta > 0 && fecha > fechaHasta) return false;
        }
        return true;
    }

    private static boolean coincide(String criterio, String valor) {
        return !tieneValor(criterio) || criterio.equalsIgnoreCase(valor);
    }

    private static boolean tieneValor(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }
}
