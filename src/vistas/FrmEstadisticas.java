package vistas;

import estructuras.ListaOrdenes;
import estructuras.ListaResumen;
import estructuras.MatrizEstadistica;
import estructuras.NodoResumen;
import modelo.FiltroBusqueda;
import modelo.ResumenEstadistico;
import servicios.ServicioEstadistica;
import servicios.ServicioFiltro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Formulario de estadísticas (RF-08 a RF-10). Primero se eligen los
 * filtros y luego la estadística; esta se calcula solo sobre las órdenes
 * que pasan el filtro (RN-05).
 */
public class FrmEstadisticas extends JFrame {
    private static final String EST_A = "A. Órdenes por taller y tipo de servicio";
    private static final String EST_B = "B. Órdenes e importe por mecánico y estado";
    private static final String EST_C = "C. Servicios e importe por mes de ingreso y tipo de vehículo";
    private static final String EST_RANKING = "Servicios más solicitados";

    private ListaOrdenes lista = new ListaOrdenes();

    private final JComboBox<String> cboEstadistica =
            new JComboBox<>(new String[]{EST_A, EST_B, EST_C, EST_RANKING});
    private final JCheckBox chkMatriz = new JCheckBox("Ver como tabla cruzada (matriz)");
    private final PnlFiltros pnlFiltros = new PnlFiltros();
    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblFinalizadas = new JLabel(" ");

    public FrmEstadisticas() {
        setTitle("AutoService Perú - Estadísticas");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel pnlSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlSeleccion.setBorder(BorderFactory.createTitledBorder("Estadística"));
        JButton btnGenerar = new JButton("Generar");
        JButton btnLimpiar = new JButton("Limpiar filtros");
        pnlSeleccion.add(cboEstadistica);
        pnlSeleccion.add(chkMatriz);
        pnlSeleccion.add(btnGenerar);
        pnlSeleccion.add(btnLimpiar);

        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.add(pnlFiltros, BorderLayout.NORTH);
        pnlNorte.add(pnlSeleccion, BorderLayout.CENTER);
        add(pnlNorte, BorderLayout.NORTH);

        add(new JScrollPane(new JTable(modeloTabla)), BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new GridLayout(2, 1));
        pnlSur.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        pnlSur.add(lblResumen);
        pnlSur.add(lblFinalizadas);
        add(pnlSur, BorderLayout.SOUTH);

        btnGenerar.addActionListener(e -> generar());
        btnLimpiar.addActionListener(e -> pnlFiltros.limpiar());
    }

    public void actualizarDatos(ListaOrdenes lista) {
        this.lista = lista;
        pnlFiltros.cargarOpciones(lista);
        modeloTabla.setDataVector(new Object[0][0], new Object[0]);
        lblResumen.setText(lista.getTamaño() + " órdenes disponibles. Elija los filtros y una estadística.");
        lblFinalizadas.setText(" ");
    }

