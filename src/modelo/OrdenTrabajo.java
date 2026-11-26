package modelo;

import util.ValidadorFecha;

public class OrdenTrabajo {
    private String codigoOrden;
    private String fechaIngreso;
    private String placa;
    private String tipoVehiculo;
    private String taller;
    private String servicioPrincipal;
    private String mecanicoResponsable;
    private String fechaEntrega;
    private double costoTotal;
    private String estado;

    // Fecha de ingreso convertida a AAAAMMDD para comparar y filtrar por rango
    private int fechaIngresoNumero;

    // Constructor para inicializar los 10 datos de cada orden
    public OrdenTrabajo(String codigoOrden, String fechaIngreso, String placa,
                        String tipoVehiculo, String taller, String servicioPrincipal,
                        String mecanicoResponsable, String fechaEntrega,
                        double costoTotal, String estado) {
        this.codigoOrden = codigoOrden;
        this.fechaIngreso = fechaIngreso;
        this.placa = placa;
        this.tipoVehiculo = tipoVehiculo;
        this.taller = taller;
        this.servicioPrincipal = servicioPrincipal;
        this.mecanicoResponsable = mecanicoResponsable;
        this.fechaEntrega = fechaEntrega;
        this.costoTotal = costoTotal;
        this.estado = estado;
        this.fechaIngresoNumero = ValidadorFecha.aNumero(fechaIngreso);
    }

    // Métodos Getters para consultar los datos
    public String getCodigoOrden() { return codigoOrden; }
    public String getFechaIngreso() { return fechaIngreso; }
    public String getPlaca() { return placa; }
    public String getTipoVehiculo() { return tipoVehiculo; }
    public String getTaller() { return taller; }
    public String getServicioPrincipal() { return servicioPrincipal; }
    public String getMecanicoResponsable() { return mecanicoResponsable; }
    public String getFechaEntrega() { return fechaEntrega; }
    public double getCostoTotal() { return costoTotal; }
    public String getEstado() { return estado; }

    public int getFechaIngresoNumero() { return fechaIngresoNumero; }

    public boolean tieneFechaIngresoValida() {
        return fechaIngresoNumero != ValidadorFecha.FECHA_INVALIDA;
    }

    // Mes de ingreso "AAAA-MM", usado en la estadística C
    public String getMesIngreso() {
        return ValidadorFecha.mesDe(fechaIngresoNumero);
    }

    // Convierte el objeto en una fila lista para pintarse en la tabla visual
    public Object[] toTableRow() {
        return new Object[]{
                codigoOrden, fechaIngreso, placa, tipoVehiculo, taller,
                servicioPrincipal, mecanicoResponsable, fechaEntrega,
                String.format("%.2f", costoTotal), estado
        };
    }
}
