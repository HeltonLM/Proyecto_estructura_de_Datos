package modelo;

/**
 * Todo lo que el usuario eligió en el formulario de búsquedas: criterio y
 * valor de búsqueda, filtros y orden. Se guarda en la PilaHistorialFiltros
 * para poder volver a la consulta anterior.
 */
public class ConsultaBusqueda {
    private final int criterio;
    private final String valor;
    private final FiltroBusqueda filtro;
    private final int orden;

    public ConsultaBusqueda(int criterio, String valor, FiltroBusqueda filtro, int orden) {
        this.criterio = criterio;
        this.valor = valor;
        this.filtro = filtro;
        this.orden = orden;
    }

    public int getCriterio() { return criterio; }
    public String getValor() { return valor; }
    public FiltroBusqueda getFiltro() { return filtro; }
    public int getOrden() { return orden; }
}
