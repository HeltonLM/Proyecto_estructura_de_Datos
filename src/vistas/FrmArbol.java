package vistas;

import estructuras.ArbolOrdenCodigo;
import estructuras.ListaOrdenes;
import estructuras.NodoOrden;

import javax.swing.*;
import java.awt.*;

/**
 * Muestra el árbol AVL de códigos: su forma (dibujada en preorden, con la
 * altura y el factor de equilibrio de cada nodo) y la secuencia de códigos
 * en los tres recorridos: preorden, inorden y postorden.
 */
public class FrmArbol extends JFrame {
    private static final String ESTRUCTURA = "Estructura (primeros 6 niveles)";
    private static final String PREORDEN = "Preorden (raíz, izq., der.)";
    private static final String INORDEN = "Inorden (izq., raíz, der.)";
    private static final String POSTORDEN = "Postorden (izq., der., raíz)";
    private static final int NIVELES_DIBUJO = 6;
    private static final int CODIGOS_POR_LINEA = 10;

    private ArbolOrdenCodigo arbol = new ArbolOrdenCodigo();

    private final JComboBox<String> cboVista =
            new JComboBox<>(new String[]{ESTRUCTURA, PREORDEN, INORDEN, POSTORDEN});
    private final JTextArea txtArbol = new JTextArea();
    private final JLabel lblDatos = new JLabel(" ");

    public FrmArbol() {
        setTitle("AutoService Perú - Árbol AVL de códigos");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlNorte.add(new JLabel("Ver:"));
        pnlNorte.add(cboVista);
        add(pnlNorte, BorderLayout.NORTH);

        txtArbol.setEditable(false);
        txtArbol.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(txtArbol), BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSur.add(lblDatos);
        add(pnlSur, BorderLayout.SOUTH);

        cboVista.addActionListener(e -> mostrar());
    }

    public void actualizarDatos(ArbolOrdenCodigo arbol) {
        this.arbol = arbol;
        mostrar();
    }

    private void mostrar() {
        lblDatos.setText("Nodos: " + arbol.getTamaño() + "  ·  Altura: " + arbol.getAltura()
                + "  ·  Hojas (contadas en postorden): " + arbol.contarHojas());
        if (arbol.estaVacio()) {
            txtArbol.setText("El árbol está vacío. Importe un archivo CSV.");
            return;
        }

        String vista = (String) cboVista.getSelectedItem();
        if (ESTRUCTURA.equals(vista)) {
            txtArbol.setText(arbol.dibujar(NIVELES_DIBUJO));
        } else if (PREORDEN.equals(vista)) {
            txtArbol.setText(codigos(arbol.recorrerPreOrden()));
        } else if (INORDEN.equals(vista)) {
            txtArbol.setText(codigos(arbol.recorrerInOrden()));
        } else {
            txtArbol.setText(codigos(arbol.recorrerPostOrden()));
        }
        txtArbol.setCaretPosition(0);
    }

    private static String codigos(ListaOrdenes recorrido) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        NodoOrden actual = recorrido.getCabeza();
        while (actual != null) {
            sb.append(actual.getDato().getCodigoOrden());
            i++;
            sb.append(i % CODIGOS_POR_LINEA == 0 ? "\n" : "  ");
            actual = actual.getSiguiente();
        }
        return sb.toString();
    }
}
