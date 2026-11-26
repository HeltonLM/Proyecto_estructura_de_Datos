package vistas;

import estructuras.ColaPrioridadOrdenes;
import estructuras.ListaOrdenes;
import modelo.OrdenTrabajo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Cola de atención: las órdenes que todavía no terminan, en el orden en que
 * conviene atenderlas (cola con prioridad). Primero las pendientes y luego
 * las que están en proceso; dentro de cada grupo, la de ingreso más antiguo.
 * "Atender siguiente" desencola la más urgente.
 */
public class FrmColaAtencion extends JFrame {
    private static final String[] COLUMNAS = {
            "Turno", "Código", "F. Ingreso", "Placa", "Tipo Vehículo", "Taller",
            "Servicio", "Mecánico", "F. Entrega", "Costo (S/)", "Estado"
    };

    private ListaOrdenes lista = new ListaOrdenes();
    private ColaPrioridadOrdenes cola = new ColaPrioridadOrdenes();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel lblEstado = new JLabel(" ");

    public FrmColaAtencion() {
        setTitle("AutoService Perú - Cola de atención");
        setSize(1150, 500);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton btnAtender = new JButton("Atender siguiente");
        JButton btnReiniciar = new JButton("Volver a armar la cola");
        pnlNorte.add(new JLabel("Prioridad: 1) Pendiente  2) En proceso; a igual prioridad, el ingreso más antiguo."));
        pnlNorte.add(btnAtender);
        pnlNorte.add(btnReiniciar);
        add(pnlNorte, BorderLayout.NORTH);

        add(new JScrollPane(new JTable(modeloTabla)), BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSur.add(lblEstado);
        add(pnlSur, BorderLayout.SOUTH);

        btnAtender.addActionListener(e -> atenderSiguiente());
        btnReiniciar.addActionListener(e -> armarCola());
    }

    public void actualizarDatos(ListaOrdenes lista) {
        this.lista = lista;
        armarCola();
    }

    private void armarCola() {
        cola = ColaPrioridadOrdenes.desdeLista(lista);
        pintar();
        lblEstado.setText(cola.getTamaño() + " órdenes por atender.");
    }

    private void atenderSiguiente() {
        if (cola.estaVacia()) {
            JOptionPane.showMessageDialog(this, "No hay órdenes por atender.");
            return;
        }
        OrdenTrabajo orden = cola.desencolar();
        pintar();
        lblEstado.setText("Atendida: " + orden.getCodigoOrden() + " (" + orden.getPlaca() + ", "
                + orden.getEstado() + ")  ·  Quedan " + cola.getTamaño() + " en la cola.");
    }

    private void pintar() {
        modeloTabla.setDataVector(cola.aFilasTabla(), COLUMNAS);
    }
}
