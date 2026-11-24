package vistas;

import estructuras.ListaOrdenes;
import modelo.Campo;
import modelo.FiltroBusqueda;
import servicios.ServicioEstadistica;
import util.ValidadorFecha;

import javax.swing.*;
import java.awt.*;

/**
 * Panel con los filtros del caso: taller, tipo de vehículo, servicio,
 * estado y rango de fechas de ingreso (RF-07). Lo usan los dos formularios.
 */
public class PnlFiltros extends JPanel {
    private static final String TODOS = "(Todos)";

    private final JComboBox<String> cboTaller = new JComboBox<>();
    private final JComboBox<String> cboTipoVehiculo = new JComboBox<>();
    private final JComboBox<String> cboServicio = new JComboBox<>();
    private final JComboBox<String> cboEstado = new JComboBox<>();
    private final JTextField txtDesde = new JTextField(8);
    private final JTextField txtHasta = new JTextField(8);

    public PnlFiltros() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Filtros"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        agregar("Taller:", cboTaller, 0, 0, c);
        agregar("Tipo de vehículo:", cboTipoVehiculo, 2, 0, c);
        agregar("Servicio:", cboServicio, 4, 0, c);
        agregar("Estado:", cboEstado, 0, 1, c);
        agregar("Ingreso desde:", txtDesde, 2, 1, c);
        agregar("hasta:", txtHasta, 4, 1, c);

        txtDesde.setToolTipText("dd/mm/aaaa (vacío = sin límite)");
        txtHasta.setToolTipText("dd/mm/aaaa (vacío = sin límite)");

        cargarOpciones(new ListaOrdenes());
    }

    private void agregar(String etiqueta, JComponent campo, int x, int y, GridBagConstraints c) {
        c.gridx = x;
        c.gridy = y;
        c.weightx = 0;
        add(new JLabel(etiqueta), c);
        c.gridx = x + 1;
        c.weightx = 1;
        add(campo, c);
    }

    /** Llena los combos con los valores distintos que existen en la lista. */
    public void cargarOpciones(ListaOrdenes lista) {
        llenar(cboTaller, ServicioEstadistica.valoresDistintos(lista, Campo.TALLER));
        llenar(cboTipoVehiculo, ServicioEstadistica.valoresDistintos(lista, Campo.TIPO_VEHICULO));
        llenar(cboServicio, ServicioEstadistica.valoresDistintos(lista, Campo.SERVICIO));
        llenar(cboEstado, ServicioEstadistica.valoresDistintos(lista, Campo.ESTADO));
    }

    private void llenar(JComboBox<String> combo, String[] valores) {
        combo.removeAllItems();
        combo.addItem(TODOS);
        for (String v : valores) {
            combo.addItem(v);
        }
    }

    public void limpiar() {
        cboTaller.setSelectedIndex(0);
        cboTipoVehiculo.setSelectedIndex(0);
        cboServicio.setSelectedIndex(0);
        cboEstado.setSelectedIndex(0);
        txtDesde.setText("");
        txtHasta.setText("");
    }

    /**
     * Arma el filtro con lo seleccionado.
     * @throws IllegalArgumentException si una fecha no es válida o el rango está invertido
     */
    public FiltroBusqueda obtenerFiltro() {
        int desde = leerFecha(txtDesde, "desde");
        int hasta = leerFecha(txtHasta, "hasta");
        if (desde > 0 && hasta > 0 && desde > hasta) {
            throw new IllegalArgumentException("La fecha 'desde' es posterior a la fecha 'hasta'.");
        }
        return new FiltroBusqueda(seleccion(cboTaller), seleccion(cboTipoVehiculo),
                seleccion(cboServicio), seleccion(cboEstado), desde, hasta);
    }

    private int leerFecha(JTextField campo, String nombre) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            return 0;
        }
        int fecha = ValidadorFecha.aNumero(texto);
        if (fecha == ValidadorFecha.FECHA_INVALIDA) {
            throw new IllegalArgumentException("La fecha '" + nombre + "' no es válida. Use dd/mm/aaaa.");
        }
        return fecha;
    }

    /** Vuelve a mostrar un filtro guardado (al regresar a la consulta anterior). */
    public void mostrarFiltro(FiltroBusqueda filtro) {
        seleccionar(cboTaller, filtro.getTaller());
        seleccionar(cboTipoVehiculo, filtro.getTipoVehiculo());
        seleccionar(cboServicio, filtro.getServicio());
        seleccionar(cboEstado, filtro.getEstado());
        txtDesde.setText(ValidadorFecha.formatear(filtro.getFechaDesde()));
        txtHasta.setText(ValidadorFecha.formatear(filtro.getFechaHasta()));
    }

    private void seleccionar(JComboBox<String> combo, String valor) {
        combo.setSelectedIndex(0);
        if (valor == null) {
            return;
        }
        for (int i = 1; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equalsIgnoreCase(valor)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String seleccion(JComboBox<String> combo) {
        Object valor = combo.getSelectedItem();
        if (valor == null || TODOS.equals(valor)) {
            return null;
        }
        return valor.toString();
    }
}
