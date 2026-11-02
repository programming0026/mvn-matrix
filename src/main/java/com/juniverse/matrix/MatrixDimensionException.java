package com.juniverse.matrix;

/** Las dimensiones de las matrices no permiten la operación (por ejemplo, sumar una 2x3 con una 3x2). */
public class MatrixDimensionException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción.
     *
     * @param message explicación con las dimensiones involucradas
     */
    public MatrixDimensionException(String message) {
        super(message);
    }
}
