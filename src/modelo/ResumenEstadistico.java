package modelo;

/**
 * Un grupo de una estadística: por ejemplo, grupo = "Taller Surco" y
 * subgrupo = "Frenos", con la cantidad de órdenes y la suma de sus costos.
 */
public class ResumenEstadistico {
    private final String grupo;
    private final String subgrupo;
    private int cantidad;
    private double importeTotal;

    public ResumenEstadistico(String grupo, String subgrupo) {
        this.grupo = grupo;
        this.subgrupo = subgrupo;
        this.cantidad = 0;
        this.importeTotal = 0;
    }

    public String getGrupo() { return grupo; }
    public String getSubgrupo() { return subgrupo; }
    public int getCantidad() { return cantidad; }
    public double getImporteTotal() { return importeTotal; }

    /** Suma una orden al grupo: +1 en la cantidad y su costo al importe. */
    public void acumular(double importe) {
        cantidad++;
        importeTotal += importe;
    }

    public boolean esDelGrupo(String grupoBuscado, String subgrupoBuscado) {
        return grupo.equalsIgnoreCase(grupoBuscado) && subgrupo.equalsIgnoreCase(subgrupoBuscado);
    }
}
