package com.juniverse.matrix;

import java.util.Arrays;

/**
 * Matriz de fracciones exactas, inmutable. Tiene las mismas operaciones que {@link Matrix} pero SIN errores de redondeo: la
 * inversa de [[1,2],[3,4]] es exactamente [[-2, 1], [3/2, -1/2]], no [[-2.0000000000000004, ...]]. Cuesta más memoria y
 * tiempo, así que se usa cuando importa la exactitud (matrices pequeñas, ejemplos, álgebra).
 */
public final class FractionMatrix {

    private final Fraction[][] data;
    private final int rows;
    private final int cols;

    /**
     * Crea una matriz copiando los valores recibidos.
     *
     * @param values filas rectangulares de fracciones
     * @throws IllegalArgumentException si está vacía o es irregular
     */
    public FractionMatrix(Fraction[][] values) {
        if (values == null || values.length == 0 || values[0] == null || values[0].length == 0) {
            throw new IllegalArgumentException("La matriz debe tener al menos una fila y una columna");
        }
        this.rows = values.length;
        this.cols = values[0].length;
        this.data = new Fraction[rows][cols];
        for (int i = 0; i < rows; i++) {
            if (values[i] == null || values[i].length != cols) {
                throw new IllegalArgumentException("Todas las filas deben tener el mismo número de columnas (fila " + (i + 1) + ")");
            }
            data[i] = values[i].clone();
        }
    }

    /**
     * Crea una matriz exacta desde una de {@code double} (cada decimal se convierte a la fracción que lo representa en texto).
     *
     * @param matrix matriz de números reales
     * @return la matriz de fracciones
     */
    public static FractionMatrix from(Matrix matrix) {
        Fraction[][] values = new Fraction[matrix.rows()][matrix.cols()];
        for (int i = 0; i < matrix.rows(); i++) {
            for (int j = 0; j < matrix.cols(); j++) {
                values[i][j] = Fraction.parse(Double.toString(matrix.get(i, j)));
            }
        }
        return new FractionMatrix(values);
    }

    /**
     * Crea una matriz exacta desde texto: {@code "1 2; 3/2 0.5"}.
     *
     * @param text texto de la matriz
     * @return la matriz
     */
    public static FractionMatrix parse(String text) {
        String[][] cells = MatrixParser.cells(text);
        Fraction[][] values = new Fraction[cells.length][];
        for (int i = 0; i < cells.length; i++) {
            values[i] = new Fraction[cells[i].length];
            for (int j = 0; j < cells[i].length; j++) {
                values[i][j] = Fraction.parse(cells[i][j]);
            }
        }
        return new FractionMatrix(values);
    }

