package estructuras;

import modelo.Campo;

/**
 * TAD MatrizEstadistica: tabla cruzada de una estadística del caso.
 *
 * Usa arreglos paralelos y matrices (sílabo, unidad 1):
 *   etiquetasFila[i]    nombre de la fila i     (por ejemplo, el taller)
 *   etiquetasColumna[j] nombre de la columna j  (por ejemplo, el servicio)
 *   cantidades[i][j]    órdenes de esa fila y esa columna
 *   importes[i][j]      suma de sus costos
 *
 * Se construye en dos recorridos de la lista:
 *   1) Se reúnen los valores distintos de cada campo en un arreglo
 *      unidimensional ordenado (inserción con desplazamiento). Si el arreglo
 *      se llena, se copia a otro del doble de tamaño.
 *   2) Con las dimensiones ya conocidas se crean las matrices y cada orden
 *      suma en la celda [fila][columna] que le corresponde.
 */
public class MatrizEstadistica {
    private static final int CAPACIDAD_INICIAL = 8;

    private final String[] etiquetasFila;
    private final String[] etiquetasColumna;
    private final int[][] cantidades;
    private final double[][] importes;

    private MatrizEstadistica(String[] etiquetasFila, String[] etiquetasColumna) {
        this.etiquetasFila = etiquetasFila;
        this.etiquetasColumna = etiquetasColumna;
        this.cantidades = new int[etiquetasFila.length][etiquetasColumna.length];
        this.importes = new double[etiquetasFila.length][etiquetasColumna.length];
    }

    /** Construye la tabla cruzada campoFila × campoColumna de las órdenes de la lista. */
    public static MatrizEstadistica construir(ListaOrdenes lista, Campo campoFila, Campo campoColumna) {
        MatrizEstadistica matriz = new MatrizEstadistica(
                valoresDistintos(lista, campoFila), valoresDistintos(lista, campoColumna));

        NodoOrden actual = lista.getCabeza();
        while (actual != null) {
            int i = buscar(matriz.etiquetasFila, matriz.etiquetasFila.length,
                    valor(campoFila.valorDe(actual.getDato())));
            int j = buscar(matriz.etiquetasColumna, matriz.etiquetasColumna.length,
                    valor(campoColumna.valorDe(actual.getDato())));
            matriz.cantidades[i][j]++;
            matriz.importes[i][j] += actual.getDato().getCostoTotal();
            actual = actual.getSiguiente();
        }
        return matriz;
    }

    // ----- Consultas -----

    public int getFilas() { return etiquetasFila.length; }
    public int getColumnas() { return etiquetasColumna.length; }
    public String getEtiquetaFila(int i) { return etiquetasFila[i]; }
    public String getEtiquetaColumna(int j) { return etiquetasColumna[j]; }
    public int getCantidad(int i, int j) { return cantidades[i][j]; }
    public double getImporte(int i, int j) { return importes[i][j]; }

    /** Recorre la fila i sumando sus cantidades. */
    public int totalCantidadFila(int i) {
        int total = 0;
        for (int j = 0; j < cantidades[i].length; j++) {
            total += cantidades[i][j];
        }
        return total;
    }

    /** Recorre la columna j sumando sus cantidades. */
    public int totalCantidadColumna(int j) {
        int total = 0;
        for (int i = 0; i < cantidades.length; i++) {
            total += cantidades[i][j];
        }
        return total;
    }

    public double totalImporteFila(int i) {
        double total = 0;
        for (int j = 0; j < importes[i].length; j++) {
            total += importes[i][j];
        }
        return total;
    }

    public double totalImporteColumna(int j) {
        double total = 0;
        for (int i = 0; i < importes.length; i++) {
            total += importes[i][j];
        }
        return total;
    }

    /** Recorrido completo de la matriz. */
    public int totalCantidad() {
        int total = 0;
        for (int i = 0; i < cantidades.length; i++) {
            total += totalCantidadFila(i);
        }
        return total;
    }

    public double totalImporte() {
        double total = 0;
        for (int i = 0; i < importes.length; i++) {
            total += totalImporteFila(i);
        }
        return total;
    }

    // ----- Operaciones con arreglos unidimensionales -----

    /**
     * Devuelve los valores distintos del campo, ordenados alfabéticamente,
     * en un arreglo del tamaño exacto.
     */
    static String[] valoresDistintos(ListaOrdenes lista, Campo campo) {
        String[] valores = new String[CAPACIDAD_INICIAL];
        int cantidad = 0;
        NodoOrden actual = lista.getCabeza();
        while (actual != null) {
            String v = valor(campo.valorDe(actual.getDato()));
            if (buscar(valores, cantidad, v) == -1) {
                if (cantidad == valores.length) {
                    valores = copiar(valores, valores.length * 2);
                }
                insertarOrdenado(valores, cantidad, v);
                cantidad++;
            }
            actual = actual.getSiguiente();
        }
        return copiar(valores, cantidad);
    }

    /** Búsqueda secuencial en las primeras n posiciones. Devuelve el índice o -1. */
    static int buscar(String[] arreglo, int n, String valor) {
        for (int i = 0; i < n; i++) {
            if (arreglo[i].equalsIgnoreCase(valor)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Inserta el valor en su posición dentro de las n primeras celdas
     * (ya ordenadas), desplazando a la derecha los mayores. Requiere n < largo.
     */
    static void insertarOrdenado(String[] arreglo, int n, String valor) {
        int i = n - 1;
        while (i >= 0 && arreglo[i].compareToIgnoreCase(valor) > 0) {
            arreglo[i + 1] = arreglo[i];
            i--;
        }
        arreglo[i + 1] = valor;
    }

    /** Copia los elementos a un arreglo nuevo del tamaño indicado. */
    static String[] copiar(String[] origen, int nuevoTamaño) {
        String[] destino = new String[nuevoTamaño];
        int limite = Math.min(origen.length, nuevoTamaño);
        for (int i = 0; i < limite; i++) {
            destino[i] = origen[i];
        }
        return destino;
    }

    private static String valor(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "(Sin dato)";
        }
        return texto.trim();
    }
}