    private void generar() {
        if (lista.estaVacia()) {
            JOptionPane.showMessageDialog(this, "Primero importe un archivo CSV.");
            return;
        }

        FiltroBusqueda filtro;
        try {
            filtro = pnlFiltros.obtenerFiltro();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Filtro inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ListaOrdenes datos = ServicioFiltro.aplicar(lista, filtro);
        if (datos.estaVacia()) {
            modeloTabla.setDataVector(new Object[0][0], new Object[0]);
            lblResumen.setText("Ninguna orden cumple los filtros seleccionados.");
            lblFinalizadas.setText(" ");
            return;
        }

        String opcion = (String) cboEstadistica.getSelectedItem();
        boolean comoMatriz = chkMatriz.isSelected();
        if (comoMatriz && EST_A.equals(opcion)) {
            mostrarMatriz(ServicioEstadistica.matrizTallerServicio(datos), "Taller / Servicio", false);
        } else if (comoMatriz && EST_B.equals(opcion)) {
            mostrarMatriz(ServicioEstadistica.matrizMecanicoEstado(datos), "Mecánico / Estado", true);
        } else if (comoMatriz && EST_C.equals(opcion)) {
            mostrarMatriz(ServicioEstadistica.matrizMesTipoVehiculo(datos), "Mes / Tipo de vehículo", true);
        } else if (EST_A.equals(opcion)) {
            mostrarAgrupado(ServicioEstadistica.estadisticaTallerServicio(datos),
                    "Taller", "Servicio", "N° de órdenes", false);
        } else if (EST_B.equals(opcion)) {
            mostrarAgrupado(ServicioEstadistica.estadisticaMecanicoEstado(datos),
                    "Mecánico", "Estado", "N° de órdenes", true);
        } else if (EST_C.equals(opcion)) {
            mostrarAgrupado(ServicioEstadistica.estadisticaMesTipoVehiculo(datos),
                    "Mes de ingreso", "Tipo de vehículo", "Cantidad de servicios", true);
        } else {
            mostrarRanking(ServicioEstadistica.rankingServicios(datos));
        }

        lblResumen.setText(String.format("Órdenes analizadas: %d de %d%s  ·  Importe total: S/ %,.2f",
                datos.getTamaño(), lista.getTamaño(),
                filtro.estaActivo() ? " (con filtros)" : "", datos.calcularImporteTotal()));
        ResumenEstadistico finalizadas = ServicioEstadistica.resumenFinalizadas(datos);
        lblFinalizadas.setText(String.format("Órdenes finalizadas: %d  ·  Importe de órdenes finalizadas: S/ %,.2f",
                finalizadas.getCantidad(), finalizadas.getImporteTotal()));
    }

    /**
     * Muestra la ListaResumen (ya ordenada por grupo y subgrupo) con un
     * subtotal al terminar cada grupo y el total general al final.
     */
    private void mostrarAgrupado(ListaResumen resumen, String tituloGrupo, String tituloSubgrupo,
                                 String tituloCantidad, boolean conImporte) {
        // Cuenta los grupos distintos para dimensionar la matriz de la tabla
        int grupos = 0;
        String anterior = null;
        NodoResumen actual = resumen.getCabeza();
        while (actual != null) {
            if (!actual.getDato().getGrupo().equals(anterior)) {
                grupos++;
                anterior = actual.getDato().getGrupo();
            }
            actual = actual.getSiguiente();
        }

        int columnas = conImporte ? 4 : 3;
        Object[][] filas = new Object[resumen.getTamaño() + grupos + 1][columnas];
        int k = 0;
        int subtotalCantidad = 0;
        double subtotalImporte = 0;
        actual = resumen.getCabeza();
        while (actual != null) {
            ResumenEstadistico r = actual.getDato();
            filas[k++] = fila(r.getGrupo(), r.getSubgrupo(), r.getCantidad(), r.getImporteTotal(), conImporte);
            subtotalCantidad += r.getCantidad();
            subtotalImporte += r.getImporteTotal();

            NodoResumen siguiente = actual.getSiguiente();
            if (siguiente == null || !siguiente.getDato().getGrupo().equals(r.getGrupo())) {
                filas[k++] = fila(r.getGrupo(), "Subtotal", subtotalCantidad, subtotalImporte, conImporte);
                subtotalCantidad = 0;
                subtotalImporte = 0;
            }
            actual = siguiente;
        }
        filas[k] = fila("TOTAL GENERAL", "", resumen.totalCantidad(), resumen.totalImporte(), conImporte);

        Object[] titulos = conImporte
                ? new Object[]{tituloGrupo, tituloSubgrupo, tituloCantidad, "Importe total (S/)"}
                : new Object[]{tituloGrupo, tituloSubgrupo, tituloCantidad};
        modeloTabla.setDataVector(filas, titulos);
    }

    /**
     * Muestra la matriz como tabla cruzada: una fila por etiqueta de fila,
     * una columna por etiqueta de columna, y al final la columna y la fila
     * de totales. Con importe, cada celda muestra "cantidad (S/ importe)".
     */
    private void mostrarMatriz(MatrizEstadistica m, String esquina, boolean conImporte) {
        int filas = m.getFilas();
        int columnas = m.getColumnas();

        Object[] titulos = new Object[columnas + 2];
        titulos[0] = esquina;
        for (int j = 0; j < columnas; j++) {
            titulos[j + 1] = m.getEtiquetaColumna(j);
        }
        titulos[columnas + 1] = "TOTAL";

        Object[][] celdas = new Object[filas + 1][columnas + 2];
        for (int i = 0; i < filas; i++) {
            celdas[i][0] = m.getEtiquetaFila(i);
            for (int j = 0; j < columnas; j++) {
                celdas[i][j + 1] = celda(m.getCantidad(i, j), m.getImporte(i, j), conImporte);
            }
            celdas[i][columnas + 1] = celda(m.totalCantidadFila(i), m.totalImporteFila(i), conImporte);
        }
        celdas[filas][0] = "TOTAL";
        for (int j = 0; j < columnas; j++) {
            celdas[filas][j + 1] = celda(m.totalCantidadColumna(j), m.totalImporteColumna(j), conImporte);
        }
        celdas[filas][columnas + 1] = celda(m.totalCantidad(), m.totalImporte(), conImporte);

        modeloTabla.setDataVector(celdas, titulos);
    }

    private static Object celda(int cantidad, double importe, boolean conImporte) {
        if (!conImporte) {
            return cantidad;
        }
        return cantidad == 0 ? "0" : String.format("%d (S/ %,.2f)", cantidad, importe);
    }

    private void mostrarRanking(ListaResumen resumen) {
        Object[][] filas = new Object[resumen.getTamaño()][4];
        int i = 0;
        NodoResumen actual = resumen.getCabeza();
        while (actual != null) {
            ResumenEstadistico r = actual.getDato();
            filas[i] = new Object[]{i + 1, r.getGrupo(), r.getCantidad(), String.format("%,.2f", r.getImporteTotal())};
            i++;
            actual = actual.getSiguiente();
        }
        modeloTabla.setDataVector(filas,
                new Object[]{"Puesto", "Servicio", "N° de órdenes", "Importe total (S/)"});
    }

    private static Object[] fila(String grupo, String detalle, int cantidad, double importe, boolean conImporte) {
        if (conImporte) {
            return new Object[]{grupo, detalle, cantidad, String.format("%,.2f", importe)};
        }
        return new Object[]{grupo, detalle, cantidad};
    }
}
