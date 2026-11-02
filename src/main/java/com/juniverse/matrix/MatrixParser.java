package com.juniverse.matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte texto en filas de números. Las filas se separan con {@code ;} o con saltos de línea y los elementos con espacios
 * o comas: {@code "1 2; 3 4"} es la matriz de 2x2 con filas (1, 2) y (3, 4).
 */
final class MatrixParser {

    private MatrixParser() {
    }

    /**
     * Separa el texto en filas y celdas.
     *
     * @param text texto de la matriz
     * @return celdas como texto, fila por fila
     * @throws IllegalArgumentException si el texto está vacío
     */
    static String[][] cells(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("El texto de la matriz está vacío");
        }
        List<String[]> rows = new ArrayList<>();
        for (String row : text.strip().split("[;\\n\\r]+")) {
            String trimmed = row.strip();
            if (!trimmed.isEmpty()) {
                rows.add(trimmed.split("[\\s,]+"));
            }
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("El texto de la matriz está vacío");
        }
        return rows.toArray(new String[0][]);
    }
}
