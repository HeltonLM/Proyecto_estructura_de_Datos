package servicios;

import modelo.OrdenTrabajo;
import util.ValidadorFecha;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/**
 * Lee ordenes_trabajo.csv (RF-01) y crea una OrdenTrabajo por cada fila
 * válida. Las órdenes se encolan en una ColaImportacion en el orden en que
 * se leen; quien llama decide después a qué lista pasarlas.
 *
 * Es tolerante con los formatos más comunes:
 *  - separador coma, punto y coma o tabulación (se detecta en el encabezado);
 *  - campos entre comillas que contienen el separador ("Frenos, pastillas");
 *  - columnas en otro orden, si el encabezado tiene nombres reconocibles;
 *  - archivos UTF-8 (con o sin BOM) o Windows-1252, típico de Excel;
 *  - costos como 1200.50, 1,200.50, 1200,50 o "S/ 1 200.50".
 * Valida los datos mínimos: campos obligatorios, fechas reales, costo
 * numérico y no negativo (RN-04). Una fila con error no detiene la carga:
 * se omite y se informa con su número de línea.
 */
public class LectorCSV {

    // Posiciones de las columnas en el orden que indica el caso
    private static final int CODIGO = 0, FECHA_INGRESO = 1, PLACA = 2, TIPO_VEHICULO = 3,
            TALLER = 4, SERVICIO = 5, MECANICO = 6, FECHA_ENTREGA = 7, COSTO = 8, ESTADO = 9;
    private static final int TOTAL_COLUMNAS = 10;

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    /**
     * Lee el archivo completo. Primero intenta UTF-8; si encuentra bytes que
     * no son UTF-8 válido, vuelve a leer con Windows-1252 para que las tildes
     * se vean bien.
     */
    public static ResultadoCarga cargarArchivo(File archivo) throws IOException {
        try {
            return leer(archivo, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            return leer(archivo, WINDOWS_1252);
        }
    }

    private static ResultadoCarga leer(File archivo, Charset codificacion) throws IOException {
        CharsetDecoder decodificador = codificacion.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        ResultadoCarga resultado = new ResultadoCarga();
        resultado.setCodificacion(codificacion.displayName());

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), decodificador))) {

            String encabezado = br.readLine();
            if (encabezado == null) {
                throw new IOException("El archivo está vacío.");
            }
            if (!encabezado.isEmpty() && encabezado.charAt(0) == '﻿') {
                encabezado = encabezado.substring(1); // quita el BOM de UTF-8
            }

            char separador = detectarSeparador(encabezado);
            int[] posiciones = mapearColumnas(dividirLinea(encabezado, separador));

            int numeroLinea = 1;
            String linea;
            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                int lineaInicio = numeroLinea;

                // Un campo entre comillas puede contener saltos de línea
                while (comillasAbiertas(linea)) {
                    String siguiente = br.readLine();
                    if (siguiente == null) break;
                    numeroLinea++;
                    linea = linea + "\n" + siguiente;
                }

                if (linea.trim().isEmpty()) continue;
                procesarLinea(linea, separador, posiciones, lineaInicio, resultado);
            }
        }
        return resultado;
    }

    private static void procesarLinea(String linea, char separador, int[] posiciones,
                                      int numeroLinea, ResultadoCarga resultado) {
        String[] datos = dividirLinea(linea, separador);
        int necesarias = maximo(posiciones) + 1;
        if (datos.length < necesarias) {
            resultado.registrarError(numeroLinea, "tiene " + datos.length
                    + " columnas y se esperaban " + necesarias + ".");
            return;
        }

        String codigo = campo(datos, posiciones, CODIGO);
        String fechaIngreso = campo(datos, posiciones, FECHA_INGRESO);
        String placa = campo(datos, posiciones, PLACA);
        String tipoVehiculo = campo(datos, posiciones, TIPO_VEHICULO);
        String taller = campo(datos, posiciones, TALLER);
        String servicio = campo(datos, posiciones, SERVICIO);
        String mecanico = campo(datos, posiciones, MECANICO);
        String fechaEntrega = campo(datos, posiciones, FECHA_ENTREGA);
        String textoCosto = campo(datos, posiciones, COSTO);
        String estado = campo(datos, posiciones, ESTADO);

        // Campos obligatorios (la fecha de entrega puede faltar si la orden no terminó)
        String faltante = codigo.isEmpty() ? "código de orden"
                : fechaIngreso.isEmpty() ? "fecha de ingreso"
                : placa.isEmpty() ? "placa"
                : tipoVehiculo.isEmpty() ? "tipo de vehículo"
                : taller.isEmpty() ? "taller"
                : servicio.isEmpty() ? "servicio"
                : mecanico.isEmpty() ? "mecánico"
                : estado.isEmpty() ? "estado"
                : null;
        if (faltante != null) {
            resultado.registrarError(numeroLinea, "falta el dato obligatorio '" + faltante + "'.");
            return;
        }

        int ingreso = ValidadorFecha.aNumero(fechaIngreso);
        if (ingreso == ValidadorFecha.FECHA_INVALIDA) {
            resultado.registrarError(numeroLinea, "fecha de ingreso inválida '" + fechaIngreso + "'.");
            return;
        }
        if (!fechaEntrega.isEmpty()) {
            int entrega = ValidadorFecha.aNumero(fechaEntrega);
            if (entrega == ValidadorFecha.FECHA_INVALIDA) {
                resultado.registrarError(numeroLinea, "fecha de entrega inválida '" + fechaEntrega + "'.");
                return;
            }
            if (entrega < ingreso) {
                resultado.registrarError(numeroLinea, "la fecha de entrega es anterior a la de ingreso.");
                return;
            }
        }

        double costo;
        try {
            costo = convertirImporte(textoCosto, separador);
        } catch (NumberFormatException e) {
            resultado.registrarError(numeroLinea, "costo inválido '" + textoCosto + "'.");
            return;
        }
        if (costo < 0) { // RN-04
            resultado.registrarError(numeroLinea, "el costo no puede ser negativo (" + textoCosto + ").");
            return;
        }

        resultado.getOrdenes().encolar(new OrdenTrabajo(codigo, fechaIngreso, placa, tipoVehiculo,
                taller, servicio, mecanico, fechaEntrega, costo, estado));
    }

    private static String campo(String[] datos, int[] posiciones, int indice) {
        return datos[posiciones[indice]].trim();
    }

    /** Elige el separador que más aparece en el encabezado (fuera de comillas). */
    static char detectarSeparador(String encabezado) {
        char[] candidatos = {',', ';', '\t'};
        char mejor = ',';
        int mejorCuenta = 0;
        for (char c : candidatos) {
            int cuenta = dividirLinea(encabezado, c).length;
            if (cuenta > mejorCuenta) {
                mejorCuenta = cuenta;
                mejor = c;
            }
        }
        return mejor;
    }

    /**
     * Divide una línea respetando las comillas dobles: "a,b" es un solo campo
     * y "" dentro de comillas representa una comilla literal.
     */
    static String[] dividirLinea(String linea, char separador) {
        // Primera pasada: contar campos para crear el arreglo del tamaño exacto
        int campos = 1;
        boolean dentro = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') dentro = !dentro;
            else if (c == separador && !dentro) campos++;
        }

        String[] resultado = new String[campos];
        StringBuilder actual = new StringBuilder();
        int indice = 0;
        dentro = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (dentro && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"'); // comilla escapada
                    i++;
                } else {
                    dentro = !dentro;
                }
            } else if (c == separador && !dentro) {
                resultado[indice++] = actual.toString();
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        resultado[indice] = actual.toString();
        return resultado;
    }

    private static boolean comillasAbiertas(String linea) {
        int comillas = 0;
        for (int i = 0; i < linea.length(); i++) {
            if (linea.charAt(i) == '"') comillas++;
        }
        return comillas % 2 != 0;
    }

    /**
     * Relaciona cada dato del caso con su columna en el archivo, según el
     * nombre del encabezado. Si no se reconocen todos los nombres, se usa
     * el orden de columnas del enunciado.
     */
    static int[] mapearColumnas(String[] encabezados) {
        int[] posiciones = new int[TOTAL_COLUMNAS];
        for (int i = 0; i < TOTAL_COLUMNAS; i++) {
            posiciones[i] = -1;
        }

        for (int col = 0; col < encabezados.length; col++) {
            String n = normalizar(encabezados[col]);
            int campo = -1;
            if (n.contains("codigo") || n.equals("orden") || n.equals("id")) campo = CODIGO;
            else if (n.contains("fecha") && n.contains("entrega")) campo = FECHA_ENTREGA;
            else if (n.contains("fecha") || n.contains("ingreso")) campo = FECHA_INGRESO;
            else if (n.contains("placa")) campo = PLACA;
            else if (n.contains("tipo") || n.contains("vehiculo")) campo = TIPO_VEHICULO;
            else if (n.contains("taller") || n.contains("sede")) campo = TALLER;
            else if (n.contains("servicio")) campo = SERVICIO;
            else if (n.contains("mecanico") || n.contains("tecnico")) campo = MECANICO;
            else if (n.contains("entrega")) campo = FECHA_ENTREGA;
            else if (n.contains("costo") || n.contains("importe") || n.contains("monto")
                    || n.contains("total") || n.contains("precio")) campo = COSTO;
            else if (n.contains("estado")) campo = ESTADO;

            if (campo >= 0 && posiciones[campo] == -1) {
                posiciones[campo] = col;
            }
        }

        for (int i = 0; i < TOTAL_COLUMNAS; i++) {
            if (posiciones[i] == -1) {
                // Encabezado no reconocible: orden del enunciado
                for (int j = 0; j < TOTAL_COLUMNAS; j++) {
                    posiciones[j] = j;
                }
                break;
            }
        }
        return posiciones;
    }

    /**
     * Convierte un importe a double. Quita "S/", espacios y separadores de
     * miles. Si el texto tiene punto y coma, el último símbolo es el decimal.
     * Si solo tiene comas: en archivos separados por ';' (Excel en español)
     * la coma es decimal; en los demás, "1,200" se lee como mil doscientos.
     */
    static double convertirImporte(String texto, char separador) {
        String t = texto.trim().replace("S/.", "").replace("S/", "")
                .replace(" ", "").replace(" ", "");
        if (t.isEmpty()) {
            throw new NumberFormatException("vacío");
        }

        int ultimaComa = t.lastIndexOf(',');
        int ultimoPunto = t.lastIndexOf('.');
        if (ultimaComa >= 0 && ultimoPunto >= 0) {
            if (ultimaComa > ultimoPunto) {           // 1.200,50
                t = t.replace(".", "").replace(',', '.');
            } else {                                  // 1,200.50
                t = t.replace(",", "");
            }
        } else if (ultimaComa >= 0) {
            boolean pareceMiles = t.matches("\\d{1,3}(,\\d{3})+");
            if (separador != ';' && pareceMiles) {    // 1,200
                t = t.replace(",", "");
            } else {                                  // 1200,50
                t = t.replace(',', '.');
            }
        }
        if (!t.matches("-?\\d+(\\.\\d+)?")) { // descarta textos como "abc", "NaN" o "1e5"
            throw new NumberFormatException(texto);
        }
        return Double.parseDouble(t);
    }

    // Minúsculas, sin tildes ni símbolos: "Fecha de Ingreso" -> "fechadeingreso"
    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static int maximo(int[] arreglo) {
        int max = arreglo[0];
        for (int i = 1; i < arreglo.length; i++) {
            if (arreglo[i] > max) max = arreglo[i];
        }
        return max;
    }
}
