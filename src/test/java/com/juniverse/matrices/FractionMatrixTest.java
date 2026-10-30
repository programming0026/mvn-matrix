package com.juniverse.matrices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Pruebas de {@link Fraction} y {@link FractionMatrix}: aritmética exacta. */
class FractionMatrixTest {

    @ParameterizedTest
    @CsvSource({"1/3, 1/6, 1/2", "1/2, 1/2, 1", "-1/2, 1/4, -1/4", "0.25, 0.75, 1", "2, 1/3, 7/3"})
    void addsExactly(String a, String b, String expected) {
        assertEquals(Fraction.parse(expected), Fraction.parse(a).add(Fraction.parse(b)));
    }

    @Test
    void normalizesFractions() {
        assertEquals("1/2", Fraction.of(2, 4).toString());
        assertEquals("-1/2", Fraction.of(1, -2).toString());
        assertEquals("3", Fraction.of(6, 2).toString());
        assertEquals("0", Fraction.of(0, 5).toString());
        assertEquals(Fraction.of(1, 3), Fraction.of(-2, -6));
    }

    @Test
    void parsesDecimalsExactly() {
        assertEquals(Fraction.of(1, 4), Fraction.parse("0.25"));
        assertEquals(Fraction.of(-5, 2), Fraction.parse("-2.5"));
        assertEquals(Fraction.of(1200), Fraction.parse("1.2E3"));
        assertThrows(IllegalArgumentException.class, () -> Fraction.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> Fraction.parse("1/0"));
    }

    @Test
    void avoidsTheFloatingPointErrorOfDoubles() {
        assertTrue(0.1 + 0.2 != 0.3);
        assertEquals(Fraction.parse("0.3"), Fraction.parse("0.1").add(Fraction.parse("0.2")));
    }

    @Test
    void inverseOfTwoByTwoIsExact() {
        FractionMatrix inverse = FractionMatrix.parse("1 2; 3 4").inverse();
        assertEquals(FractionMatrix.parse("-2 1; 3/2 -1/2"), inverse);
        assertEquals(FractionMatrix.identity(2), FractionMatrix.parse("1 2; 3 4").multiply(inverse));
    }

    @Test
    void inverseOfTridiagonalIsExactAndHasFractions() {
        FractionMatrix e = FractionMatrix.parse("2 -1 0; -1 2 -1; 0 -1 2");
        assertEquals(FractionMatrix.parse("3/4 1/2 1/4; 1/2 1 1/2; 1/4 1/2 3/4"), e.inverse());
        assertEquals(Fraction.of(4), e.determinant());
    }

    @Test
    void hilbertMatrixIsInvertedWithoutLoss() {
        // La matriz de Hilbert es famosa por su mal condicionamiento: con double pierde precisión, con fracciones no
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            for (int j = 1; j <= 6; j++) {
                text.append("1/").append(i + j - 1).append(j < 6 ? " " : "");
            }
            text.append(i < 6 ? "; " : "");
        }
        FractionMatrix hilbert = FractionMatrix.parse(text.toString());
        assertEquals(FractionMatrix.identity(6), hilbert.multiply(hilbert.inverse()));
    }

    @Test
    void supportsAllOperations() {
        FractionMatrix a = FractionMatrix.parse("1/2 1/3; 1/4 1/5");
        FractionMatrix b = FractionMatrix.parse("1/2 2/3; 3/4 4/5");
        assertEquals(FractionMatrix.parse("1 1; 1 1"), a.add(b));
        assertEquals(FractionMatrix.parse("0 -1/3; -1/2 -3/5"), a.subtract(b));
        assertEquals(FractionMatrix.parse("1 2/3; 1/2 2/5"), a.scale(Fraction.of(2)));
        assertEquals(FractionMatrix.parse("1/2 1/4; 1/3 1/5"), a.transpose());
        assertEquals(a, a.transpose().transpose());
        assertEquals(a.multiply(b).transpose(), b.transpose().multiply(a.transpose()));
    }

    @Test
    void determinantMatchesTheDoubleVersion() {
        Matrix m = Matrix.parse("2 1 3; 0 -1 4; 5 2 1");
        assertEquals(m.determinant(), FractionMatrix.from(m).determinant().doubleValue(), 1e-9);
        assertEquals(Fraction.of(-2), FractionMatrix.parse("1 2; 3 4").determinant());
    }

    @Test
    void detectsErrors() {
        assertThrows(SingularMatrixException.class, () -> FractionMatrix.parse("1 2; 2 4").inverse());
        assertEquals(Fraction.ZERO, FractionMatrix.parse("1 2; 2 4").determinant());
        assertThrows(MatrixDimensionException.class, () -> FractionMatrix.parse("1 2 3").inverse());
        assertThrows(MatrixDimensionException.class, () -> FractionMatrix.parse("1 2").add(FractionMatrix.parse("1 2 3")));
        assertThrows(MatrixDimensionException.class, () -> FractionMatrix.parse("1 2; 3 4").multiply(FractionMatrix.parse("1 2 3")));
    }

    @Test
    void convertsBetweenRepresentations() {
        Matrix m = Matrix.parse("0.5 -1.25; 2 3");
        assertEquals(m, FractionMatrix.from(m).toMatrix());
        assertEquals("┌   1  1/2 ┐" + System.lineSeparator() + "└ 3/2   -1 ┘", FractionMatrix.parse("1 1/2; 3/2 -1").toString());
    }
}
