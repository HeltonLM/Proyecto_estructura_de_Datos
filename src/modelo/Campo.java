package modelo;

/** Campos de una orden por los que se pueden agrupar las estadísticas. */
public enum Campo {
    TALLER("Taller"),
    SERVICIO("Servicio"),
    MECANICO("Mecánico"),
    ESTADO("Estado"),
    MES_INGRESO("Mes de ingreso"),
    TIPO_VEHICULO("Tipo de vehículo");

    private final String etiqueta;

    Campo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Devuelve el valor de este campo en una orden. */
    public String valorDe(OrdenTrabajo orden) {
        switch (this) {
            case TALLER:        return orden.getTaller();
            case SERVICIO:      return orden.getServicioPrincipal();
            case MECANICO:      return orden.getMecanicoResponsable();
            case ESTADO:        return orden.getEstado();
            case MES_INGRESO:   return orden.getMesIngreso();
            case TIPO_VEHICULO: return orden.getTipoVehiculo();
            default:            return "";
        }
    }
}
