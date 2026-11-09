package vistas;

import estructuras.ListaOrdenes;
import estructuras.NodoOrden;
import modelo.OrdenTrabajo;
import servicios.LectorCSV;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class FrmPrincipal extends JFrame {
    private ListaOrdenes listaPrincipal;
    private JTable tblOrdenes;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalRegistros;

    public FrmPrincipal() {
        this.listaPrincipal = new ListaOrdenes();
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setTitle("AutoService Perú - Sistema de Búsquedas y Estadísticas");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Barra de menú superior
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemCargar = new JMenuItem("Cargar CSV...");
        JMenuItem itemSalir = new JMenuItem("Salir");

        itemCargar.addActionListener(e -> cargarCSV());
        itemSalir.addActionListener(e -> System.exit(0));

        menuArchivo.add(itemCargar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);
        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);

        // 2. Panel superior de búsqueda
        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Búsqueda Rápida"));

        JTextField txtBuscar = new JTextField(12);
        JButton btnBuscar = new JButton("Buscar Código");
        JButton btnRestaurar = new JButton("Mostrar Todos");

        btnBuscar.addActionListener(e -> {
            String cod = txtBuscar.getText().trim();
            if (cod.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese un código de orden.");
                return;
            }
            OrdenTrabajo encontrada = listaPrincipal.buscarPorCodigo(cod);
            if (encontrada != null) {
                modeloTabla.setRowCount(0);
                modeloTabla.addRow(encontrada.toTableRow());
                lblTotalRegistros.setText("Mostrando: 1 registro encontrado.");
            } else {
                JOptionPane.showMessageDialog(this, "Orden con código " + cod + " no existe.");
            }
        });

        btnRestaurar.addActionListener(e -> actualizarTabla());

        pnlNorte.add(new JLabel("Código Orden:"));
        pnlNorte.add(txtBuscar);
        pnlNorte.add(btnBuscar);
        pnlNorte.add(btnRestaurar);
        add(pnlNorte, BorderLayout.NORTH);

        // 3. Tabla de visualización
        String[] columnas = {
                "Código", "F. Ingreso", "Placa", "Tipo Vehículo",
                "Taller", "Servicio", "Mecánico", "F. Entrega", "Costo (S/)", "Estado"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblOrdenes = new JTable(modeloTabla);
        add(new JScrollPane(tblOrdenes), BorderLayout.CENTER);

        // 4. Barra de estado inferior
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotalRegistros = new JLabel("Registros cargados: 0");
        pnlSur.add(lblTotalRegistros);
        add(pnlSur, BorderLayout.SOUTH);
    }

    private void cargarCSV() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = fc.getSelectedFile();
                this.listaPrincipal = LectorCSV.cargarArchivo(archivo);
                actualizarTabla();
                JOptionPane.showMessageDialog(this, "Archivo cargado exitosamente en la lista enlazada propia.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        NodoOrden actual = listaPrincipal.getCabeza();
        while (actual != null) {
            modeloTabla.addRow(actual.getDato().toTableRow());
            actual = actual.getSiguiente();
        }
        lblTotalRegistros.setText("Registros cargados: " + listaPrincipal.getTamaño());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmPrincipal().setVisible(true));
    }
}