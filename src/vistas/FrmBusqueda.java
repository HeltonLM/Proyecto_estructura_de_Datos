package vistas;

import estructuras.ListaOrdenes;
import estructuras.PilaHistorialFiltros;
import modelo.ConsultaBusqueda;
import modelo.FiltroBusqueda;
import servicios.ServicioBusqueda;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Formulario de búsquedas (RF-03 a RF-07): buscar por código, placa, fecha
 * de ingreso o mecánico; filtrar por taller, tipo de vehículo, servicio,
 * estado y rango de fechas; y ordenar los resultados por inserción.
 * Las consultas anteriores se guardan en una pila para poder regresar.
 */
public class FrmBusqueda extends JFrame {
    private static final String[] COLUMNAS = {
            "Código", "F. Ingreso", "Placa", "Tipo Vehículo", "Taller",
            "Servicio", "Mecánico", "F. Entrega", "Costo (S/)", "Estado"
    };
    private static final int[] ANCHOS = {80, 80, 75, 120, 130, 220, 120, 80, 80, 90};

    // El índice de cada opción coincide con las constantes de ServicioBusqueda
    private static final String[] CRITERIOS = {"Código de orden", "Placa", "Fecha de ingreso", "Mecánico"};
    // El índice de cada opción coincide con las constantes de ListaOrdenes
    private static final String[] ORDENES = {"Sin ordenar", "Fecha de ingreso", "Costo (mayor a menor)",
            "Estado", "Código de orden"};

    private ServicioBusqueda servicio;
    private ConsultaBusqueda consultaActual;
    private final PilaHistorialFiltros historial = new PilaHistorialFiltros();

    private final JComboBox<String> cboCriterio = new JComboBox<>(CRITERIOS);
    private final JTextField txtValor = new JTextField(16);
    private final JComboBox<String> cboOrden = new JComboBox<>(ORDENES);
    private final JButton btnAnterior = new JButton("Consulta anterior");
    private final PnlFiltros pnlFiltros = new PnlFiltros();
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;
    private final JLabel lblResultado = new JLabel(" ");

    public FrmBusqueda() {
        setTitle("AutoService Perú - Búsquedas");
        setSize(1150, 650);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlBusqueda.setBorder(BorderFactory.createTitledBorder("Búsqueda"));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");
        pnlBusqueda.add(new JLabel("Buscar por:"));
        pnlBusqueda.add(cboCriterio);
        pnlBusqueda.add(txtValor);
        pnlBusqueda.add(new JLabel("Ordenar por:"));
        pnlBusqueda.add(cboOrden);
        pnlBusqueda.add(btnBuscar);
        pnlBusqueda.add(btnLimpiar);
        pnlBusqueda.add(btnAnterior);
        txtValor.setToolTipText("Vacío = todas las órdenes. Fecha en formato dd/mm/aaaa.");

        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.add(pnlBusqueda, BorderLayout.NORTH);
        pnlNorte.add(pnlFiltros, BorderLayout.CENTER);
        add(pnlNorte, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        aplicarAnchos();
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSur.add(lblResultado);
        add(pnlSur, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscar());
        txtValor.addActionListener(e -> buscar()); // Enter en la caja de texto
        btnLimpiar.addActionListener(e -> limpiar());
        btnAnterior.addActionListener(e -> consultaAnterior());
        btnAnterior.setEnabled(false);
    }

    /** Recibe los datos recién importados y muestra todas las órdenes. */
    public void actualizarDatos(ServicioBusqueda servicio) {
        this.servicio = servicio;
        historial.vaciar();
        btnAnterior.setEnabled(false);
        pnlFiltros.cargarOpciones(servicio.getLista());
        txtValor.setText("");
        cboOrden.setSelectedIndex(0);
        consultaActual = new ConsultaBusqueda(ServicioBusqueda.POR_CODIGO, "", FiltroBusqueda.sinFiltro(),
                ListaOrdenes.SIN_ORDEN);
        mostrar(servicio.getLista());
    }

    private void buscar() {
        if (servicio == null || servicio.getLista().estaVacia()) {
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

        ConsultaBusqueda consulta = new ConsultaBusqueda(cboCriterio.getSelectedIndex(),
                txtValor.getText().trim(), filtro, cboOrden.getSelectedIndex());
        if (ejecutar(consulta)) {
            historial.apilar(consultaActual); // guarda la consulta que se estaba viendo
            consultaActual = consulta;
            btnAnterior.setEnabled(true);
        }
    }

    private void consultaAnterior() {
        if (historial.estaVacia()) {
            return;
        }
        ConsultaBusqueda anterior = historial.desapilar();
        cboCriterio.setSelectedIndex(anterior.getCriterio());
        txtValor.setText(anterior.getValor());
        cboOrden.setSelectedIndex(anterior.getOrden());
        pnlFiltros.mostrarFiltro(anterior.getFiltro());
        ejecutar(anterior);
        consultaActual = anterior;
        btnAnterior.setEnabled(!historial.estaVacia());
    }

    private void limpiar() {
        txtValor.setText("");
        cboOrden.setSelectedIndex(0);
        pnlFiltros.limpiar();
        if (servicio != null) {
            mostrar(servicio.getLista());
        }
    }

    /** Ejecuta la consulta y muestra el resultado. Devuelve false si los datos eran inválidos. */
    private boolean ejecutar(ConsultaBusqueda consulta) {
        ListaOrdenes resultado;
        try {
            resultado = servicio.ejecutar(consulta);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        mostrar(resultado);
        if (resultado.estaVacia()) {
            JOptionPane.showMessageDialog(this, "No se encontraron órdenes con esos criterios.");
        }
        return true;
    }

    private void mostrar(ListaOrdenes resultado) {
        modeloTabla.setDataVector(resultado.aFilasTabla(), COLUMNAS);
        aplicarAnchos(); // setDataVector recrea las columnas
        lblResultado.setText(String.format("Mostrando %d de %d órdenes  ·  Importe total: S/ %,.2f",
                resultado.getTamaño(), servicio.getLista().getTamaño(), resultado.calcularImporteTotal()));
    }

    private void aplicarAnchos() {
        for (int i = 0; i < ANCHOS.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(ANCHOS[i]);
        }
    }
}
