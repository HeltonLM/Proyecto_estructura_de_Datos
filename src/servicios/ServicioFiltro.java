package servicios;

import estructuras.ListaOrdenes;
import modelo.FiltroBusqueda;

/**
 * Aplica los filtros del caso (RF-07) sobre una lista y devuelve una lista
 * de resultados nueva; la lista recibida no se modifica.
 * Todos los criterios activos se evalúan en un solo recorrido.
 */
public final class ServicioFiltro {

    private ServicioFiltro() {
    }

    public static ListaOrdenes aplicar(ListaOrdenes lista, FiltroBusqueda filtro) {
        if (filtro == null || !filtro.estaActivo()) {
            return lista;
        }
        return lista.filtrar(filtro);
    }

    public static ListaOrdenes filtrarPorTaller(ListaOrdenes lista, String taller) {
        return aplicar(lista, new FiltroBusqueda(taller, null, null, null, 0, 0));
    }

    public static ListaOrdenes filtrarPorTipoVehiculo(ListaOrdenes lista, String tipo) {
        return aplicar(lista, new FiltroBusqueda(null, tipo, null, null, 0, 0));
    }

    public static ListaOrdenes filtrarPorServicio(ListaOrdenes lista, String servicio) {
        return aplicar(lista, new FiltroBusqueda(null, null, servicio, null, 0, 0));
    }

    public static ListaOrdenes filtrarPorEstado(ListaOrdenes lista, String estado) {
        return aplicar(lista, new FiltroBusqueda(null, null, null, estado, 0, 0));
    }

    /** Fechas en formato AAAAMMDD; 0 = sin límite. */
    public static ListaOrdenes filtrarPorRangoFechas(ListaOrdenes lista, int desde, int hasta) {
        return aplicar(lista, new FiltroBusqueda(null, null, null, null, desde, hasta));
    }
}
