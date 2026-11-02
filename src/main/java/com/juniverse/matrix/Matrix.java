package com.juniverse.matrix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Matriz de números reales (tipo {@code double}), <b>inmutable</b>: cada operación devuelve una matriz nueva y la original
 * nunca cambia. Implementa suma, resta, multiplicación, multiplicación por un escalar, transpuesta, determinante e inversa.
 *
 * <p>Al usar {@code double} hay errores de redondeo (por ejemplo {@code 0.1 + 0.2 != 0.3}); para resultados exactos
 * con fracciones usa {@link FractionMatrix}.</p>
 */
public final class Matrix {

    /** Tolerancia por debajo de la cual un pivote se considera cero (matriz singular). */
    public static final double EPSILON = 1e-12;

    private final double[][] data;
    private final int rows;
    private final int cols;

    /**
     * Crea una matriz copiando los valores recibidos.
     *
     * @param values filas de la matriz; deben ser rectangulares y finitas
     * @throws IllegalArgumentException si está vacía, las filas tienen distinto largo o hay NaN o infinitos
     */
    public Matrix(double[][] values) {
        if (values == null || values.length == 0 || values[0] == null || values[0].length == 0) {
            throw new IllegalArgumentException("La matriz debe tener al menos una fila y una columna");
        }
        this.rows = values.length;
        this.cols = values[0].length;
        this.data = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            if (values[i] == null || values[i].length != cols) {
                throw new IllegalArgumentException("Todas las filas deben tener el mismo número de columnas (fila " + (i + 1) + ")");
            }
            for (int j = 0; j < cols; j++) {
                if (!Double.isFinite(values[i][j])) {
                    throw new IllegalArgumentException("La posición (" + (i + 1) + "," + (j + 1) + ") no es un número finito");
                }
                data[i][j] = values[i][j] + 0.0; // normaliza -0.0 a 0.0 para que equals() y hashCode() traten ambos ceros como iguales
            }
        }
    }

    /**
     * Crea una matriz a partir de sus filas.
     *
     * @param rows filas
     * @return la matriz
     */
    public static Matrix of(double[]... rows) {
        return new Matrix(rows);
    }

    /**
     * Matriz identidad: unos en la diagonal y ceros en el resto. Es el elemento neutro de la multiplicación.
     *
     * @param n tamaño (n x n)
     * @return la identidad
     */
    public static Matrix identity(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("El tamaño debe ser al menos 1");
        }
        double[][] values = new double[n][n];
        for (int i = 0; i < n; i++) {
            values[i][i] = 1;
        }
        return new Matrix(values);
    }

    /**
     * Matriz de ceros.
     *
     * @param rows filas
     * @param cols columnas
     * @return la matriz nula
     */
    public static Matrix zeros(int rows, int cols) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Las dimensiones deben ser al menos 1x1");
        }
        return new Matrix(new double[rows][cols]);
    }

    /**
     * Crea una matriz desde texto: {@code "1 2; 3 4"} o una fila por línea. También acepta fracciones como {@code 1/2}.
     *
     * @param text texto de la matriz
     * @return la matriz
     * @throws IllegalArgumentException si el texto no es una matriz válida
     */
    public static Matrix parse(String text) {
        String[][] cells = MatrixParser.cells(text);
        double[][] values = new double[cells.length][];
        for (int i = 0; i < cells.length; i++) {
            values[i] = new double[cells[i].length];
            for (int j = 0; j < cells[i].length; j++) {
                values[i][j] = Fraction.parse(cells[i][j]).doubleValue();
            }
        }
        return new Matrix(values);
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
     * Indica si es cuadrada (mismas filas que columnas). Solo las cuadradas tienen determinante e inversa.
     *
     * @return {@code true} si es cuadrada
     */
    public boolean isSquare() {
        return rows == cols;
    }

    /**
     * Elemento de la posición indicada.
     *
     * @param row fila (desde 0)
     * @param col columna (desde 0)
     * @return el valor
     */
    public double get(int row, int col) {
        return data[row][col];
    }

    /**
     * Copia de los datos (modificarla no afecta a la matriz).
     *
     * @return arreglo bidimensional nuevo
     */
    public double[][] toArray() {
        double[][] copy = new double[rows][];
        for (int i = 0; i < rows; i++) {
            copy[i] = data[i].clone();
        }
        return copy;
    }

    /**
     * Suma: {@code C[i][j] = A[i][j] + B[i][j]}. Requiere matrices de las mismas dimensiones.
     *
     * @param other matriz a sumar
     * @return la suma
     * @throws MatrixDimensionException si las dimensiones difieren
     */
    public Matrix add(Matrix other) {
        requireSameSize(other, "sumar");
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j] + other.data[i][j];
            }
        }
        return new Matrix(result);
    }

    /**
     * Resta: {@code C[i][j] = A[i][j] - B[i][j]}. Requiere matrices de las mismas dimensiones.
     *
     * @param other matriz a restar
     * @return la diferencia
     * @throws MatrixDimensionException si las dimensiones difieren
     */
    public Matrix subtract(Matrix other) {
        requireSameSize(other, "restar");
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j] - other.data[i][j];
            }
        }
        return new Matrix(result);
    }

    /**
     * Multiplicación por un escalar: cada elemento se multiplica por el mismo número.
     *
     * @param scalar número por el que se multiplica
     * @return la matriz escalada
     */
    public Matrix scale(double scalar) {
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j] * scalar;
            }
        }
        return new Matrix(result);
    }

    /**
     * Producto de matrices: {@code C[i][j] = suma sobre k de A[i][k] * B[k][j]}. El número de columnas de A debe ser igual al
     * número de filas de B, y el resultado tiene las filas de A y las columnas de B. NO es conmutativo: en general A·B ≠ B·A.
     *
     * @param other matriz por la derecha
     * @return el producto
     * @throws MatrixDimensionException si las columnas de esta matriz no coinciden con las filas de la otra
     */
    public Matrix multiply(Matrix other) {
        if (cols != other.rows) {
            throw new MatrixDimensionException("No se puede multiplicar una matriz " + rows + "x" + cols + " por una " + other.rows + "x" + other.cols
                    + ": las columnas de la primera (" + cols + ") deben ser iguales a las filas de la segunda (" + other.rows + ")");
        }
        double[][] result = new double[rows][other.cols];
        for (int i = 0; i < rows; i++) {
            for (int k = 0; k < cols; k++) {      // orden i-k-j: recorre las filas de B en secuencia (mejor uso de la caché)
                double a = data[i][k];
                for (int j = 0; j < other.cols; j++) {
                    result[i][j] += a * other.data[k][j];
                }
            }
        }
        return new Matrix(result);
    }

    /**
     * Transpuesta: intercambia filas por columnas, {@code T[j][i] = A[i][j]}. Una matriz de m x n pasa a n x m.
     *
     * @return la transpuesta
     */
    public Matrix transpose() {
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = data[i][j];
            }
        }
        return new Matrix(result);
    }

    /**
     * Determinante, calculado por eliminación gaussiana con pivoteo parcial (O(n³)); mucho más eficiente que la expansión
     * por cofactores (O(n!)). Cada intercambio de filas cambia el signo.
     *
     * @return el determinante
     * @throws MatrixDimensionException si la matriz no es cuadrada
     */
    public double determinant() {
        requireSquare("calcular el determinante");
        double[][] a = toArray();
        double det = 1;
        for (int col = 0; col < rows; col++) {
            int pivot = pivotRow(a, col);
            if (Math.abs(a[pivot][col]) < EPSILON) {
                return 0;
            }
            if (pivot != col) {
                double[] tmp = a[pivot];
                a[pivot] = a[col];
                a[col] = tmp;
                det = -det;
            }
            det *= a[col][col];
            for (int i = col + 1; i < rows; i++) {
                double factor = a[i][col] / a[col][col];
                for (int j = col; j < rows; j++) {
                    a[i][j] -= factor * a[col][j];
                }
            }
        }
        return det;
    }

    /**
     * Inversa por el método de Gauss-Jordan: se parte de la matriz ampliada {@code [A | I]} y se aplican operaciones de fila
     * hasta obtener {@code [I | A⁻¹]}. Se usa pivoteo parcial (el mayor valor de la columna) para reducir el error numérico.
     *
     * @return la inversa, tal que {@code A · A⁻¹ = I}
     * @throws MatrixDimensionException si la matriz no es cuadrada
     * @throws SingularMatrixException si el determinante es cero (no existe la inversa)
     */
    public Matrix inverse() {
        requireSquare("calcular la inversa");
        int n = rows;
        double[][] a = toArray();
        double[][] inv = identity(n).toArray();
        for (int col = 0; col < n; col++) {
            int pivot = pivotRow(a, col);
            if (Math.abs(a[pivot][col]) < EPSILON) {
                throw new SingularMatrixException("La matriz es singular (determinante 0): no tiene inversa");
            }
            swap(a, pivot, col);
            swap(inv, pivot, col);
            double pivotValue = a[col][col];
            for (int j = 0; j < n; j++) {         // la fila del pivote se divide para que el pivote valga 1
                a[col][j] /= pivotValue;
                inv[col][j] /= pivotValue;
            }
            for (int i = 0; i < n; i++) {         // se anula el resto de la columna
                if (i != col && a[i][col] != 0) {
                    double factor = a[i][col];
                    for (int j = 0; j < n; j++) {
                        a[i][j] -= factor * a[col][j];
                        inv[i][j] -= factor * inv[col][j];
                    }
                }
            }
        }
        return new Matrix(inv);
    }

    private static int pivotRow(double[][] a, int col) {
        int best = col;
        for (int i = col + 1; i < a.length; i++) {
            if (Math.abs(a[i][col]) > Math.abs(a[best][col])) {
                best = i;
            }
        }
        return best;
    }

    private static void swap(double[][] a, int i, int j) {
        double[] tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private void requireSameSize(Matrix other, String operation) {
        if (rows != other.rows || cols != other.cols) {
            throw new MatrixDimensionException("No se pueden " + operation + " matrices de distinto tamaño: " + rows + "x" + cols + " y " + other.rows + "x" + other.cols);
        }
    }

    private void requireSquare(String operation) {
        if (!isSquare()) {
            throw new MatrixDimensionException("Solo una matriz cuadrada permite " + operation + " (esta es " + rows + "x" + cols + ")");
        }
    }

    /**
     * Compara con otra matriz admitiendo un margen de error, necesario con {@code double} porque tras operar casi nunca
     * se obtienen valores exactamente iguales.
     *
     * @param other otra matriz
     * @param tolerance diferencia máxima permitida por elemento
     * @return {@code true} si tienen el mismo tamaño y todos los elementos difieren menos que la tolerancia
     */
    public boolean approxEquals(Matrix other, double tolerance) {
        if (rows != other.rows || cols != other.cols) {
            return false;
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.abs(data[i][j] - other.data[i][j]) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Matrix other && Arrays.deepEquals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(data);
    }

    /**
     * Texto con la matriz alineada por columnas, redondeando a 4 decimales y sin ceros sobrantes.
     *
     * @return representación en varias líneas
     */
    @Override
    public String toString() {
        return format(4);
    }

    /**
     * Texto con la matriz alineada por columnas.
     *
     * @param decimals decimales máximos a mostrar
     * @return representación en varias líneas
     */
    public String format(int decimals) {
        String[][] text = new String[rows][cols];
        int[] width = new int[cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                text[i][j] = number(data[i][j], decimals);
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

    private static String number(double value, int decimals) {
        BigDecimal rounded = BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).stripTrailingZeros();
        if (rounded.signum() == 0) {
            return "0"; // evita mostrar "-0"
        }
        return rounded.toPlainString();
    }
}
