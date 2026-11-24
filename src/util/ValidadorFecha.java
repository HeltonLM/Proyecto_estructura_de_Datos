package util;

/**
 * Valida y convierte las fechas que vienen como texto en el CSV.
 *
 * Las fechas se convierten a un entero con forma AAAAMMDD (por ejemplo,
 * 15/03/2026 -> 20260315). Así, comparar dos fechas es comparar dos números,
 * lo que simplifica los filtros por rango y el ordenamiento por fecha.
 */
public final class ValidadorFecha {

    public static final int FECHA_INVALIDA = -1;

    private ValidadorFecha() {
    }

    /**
     * Convierte un texto a AAAAMMDD. Acepta dd/MM/yyyy, d/M/yyyy, dd-MM-yyyy,
     * dd.MM.yyyy, yyyy-MM-dd y yyyy/MM/dd. Si trae hora ("15/03/2026 10:30"),
     * la hora se ignora. Devuelve FECHA_INVALIDA si no es una fecha real.
     */
    public static int aNumero(String texto) {
        if (texto == null) {
            return FECHA_INVALIDA;
        }
        String limpio = texto.trim();
        int espacio = limpio.indexOf(' ');
        if (espacio > 0) {
            limpio = limpio.substring(0, espacio);
        }
        if (limpio.isEmpty()) {
            return FECHA_INVALIDA;
        }

        String[] partes = limpio.split("[/\\-.]");
        if (partes.length != 3) {
            return FECHA_INVALIDA;
        }

        int a, b, c;
        try {
            a = Integer.parseInt(partes[0]);
            b = Integer.parseInt(partes[1]);
            c = Integer.parseInt(partes[2]);
        } catch (NumberFormatException e) {
            return FECHA_INVALIDA;
        }

        int dia, mes, anio;
        if (partes[0].length() == 4) {      // yyyy-MM-dd
            anio = a;
            mes = b;
            dia = c;
        } else {                            // dd/MM/yyyy
            dia = a;
            mes = b;
            anio = c;
            if (partes[2].length() <= 2) {  // año de dos dígitos: 26 -> 2026
                anio += 2000;
            }
        }

        if (!esFechaValida(dia, mes, anio)) {
            return FECHA_INVALIDA;
        }
        return anio * 10000 + mes * 100 + dia;
    }

    public static boolean esValida(String texto) {
        return aNumero(texto) != FECHA_INVALIDA;
    }

    /** Convierte AAAAMMDD a texto dd/MM/yyyy. */
    public static String formatear(int fechaNumero) {
        if (fechaNumero <= 0) {
            return "";
        }
        int anio = fechaNumero / 10000;
        int mes = (fechaNumero / 100) % 100;
        int dia = fechaNumero % 100;
        return (dia < 10 ? "0" : "") + dia + "/" + (mes < 10 ? "0" : "") + mes + "/" + anio;
    }

    /** Devuelve el mes de una fecha AAAAMMDD como texto "AAAA-MM". */
    public static String mesDe(int fechaNumero) {
        if (fechaNumero == FECHA_INVALIDA) {
            return "Sin fecha";
        }
        int anio = fechaNumero / 10000;
        int mes = (fechaNumero / 100) % 100;
        return anio + "-" + (mes < 10 ? "0" + mes : String.valueOf(mes));
    }

    private static boolean esFechaValida(int dia, int mes, int anio) {
        if (anio < 1900 || anio > 2100 || mes < 1 || mes > 12 || dia < 1) {
            return false;
        }
        int[] diasPorMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int maximo = diasPorMes[mes - 1];
        boolean bisiesto = (anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0;
        if (mes == 2 && bisiesto) {
            maximo = 29;
        }
        return dia <= maximo;
    }
}
