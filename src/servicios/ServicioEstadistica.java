package servicios;

import estructuras.ListaOrdenes;
import estructuras.ListaResumen;
import estructuras.NodoOrden;
import modelo.Campo;
import modelo.ResumenEstadistico;

/**
 * Genera las estadísticas del caso (RF-08 a RF-10). Cada una recorre la
 * lista de órdenes (ya filtrada, RN-05) una sola vez y acumula cantidades
 * e importes en una ListaResumen.
 */
public final class ServicioEstadistica {

    private ServicioEstadistica() {
    }

    /** A. Cantidad de órdenes agrupadas por taller y tipo de servicio. */
    public static ListaResumen estadisticaTallerServicio(ListaOrdenes lista) {
        return agrupar(lista, Campo.TALLER, Campo.SERVICIO);
    }

    /** B. Número de órdenes e importe total agrupados por mecánico y estado. */
    public static ListaResumen estadisticaMecanicoEstado(ListaOrdenes lista) {
        return agrupar(lista, Campo.MECANICO, Campo.ESTADO);
    }

    /** C. Cantidad de servicios e importe total por mes de ingreso y tipo de vehículo. */
    public static ListaResumen estadisticaMesTipoVehiculo(ListaOrdenes lista) {
        return agrupar(lista, Campo.MES_INGRESO, Campo.TIPO_VEHICULO);
    }

    /** Servicios ordenados del más al menos solicitado. */
    public static ListaResumen rankingServicios(ListaOrdenes lista) {
        ListaResumen resumen = agrupar(lista, Campo.SERVICIO, null);
        resumen.ordenar(ListaResumen.POR_CANTIDAD_DESCENDENTE);
        return resumen;
    }

    /**
     * Agrupa por uno o dos campos y deja el resultado ordenado
     * alfabéticamente. Si campoSubgrupo es null, se agrupa solo por el primero.
     */
    public static ListaResumen agrupar(ListaOrdenes lista, Campo campoGrupo, Campo campoSubgrupo) {
        ListaResumen resumen = new ListaResumen();
        NodoOrden actual = lista.getCabeza();
        while (actual != null) {
            String grupo = valor(campoGrupo.valorDe(actual.getDato()));
            String subgrupo = campoSubgrupo == null ? "" : valor(campoSubgrupo.valorDe(actual.getDato()));
            resumen.acumular(grupo, subgrupo, actual.getDato().getCostoTotal());
            actual = actual.getSiguiente();
        }
        resumen.ordenar(ListaResumen.POR_NOMBRE);
        return resumen;
    }

    /** Valores distintos de un campo, ordenados (para llenar los combos de filtros). */
    public static String[] valoresDistintos(ListaOrdenes lista, Campo campo) {
        return agrupar(lista, campo, null).grupos();
    }

    /**
     * RN-06: cantidad e importe de las órdenes finalizadas. Se considera
     * finalizada toda orden cuyo estado empiece con "finaliz" o "termin"
     * (Finalizada, FINALIZADO, Terminada...).
     */
    public static ResumenEstadistico resumenFinalizadas(ListaOrdenes lista) {
        ResumenEstadistico resumen = new ResumenEstadistico("Finalizadas", "");
        NodoOrden actual = lista.getCabeza();
        while (actual != null) {
            if (esFinalizada(actual.getDato().getEstado())) {
                resumen.acumular(actual.getDato().getCostoTotal());
            }
            actual = actual.getSiguiente();
        }
        return resumen;
    }

    public static boolean esFinalizada(String estado) {
        if (estado == null) {
            return false;
        }
        String e = estado.trim().toLowerCase();
        return e.startsWith("finaliz") || e.startsWith("termin");
    }

    private static String valor(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "(Sin dato)";
        }
        return texto.trim();
    }
}
