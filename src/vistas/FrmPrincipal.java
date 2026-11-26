package vistas;

import estructuras.ColaImportacion;
import estructuras.ListaOrdenes;
import servicios.LectorCSV;
import servicios.ResultadoCarga;
import servicios.ServicioBusqueda;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/**
 * Formulario principal. El menú da acceso a la importación de los CSV,
 * a los formularios de búsquedas y estadísticas, y a la salida. La tabla
 * central muestra todas las órdenes cargadas en la lista principal.
 */
public class FrmPrincipal extends JFrame {
    private static final String[] COLUMNAS = {
            "Código", "F. Ingreso", "Placa", "Tipo Vehículo", "Taller",
            "Servicio", "Mecánico", "F. Entrega", "Costo (S/)", "Estado"
    };

    private ListaOrdenes listaPrincipal = new ListaOrdenes();
    private ServicioBusqueda servicioBusqueda = new ServicioBusqueda(listaPrincipal);

    private final FrmBusqueda frmBusqueda = new FrmBusqueda();
    private final FrmEstadisticas frmEstadisticas = new FrmEstadisticas();
    private final FrmColaAtencion frmColaAtencion = new FrmColaAtencion();
    private final FrmArbol frmArbol = new FrmArbol();

    private DefaultTableModel modeloTabla;
    private JLabel lblTotalRegistros;
    private JMenuItem itemImportar;
    private JMenuItem itemAgregar;

    public FrmPrincipal() {
        iniciarComponentes();
        frmBusqueda.actualizarDatos(servicioBusqueda);
        actualizarVistasSecundarias();
        frmBusqueda.setAlCambiarDatos(() -> datosModificados());
    }

    /** Refresca la tabla principal y los demás formularios después de eliminar o restaurar una orden. */
    private void datosModificados() {
        modeloTabla.setDataVector(listaPrincipal.aFilasTabla(), COLUMNAS);
        actualizarVistasSecundarias();
        actualizarEtiqueta(null);
    }

    private void actualizarVistasSecundarias() {
        frmEstadisticas.actualizarDatos(listaPrincipal);
        frmColaAtencion.actualizarDatos(listaPrincipal);
        frmArbol.actualizarDatos(servicioBusqueda.getArbolCodigo());
    }

