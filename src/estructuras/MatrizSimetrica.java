package estructuras;

/**
 * TAD MatrizSimetrica: matriz cuadrada simétrica (a[i][j] == a[j][i])
 * guardada como matriz triangular inferior en un arreglo unidimensional.
 *
 * Una matriz n × n necesita n² celdas, pero en una simétrica la mitad
 * superior repite la inferior. Aquí solo se guardan las celdas con j <= i:
 *
 *   fila 0: a00                 -> posiciones 0
 *   fila 1: a10 a11             -> posiciones 1, 2
 *   fila 2: a20 a21 a22         -> posiciones 3, 4, 5
 *
 * Total: n(n + 1) / 2 celdas. La celda (i, j) con j <= i está en la
 * posición i(i + 1)/2 + j; si j > i se usa (j, i), por la simetría.
 *
 * Se usa para "vehículos atendidos en dos talleres": la cantidad de placas
 * atendidas en el taller i y en el taller j es la misma que en j e i.
 */
public class MatrizSimetrica {
    private final int n;
    private final int[] celdas;

    public MatrizSimetrica(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Dimensión negativa.");
        }
        this.n = n;
        this.celdas = new int[n * (n + 1) / 2];
    }

    public int getDimension() { return n; }

    /** Celdas realmente guardadas (frente a las n² de una matriz completa). */
    public int getCeldasGuardadas() { return celdas.length; }

    public int obtener(int i, int j) {
        return celdas[indice(i, j)];
    }

    public void asignar(int i, int j, int valor) {
        celdas[indice(i, j)] = valor;
    }

    public void incrementar(int i, int j) {
        celdas[indice(i, j)]++;
    }

    /** Reconstruye la matriz completa n × n (para mostrarla en una tabla). */
    public int[][] aMatrizCompleta() {
        int[][] completa = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                completa[i][j] = obtener(i, j);
            }
        }
        return completa;
    }

    // Convierte (i, j) a la posición del arreglo de la triangular inferior
    private int indice(int i, int j) {
        if (i < 0 || j < 0 || i >= n || j >= n) {
            throw new IndexOutOfBoundsException("Celda (" + i + ", " + j + ") fuera de la matriz " + n + "×" + n);
        }
        if (j > i) {
            int t = i;
            i = j;
            j = t;
        }
        return i * (i + 1) / 2 + j;
    }
}
