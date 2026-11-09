package servicios;

import estructuras.ListaOrdenes;
import modelo.OrdenTrabajo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LectorCSV {

    public static ListaOrdenes cargarArchivo(File archivo) throws IOException {
        ListaOrdenes lista = new ListaOrdenes();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea = br.readLine(); // Salta la primera fila de encabezados

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                // Detecta si está separado por coma o punto y coma
                String separador = linea.contains(";") ? ";" : ",";
                String[] datos = linea.split(separador);

                if (datos.length >= 10) {
                    String codigo = datos[0].trim();
                    String fechaIngreso = datos[1].trim();
                    String placa = datos[2].trim();
                    String tipoVehiculo = datos[3].trim();
                    String taller = datos[4].trim();
                    String servicio = datos[5].trim();
                    String mecanico = datos[6].trim();
                    String fechaEntrega = datos[7].trim();
                    double costo = Double.parseDouble(datos[8].trim());
                    String estado = datos[9].trim();

                    OrdenTrabajo orden = new OrdenTrabajo(
                            codigo, fechaIngreso, placa, tipoVehiculo, taller,
                            servicio, mecanico, fechaEntrega, costo, estado
                    );
                    lista.insertarFinal(orden);
                }
            }
        }
        return lista;
    }
}