    private void iniciarComponentes() {
        setTitle("AutoService Perú - Sistema de Búsquedas y Estadísticas");
        setSize(1150, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Menú principal
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        itemImportar = new JMenuItem("Importar CSV...");
        itemAgregar = new JMenuItem("Agregar CSV de otro taller...");
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemImportar.addActionListener(e -> importarCSV(false));
        itemAgregar.addActionListener(e -> importarCSV(true));
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemImportar);
        menuArchivo.add(itemAgregar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        JMenu menuConsultas = new JMenu("Consultas");
        JMenuItem itemBusquedas = new JMenuItem("Búsquedas...");
        JMenuItem itemEstadisticas = new JMenuItem("Estadísticas...");
        JMenuItem itemCola = new JMenuItem("Cola de atención...");
        JMenuItem itemArbol = new JMenuItem("Árbol de códigos (AVL)...");
        itemBusquedas.addActionListener(e -> abrir(frmBusqueda));
        itemEstadisticas.addActionListener(e -> abrir(frmEstadisticas));
        menuConsultas.add(itemBusquedas);
        menuConsultas.add(itemEstadisticas);
        menuConsultas.addSeparator();
        menuConsultas.add(itemCola);
        menuConsultas.add(itemArbol);
        itemCola.addActionListener(e -> abrir(frmColaAtencion));
        itemArbol.addActionListener(e -> abrir(frmArbol));

        menuBar.add(menuArchivo);
        menuBar.add(menuConsultas);
        setJMenuBar(menuBar);

        // 2. Tabla con todas las órdenes cargadas
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tblOrdenes = new JTable(modeloTabla);
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.setBorder(BorderFactory.createTitledBorder("Órdenes de trabajo cargadas"));
        pnlCentro.add(new JScrollPane(tblOrdenes), BorderLayout.CENTER);
        add(pnlCentro, BorderLayout.CENTER);

        // 3. Barra de estado inferior
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotalRegistros = new JLabel("Registros cargados: 0. Use Archivo > Importar CSV...");
        pnlSur.add(lblTotalRegistros);
        add(pnlSur, BorderLayout.SOUTH);
    }

    private void abrir(JFrame formulario) {
        formulario.setLocationRelativeTo(this);
        formulario.setVisible(true);
        formulario.toFront();
    }

    /**
     * Importa un CSV. Con agregar = true, sus órdenes se suman a las ya
     * cargadas (cada taller tiene su propio archivo). La lectura se hace en
     * segundo plano para que la ventana no se congele con archivos grandes.
     */
    private void importarCSV(boolean agregar) {
        JFileChooser fc = new JFileChooser(new File("datos"));
        fc.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv", "txt"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File archivo = fc.getSelectedFile();
        final ListaOrdenes listaActual = listaPrincipal;

        habilitarImportacion(false);
        lblTotalRegistros.setText("Leyendo " + archivo.getName() + "...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Object[], Void> tarea = new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                ResultadoCarga resultado = LectorCSV.cargarArchivo(archivo);
                ColaImportacion cola = resultado.getOrdenes();
                int leidas = cola.getTamaño();

                // Al agregar se trabaja sobre una copia: la lista visible no cambia hasta terminar
                ListaOrdenes nuevaLista = agregar ? listaActual.copiar() : new ListaOrdenes();
                cola.transferirA(nuevaLista);
                ServicioBusqueda nuevoServicio = new ServicioBusqueda(nuevaLista);
                return new Object[]{resultado, nuevoServicio, leidas};
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                habilitarImportacion(true);
                try {
                    Object[] r = get();
                    terminarImportacion(archivo, (ResultadoCarga) r[0], (ServicioBusqueda) r[1], (Integer) r[2]);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    actualizarEtiqueta(null);
                    JOptionPane.showMessageDialog(FrmPrincipal.this,
                            "Error al importar CSV: " + causa.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        tarea.execute();
    }

    private void terminarImportacion(File archivo, ResultadoCarga resultado,
                                     ServicioBusqueda nuevoServicio, int leidas) {
        servicioBusqueda = nuevoServicio;
        listaPrincipal = nuevoServicio.getLista();

        modeloTabla.setDataVector(listaPrincipal.aFilasTabla(), COLUMNAS);
        frmBusqueda.actualizarDatos(servicioBusqueda);
        actualizarVistasSecundarias();
        actualizarEtiqueta(archivo.getName() + " (" + resultado.getCodificacion() + ")");

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Archivo importado en la lista enlazada propia.\n\n")
               .append("Órdenes válidas en este archivo: ").append(leidas).append('\n')
               .append("Total de órdenes cargadas: ").append(listaPrincipal.getTamaño()).append('\n')
               .append("Placas distintas: ").append(servicioBusqueda.getCantidadPlacas()).append('\n')
               .append("Mecánicos distintos: ").append(servicioBusqueda.getCantidadMecanicos()).append('\n');
        if (servicioBusqueda.getCodigosDuplicados() > 0) {
            mensaje.append("Códigos de orden repetidos: ").append(servicioBusqueda.getCodigosDuplicados())
                   .append(" (la búsqueda por código muestra la primera)\n");
        }

        if (resultado.getLineasConError() > 0) {
            mensaje.append("\nLíneas omitidas por datos inválidos: ").append(resultado.getLineasConError())
                   .append("\n\n").append(resultado.resumenProblemas());
            JOptionPane.showMessageDialog(this, mensaje.toString(), "Importación con observaciones",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, mensaje.toString(), "Importación completa",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void actualizarEtiqueta(String ultimoArchivo) {
        String texto = "Registros cargados: " + listaPrincipal.getTamaño();
        if (ultimoArchivo != null) {
            texto += "  ·  Último archivo: " + ultimoArchivo;
        }
        lblTotalRegistros.setText(texto);
    }

    private void habilitarImportacion(boolean habilitar) {
        itemImportar.setEnabled(habilitar);
        itemAgregar.setEnabled(habilitar);
        frmBusqueda.setEliminacionHabilitada(habilitar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmPrincipal().setVisible(true));
    }
}