    /**
     * Matriz identidad.
     *
     * @param n tamaño
     * @return la identidad de n x n
     */
    public static FractionMatrix identity(int n) {
        Fraction[][] values = new Fraction[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                values[i][j] = i == j ? Fraction.ONE : Fraction.ZERO;
            }
        }
        return new FractionMatrix(values);
    }

    /**
     * Número de filas.
     *
     * @return filas
     */
    public int rows() {
        return rows;
    }

    /**
     * Número de columnas.
     *
     * @return columnas
     */
    public int cols() {
        return cols;
    }

    /**
     * Elemento de la posición indicada.
     *
     * @param row fila (desde 0)
     * @param col columna (desde 0)
     * @return la fracción
     */
    public Fraction get(int row, int col) {
        return data[row][col];
    }

    /**
     * Suma exacta.
     *
     * @param other matriz a sumar
     * @return la suma
     */
    public FractionMatrix add(FractionMatrix other) {
        requireSameSize(other, "sumar");
        Fraction[][] r = new Fraction[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                r[i][j] = data[i][j].add(other.data[i][j]);
            }
        }
        return new FractionMatrix(r);
    }

    /**
     * Resta exacta.
     *
     * @param other matriz a restar
     * @return la diferencia
     */
    public FractionMatrix subtract(FractionMatrix other) {
        requireSameSize(other, "restar");
        Fraction[][] r = new Fraction[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                r[i][j] = data[i][j].subtract(other.data[i][j]);
            }
        }
        return new FractionMatrix(r);
    }

    /**
     * Multiplicación por un escalar exacto.
     *
     * @param scalar fracción por la que se multiplica
     * @return la matriz escalada
     */
    public FractionMatrix scale(Fraction scalar) {
        Fraction[][] r = new Fraction[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                r[i][j] = data[i][j].multiply(scalar);
            }
        }
        return new FractionMatrix(r);
    }

    /**
     * Producto exacto de matrices.
     *
     * @param other matriz por la derecha
     * @return el producto
     * @throws MatrixDimensionException si las columnas no coinciden con las filas de la otra
     */
    public FractionMatrix multiply(FractionMatrix other) {
        if (cols != other.rows) {
            throw new MatrixDimensionException("No se puede multiplicar una matriz " + rows + "x" + cols + " por una " + other.rows + "x" + other.cols);
        }
        Fraction[][] r = new Fraction[rows][other.cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < cols; k++) {
                    sum = sum.add(data[i][k].multiply(other.data[k][j]));
                }
                r[i][j] = sum;
            }
        }
        return new FractionMatrix(r);
    }

    /**
     * Transpuesta.
     *
     * @return la transpuesta (n x m)
     */
    public FractionMatrix transpose() {
        Fraction[][] r = new Fraction[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                r[j][i] = data[i][j];
            }
        }
        return new FractionMatrix(r);
    }

    /**
     * Determinante exacto por eliminación gaussiana. Con fracciones no hace falta elegir el pivote más grande (no hay error
     * numérico): basta con uno distinto de cero.
     *
     * @return el determinante
     * @throws MatrixDimensionException si no es cuadrada
     */
    public Fraction determinant() {
        requireSquare("calcular el determinante");
        Fraction[][] a = copy();
        Fraction det = Fraction.ONE;
        for (int col = 0; col < rows; col++) {
            int pivot = nonZeroRow(a, col);
            if (pivot < 0) {
                return Fraction.ZERO;
            }
            if (pivot != col) {
                Fraction[] tmp = a[pivot];
                a[pivot] = a[col];
                a[col] = tmp;
                det = det.negate();
            }
            det = det.multiply(a[col][col]);
            for (int i = col + 1; i < rows; i++) {
                Fraction factor = a[i][col].divide(a[col][col]);
                for (int j = col; j < rows; j++) {
                    a[i][j] = a[i][j].subtract(factor.multiply(a[col][j]));
                }
            }
        }
        return det;
    }

    /**
     * Inversa exacta por Gauss-Jordan sobre {@code [A | I]}.
     *
     * @return la inversa
     * @throws MatrixDimensionException si no es cuadrada
     * @throws SingularMatrixException si el determinante es cero
     */
    public FractionMatrix inverse() {
        requireSquare("calcular la inversa");
        int n = rows;
        Fraction[][] a = copy();
        Fraction[][] inv = identity(n).copy();
        for (int col = 0; col < n; col++) {
            int pivot = nonZeroRow(a, col);
            if (pivot < 0) {
                throw new SingularMatrixException("La matriz es singular (determinante 0): no tiene inversa");
            }
            Fraction[] tmp = a[pivot];
            a[pivot] = a[col];
            a[col] = tmp;
            tmp = inv[pivot];
            inv[pivot] = inv[col];
            inv[col] = tmp;
            Fraction pivotValue = a[col][col];
            for (int j = 0; j < n; j++) {
                a[col][j] = a[col][j].divide(pivotValue);
                inv[col][j] = inv[col][j].divide(pivotValue);
            }
            for (int i = 0; i < n; i++) {
                if (i != col && !a[i][col].isZero()) {
                    Fraction factor = a[i][col];
                    for (int j = 0; j < n; j++) {
                        a[i][j] = a[i][j].subtract(factor.multiply(a[col][j]));
                        inv[i][j] = inv[i][j].subtract(factor.multiply(inv[col][j]));
                    }
                }
            }
        }
        return new FractionMatrix(inv);
    }

    /**
     * Convierte a una matriz de {@code double}.
     *
     * @return la matriz decimal equivalente
     */
    public Matrix toMatrix() {
        double[][] values = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                values[i][j] = data[i][j].doubleValue();
            }
        }
        return new Matrix(values);
    }

    private Fraction[][] copy() {
        Fraction[][] c = new Fraction[rows][];
        for (int i = 0; i < rows; i++) {
            c[i] = data[i].clone();
        }
        return c;
    }

    private static int nonZeroRow(Fraction[][] a, int col) {
        for (int i = col; i < a.length; i++) {
            if (!a[i][col].isZero()) {
                return i;
            }
        }
        return -1;
    }

    private void requireSameSize(FractionMatrix other, String operation) {
        if (rows != other.rows || cols != other.cols) {
            throw new MatrixDimensionException("No se pueden " + operation + " matrices de distinto tamaño: " + rows + "x" + cols + " y " + other.rows + "x" + other.cols);
        }
    }

    private void requireSquare(String operation) {
        if (rows != cols) {
            throw new MatrixDimensionException("Solo una matriz cuadrada permite " + operation + " (esta es " + rows + "x" + cols + ")");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FractionMatrix other && Arrays.deepEquals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(data);
    }

    /**
     * Texto con la matriz alineada por columnas.
     *
     * @return representación en varias líneas
     */
    @Override
    public String toString() {
        String[][] text = new String[rows][cols];
        int[] width = new int[cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                text[i][j] = data[i][j].toString();
                width[j] = Math.max(width[j], text[i][j].length());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append(i == 0 ? "┌ " : i == rows - 1 ? "└ " : "│ ");
            for (int j = 0; j < cols; j++) {
                sb.append(" ".repeat(width[j] - text[i][j].length())).append(text[i][j]).append(j < cols - 1 ? "  " : "");
            }
            sb.append(i == 0 ? " ┐" : i == rows - 1 ? " ┘" : " │");
            if (i < rows - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return rows == 1 ? "[ " + sb.substring(2, sb.length() - 2) + " ]" : sb.toString();
    }
}
