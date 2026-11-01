package com.juniverse.matrices;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Número racional exacto (numerador / denominador) con enteros de precisión ilimitada, siempre simplificado y con
 * denominador positivo. Permite operar sin errores de redondeo: {@code 1/3 + 1/6 = 1/2} exactamente.
 */
public final class Fraction {

    /** El cero. */
    public static final Fraction ZERO = new Fraction(BigInteger.ZERO, BigInteger.ONE);
    /** El uno. */
    public static final Fraction ONE = new Fraction(BigInteger.ONE, BigInteger.ONE);

    private final BigInteger numerator;
    private final BigInteger denominator;

    private Fraction(BigInteger numerator, BigInteger denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Crea una fracción simplificada.
     *
     * @param numerator numerador
     * @param denominator denominador (no puede ser cero)
     * @return la fracción en su forma irreducible
     * @throws ArithmeticException si el denominador es cero
     */
    public static Fraction of(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() == 0) {
            throw new ArithmeticException("El denominador no puede ser cero");
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger gcd = numerator.gcd(denominator);
        if (gcd.signum() != 0 && !gcd.equals(BigInteger.ONE)) {
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }
        return new Fraction(numerator, denominator);
    }

    /**
     * Crea una fracción desde dos enteros.
     *
     * @param numerator numerador
     * @param denominator denominador
     * @return la fracción simplificada
     */
    public static Fraction of(long numerator, long denominator) {
        return of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    /**
     * Crea una fracción entera.
     *
     * @param value número entero
     * @return la fracción {@code value/1}
     */
    public static Fraction of(long value) {
        return new Fraction(BigInteger.valueOf(value), BigInteger.ONE);
    }

    /**
     * Lee una fracción desde texto: entero ({@code 5}), fracción ({@code -3/4}) o decimal ({@code 0.25}, que se convierte de
     * forma exacta a {@code 1/4}).
     *
     * @param text texto
     * @return la fracción
     * @throws IllegalArgumentException si el texto no es un número
     */
    public static Fraction parse(String text) {
        String s = text.strip();
        try {
            int slash = s.indexOf('/');
            if (slash >= 0) {
                return decimal(s.substring(0, slash)).divide(decimal(s.substring(slash + 1)));
            }
            return decimal(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' no es un número válido (usa 3, -2.5 o 1/2)");
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("'" + text + "' tiene un denominador cero");
        }
    }

    private static Fraction decimal(String s) {
        BigDecimal value = new BigDecimal(s.strip());
        BigInteger unscaled = value.unscaledValue();
        return value.scale() > 0
                ? of(unscaled, BigInteger.TEN.pow(value.scale()))
                : of(unscaled.multiply(BigInteger.TEN.pow(-value.scale())), BigInteger.ONE);
    }

    /**
     * Suma.
     *
     * @param o otra fracción
     * @return {@code this + o}
     */
    public Fraction add(Fraction o) {
        return of(numerator.multiply(o.denominator).add(o.numerator.multiply(denominator)), denominator.multiply(o.denominator));
    }

    /**
     * Resta.
     *
     * @param o otra fracción
     * @return {@code this - o}
     */
    public Fraction subtract(Fraction o) {
        return add(o.negate());
    }

    /**
     * Multiplicación.
     *
     * @param o otra fracción
     * @return {@code this * o}
     */
    public Fraction multiply(Fraction o) {
        return of(numerator.multiply(o.numerator), denominator.multiply(o.denominator));
    }

    /**
     * División.
     *
     * @param o divisor (no puede ser cero)
     * @return {@code this / o}
     * @throws ArithmeticException si el divisor es cero
     */
    public Fraction divide(Fraction o) {
        if (o.isZero()) {
            throw new ArithmeticException("División entre cero");
        }
        return of(numerator.multiply(o.denominator), denominator.multiply(o.numerator));
    }

    /**
     * Opuesto.
     *
     * @return {@code -this}
     */
    public Fraction negate() {
        return new Fraction(numerator.negate(), denominator);
    }

    public boolean isZero() {
        return numerator.signum() == 0;
    }

    /**
     * Valor aproximado como {@code double}.
     *
     * @return el valor decimal
     */
    public double doubleValue() {
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), java.math.MathContext.DECIMAL64).doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Fraction f && numerator.equals(f.numerator) && denominator.equals(f.denominator);
    }

    @Override
    public int hashCode() {
        return 31 * numerator.hashCode() + denominator.hashCode();
    }

    /**
     * Texto: {@code 5} si es entera o {@code 3/2} en otro caso.
     *
     * @return representación
     */
    @Override
    public String toString() {
        return denominator.equals(BigInteger.ONE) ? numerator.toString() : numerator + "/" + denominator;
    }
}
