package servicios;

import estructuras.ArbolIndice;
import estructuras.ArbolOrdenCodigo;
import estructuras.ListaOrdenes;
import modelo.ConsultaBusqueda;
import modelo.OrdenTrabajo;
import util.ValidadorFecha;

/**
 * Resuelve las búsquedas del caso (RF-03 a RF-06) sobre la lista principal
 * y sus índices:
 *   código   -> ArbolOrdenCodigo (AVL)
 *   placa    -> ArbolIndice por placa (ABB)
 *   mecánico -> ArbolIndice por mecánico (ABB); si no hay coincidencia
 *               exacta, búsqueda secuencial por parte del nombre
 *   fecha    -> búsqueda secuencial en la lista
 */
public class ServicioBusqueda {
    public static final int POR_CODIGO = 0;
    public static final int POR_PLACA = 1;
    public static final int POR_FECHA_INGRESO = 2;
    public static final int POR_MECANICO = 3;

    private final ListaOrdenes lista;
    private final ArbolOrdenCodigo arbolCodigo;
    private final ArbolIndice arbolPlaca;
    private final ArbolIndice arbolMecanico;

    /** Construye los tres árboles recorriendo la lista principal. */
    public ServicioBusqueda(ListaOrdenes lista) {
        this.lista = lista;
        this.arbolCodigo = ArbolOrdenCodigo.desdeLista(lista);
        this.arbolPlaca = ArbolIndice.desdeLista(lista, ArbolIndice.POR_PLACA);
        this.arbolMecanico = ArbolIndice.desdeLista(lista, ArbolIndice.POR_MECANICO);
    }

    public ListaOrdenes getLista() { return lista; }
    public ArbolOrdenCodigo getArbolCodigo() { return arbolCodigo; }
    public int getCodigosDuplicados() { return arbolCodigo.getDuplicados(); }
    public int getCantidadPlacas() { return arbolPlaca.getCantidadClaves(); }
    public int getCantidadMecanicos() { return arbolMecanico.getCantidadClaves(); }

    public OrdenTrabajo buscarPorCodigo(String codigo) {
        return arbolCodigo.buscar(codigo);
    }

    public ListaOrdenes buscarPorPlaca(String placa) {
        return arbolPlaca.buscar(placa);
    }

    /**
     * Elimina una orden de todas las estructuras: el AVL por código, la
     * lista principal y los dos ABB índice. Devuelve la orden eliminada o
     * null si el código no existe.
     *
     * Si el CSV traía el código repetido (RN-01), el AVL solo guardaba la
     * primera orden; al eliminarla, la siguiente con ese código pasa a ser
     * la que encuentra la búsqueda.
     */
    public OrdenTrabajo eliminarOrden(String codigo) {
        OrdenTrabajo eliminada = arbolCodigo.eliminar(codigo);
        if (eliminada == null) {
            return null;
        }
        lista.eliminar(eliminada);
        arbolPlaca.eliminar(eliminada);
        arbolMecanico.eliminar(eliminada);

        OrdenTrabajo repetida = lista.buscarPorCodigo(eliminada.getCodigoOrden());
        if (repetida != null) {
            arbolCodigo.reemplazarDuplicado(repetida);
        }
        return eliminada;
    }

    /**
     * Vuelve a insertar una orden eliminada (deshacer) en la lista y en los
     * tres árboles.
     */
    public void restaurarOrden(OrdenTrabajo orden) {
        lista.insertarFinal(orden);
        arbolCodigo.insertar(orden);
        arbolPlaca.insertar(orden);
        arbolMecanico.insertar(orden);
    }

    /** @throws IllegalArgumentException si la fecha no es válida */
    public ListaOrdenes buscarPorFechaIngreso(String fecha) {
        int numero = ValidadorFecha.aNumero(fecha);
        if (numero == ValidadorFecha.FECHA_INVALIDA) {
            throw new IllegalArgumentException("Fecha no válida. Use dd/mm/aaaa.");
        }
        return lista.buscarPorFechaIngreso(numero);
    }

    public ListaOrdenes buscarPorMecanico(String nombre) {
        ListaOrdenes exacta = arbolMecanico.buscar(nombre);
        if (!exacta.estaVacia()) {
            return exacta;
        }
        return lista.buscarPorMecanico(nombre); // "perez" encuentra "Luis Pérez"
    }

    /**
     * Ejecuta una consulta completa del formulario: búsqueda (si hay
     * valor), luego filtros y por último el ordenamiento.
     */
    public ListaOrdenes ejecutar(ConsultaBusqueda consulta) {
        String valor = consulta.getValor() == null ? "" : consulta.getValor().trim();
        ListaOrdenes base;
        if (valor.isEmpty()) {
            base = lista;
        } else {
            switch (consulta.getCriterio()) {
                case POR_CODIGO:
                    base = new ListaOrdenes();
                    OrdenTrabajo encontrada = buscarPorCodigo(valor);
                    if (encontrada != null) {
                        base.insertarFinal(encontrada);
                    }
                    break;
                case POR_PLACA:
                    base = buscarPorPlaca(valor);
                    break;
                case POR_FECHA_INGRESO:
                    base = buscarPorFechaIngreso(valor);
                    break;
                default:
                    base = buscarPorMecanico(valor);
                    break;
            }
        }

        ListaOrdenes filtrada = ServicioFiltro.aplicar(base, consulta.getFiltro());
        if (consulta.getOrden() == ListaOrdenes.SIN_ORDEN) {
            return filtrada;
        }
        return filtrada.ordenarPorInsercion(consulta.getOrden());
    }
}
