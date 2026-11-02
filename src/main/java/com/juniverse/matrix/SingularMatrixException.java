package com.juniverse.matrix;

/** La matriz es singular (determinante cero): no tiene inversa. */
public class SingularMatrixException extends ArithmeticException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción.
     *
     * @param message explicación
     */
    public SingularMatrixException(String message) {
        super(message);
    }
}
