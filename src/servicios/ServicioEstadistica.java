package servicios;

import estructuras.ArbolIndice;
import estructuras.ListaOrdenes;
import estructuras.ListaResumen;
import estructuras.MatrizEstadistica;
import estructuras.MatrizSimetrica;
import estructuras.NodoOrden;
import modelo.Campo;
import modelo.ResumenEstadistico;

/**
 * Genera las estadísticas del caso (RF-08 a RF-10). Cada una recorre la
 * lista de órdenes (ya filtrada, RN-05) una sola vez y acumula cantidades
 * e importes en una ListaResumen (vista en lista con subtotales) o en una
 * MatrizEstadistica (vista como tabla cruzada).
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

    /** A como tabla cruzada: filas = talleres, columnas = servicios. */
    public static MatrizEstadistica matrizTallerServicio(ListaOrdenes lista) {
        return MatrizEstadistica.construir(lista, Campo.TALLER, Campo.SERVICIO);
    }

    /** B como tabla cruzada: filas = mecánicos, columnas = estados. */
    public static MatrizEstadistica matrizMecanicoEstado(ListaOrdenes lista) {
        return MatrizEstadistica.construir(lista, Campo.MECANICO, Campo.ESTADO);
    }

    /** C como tabla cruzada: filas = meses de ingreso, columnas = tipos de vehículo. */
    public static MatrizEstadistica matrizMesTipoVehiculo(ListaOrdenes lista) {
        return MatrizEstadistica.construir(lista, Campo.MES_INGRESO, Campo.TIPO_VEHICULO);
    }

    /**
     * D. Vehículos atendidos en dos talleres: la celda (i, j) es la cantidad
     * de placas con órdenes en el taller i y en el taller j; la diagonal
     * (i, i) es la cantidad de placas distintas del taller i. Como la
     * relación es simétrica, se guarda en una MatrizSimetrica.
     *
     * Recorre el ABB por placa (cada nodo tiene las órdenes de una placa) y
     * marca en un arreglo de booleanos los talleres donde se atendió.
     */
    public static MatrizSimetrica vehiculosCompartidos(ListaOrdenes lista, String[] talleres) {
        MatrizSimetrica matriz = new MatrizSimetrica(talleres.length);
        ListaOrdenes[] ordenesPorPlaca = ArbolIndice.desdeLista(lista, ArbolIndice.POR_PLACA).listasPorClave();

        for (int p = 0; p < ordenesPorPlaca.length; p++) {
            boolean[] atendida = new boolean[talleres.length];
            NodoOrden actual = ordenesPorPlaca[p].getCabeza();
            while (actual != null) {
                int t = indiceDe(talleres, valor(actual.getDato().getTaller()));
                if (t >= 0) {
                    atendida[t] = true;
                }
                actual = actual.getSiguiente();
            }
            for (int i = 0; i < talleres.length; i++) {
                for (int j = 0; j <= i; j++) {          // solo la triangular inferior
                    if (atendida[i] && atendida[j]) {
                        matriz.incrementar(i, j);
                    }
                }
            }
        }
        return matriz;
    }

    private static int indiceDe(String[] arreglo, String valor) {
        for (int i = 0; i < arreglo.length; i++) {
            if (arreglo[i].equalsIgnoreCase(valor)) {
                return i;
            }
        }
        return -1;
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
