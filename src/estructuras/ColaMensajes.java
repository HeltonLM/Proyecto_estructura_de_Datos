package estructuras;

/**
 * TAD ColaMensajes: cola (FIFO) implementada con un arreglo circular.
 *
 * Guarda los mensajes de las líneas inválidas de un CSV en el mismo orden
 * en que se encontraron. Los índices frente y fin avanzan con
 * (i + 1) % capacidad, de modo que las posiciones liberadas al desencolar
 * se reutilizan sin desplazar elementos.
 */
public class ColaMensajes {
    private final String[] datos;
    private int frente;   // posición del primer elemento
    private int fin;      // posición donde entra el siguiente
    private int cantidad;

    public ColaMensajes(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser positiva.");
        }
        this.datos = new String[capacidad];
        this.frente = 0;
        this.fin = 0;
        this.cantidad = 0;
    }

    /** enqueue: agrega al final. Devuelve false si la cola está llena. */
    public boolean encolar(String mensaje) {
        if (estaLlena()) {
            return false;
        }
        datos[fin] = mensaje;
        fin = (fin + 1) % datos.length;
        cantidad++;
        return true;
    }

    /** dequeue: retira y devuelve el primero. */
    public String desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola de mensajes está vacía.");
        }
        String dato = datos[frente];
        datos[frente] = null;
        frente = (frente + 1) % datos.length;
        cantidad--;
        return dato;
    }

    public String verFrente() {
        return estaVacia() ? null : datos[frente];
    }

    public boolean estaVacia() { return cantidad == 0; }
    public boolean estaLlena() { return cantidad == datos.length; }
    public int getCantidad() { return cantidad; }
    public int getCapacidad() { return datos.length; }

    /** Recorre la cola del frente al fin sin modificarla. */
    public String[] aArreglo() {
        String[] resultado = new String[cantidad];
        for (int k = 0; k < cantidad; k++) {
            resultado[k] = datos[(frente + k) % datos.length];
        }
        return resultado;
    }
}
