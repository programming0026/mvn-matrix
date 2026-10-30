package com.juniverse.matrices;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Pruebas de {@link Matrix}. */
class MatrixTest {

    private static final double TOL = 1e-9;

    /** Matriz aleatoria diagonalmente dominante: siempre invertible. */
    private static Matrix invertible(int n, long seed) {
        Random random = new Random(seed);
        double[][] v = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                v[i][j] = random.nextDouble() * 2 - 1;
            }
            v[i][i] += n + 1;
        }
        return new Matrix(v);
    }

    private static Matrix random(int rows, int cols, long seed) {
        Random random = new Random(seed);
        double[][] v = new double[rows][cols];
        for (double[] row : v) {
            for (int j = 0; j < cols; j++) {
                row[j] = random.nextInt(21) - 10;
            }
        }
        return new Matrix(v);
    }

    @Nested
    @DisplayName("Construcción")
    class Construction {

        @Test
        void parsesTextInSeveralFormats() {
            assertEquals(Matrix.of(new double[] {1, 2}, new double[] {3, 4}), Matrix.parse("1 2; 3 4"));
            assertEquals(Matrix.of(new double[] {1, 2}, new double[] {3, 4}), Matrix.parse("1,2\n3,4"));
            assertEquals(Matrix.of(new double[] {0.5, -2.25}), Matrix.parse("1/2 -2.25"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "1 2; 3", "a b; c d", "1 2; 3 x", "1/0 2"})
        void rejectsInvalidText(String text) {
            assertThrows(IllegalArgumentException.class, () -> Matrix.parse(text));
        }

        @Test
        void rejectsInvalidData() {
            assertThrows(IllegalArgumentException.class, () -> new Matrix(new double[0][0]));
            assertThrows(IllegalArgumentException.class, () -> new Matrix(new double[][] {{1, 2}, {3}}));
            assertThrows(IllegalArgumentException.class, () -> new Matrix(new double[][] {{1, Double.NaN}}));
            assertThrows(IllegalArgumentException.class, () -> new Matrix(new double[][] {{Double.POSITIVE_INFINITY}}));
        }

        @Test
        void isImmutable() {
            double[][] source = {{1, 2}, {3, 4}};
            Matrix m = new Matrix(source);
            source[0][0] = 99;
            m.toArray()[1][1] = 99;
            assertEquals(1, m.get(0, 0));
            assertEquals(4, m.get(1, 1));
        }

        @Test
        void identityAndZeros() {
            assertEquals(Matrix.parse("1 0; 0 1"), Matrix.identity(2));
            assertEquals(Matrix.parse("0 0 0; 0 0 0"), Matrix.zeros(2, 3));
            assertThrows(IllegalArgumentException.class, () -> Matrix.identity(0));
        }
    }

    @Nested
    @DisplayName("Suma, resta y escalar")
    class AddSubtractScale {

        @Test
        void addsAndSubtracts() {
            Matrix a = Matrix.parse("1 2; 3 4");
            Matrix b = Matrix.parse("5 6; 7 8");
            assertEquals(Matrix.parse("6 8; 10 12"), a.add(b));
            assertEquals(Matrix.parse("-4 -4; -4 -4"), a.subtract(b));
            assertEquals(Matrix.zeros(2, 2), a.subtract(a));
        }

        @Test
        void additionIsCommutativeAndAssociative() {
            Matrix a = random(3, 4, 1);
            Matrix b = random(3, 4, 2);
            Matrix c = random(3, 4, 3);
            assertEquals(a.add(b), b.add(a));
            assertTrue(a.add(b).add(c).approxEquals(a.add(b.add(c)), TOL));
        }

        @Test
        void scales() {
            Matrix a = Matrix.parse("1 -2; 3 4");
            assertEquals(Matrix.parse("3 -6; 9 12"), a.scale(3));
            assertEquals(Matrix.parse("-1 2; -3 -4"), a.scale(-1));
            assertEquals(Matrix.zeros(2, 2), a.scale(0));
            assertEquals(Matrix.zeros(2, 2).hashCode(), a.scale(0).hashCode(), "-0.0 y 0.0 deben ser iguales");
            assertEquals(a, a.scale(1));
        }

        @Test
        void scalarDistributesOverAddition() {
            Matrix a = random(3, 3, 4);
            Matrix b = random(3, 3, 5);
            assertTrue(a.add(b).scale(2.5).approxEquals(a.scale(2.5).add(b.scale(2.5)), TOL));
        }

        @Test
        void rejectsDifferentSizes() {
            Matrix a = Matrix.parse("1 2; 3 4");
            Matrix b = Matrix.parse("1 2 3; 4 5 6");
            MatrixDimensionException e = assertThrows(MatrixDimensionException.class, () -> a.add(b));
            assertTrue(e.getMessage().contains("2x2") && e.getMessage().contains("2x3"));
            assertThrows(MatrixDimensionException.class, () -> a.subtract(b));
        }
    }

    @Nested
    @DisplayName("Multiplicación")
    class Multiplication {

        @Test
        void multipliesSquareMatrices() {
            assertEquals(Matrix.parse("19 22; 43 50"), Matrix.parse("1 2; 3 4").multiply(Matrix.parse("5 6; 7 8")));
        }

        @Test
        void multipliesRectangularMatrices() {
            Matrix c = Matrix.parse("1 0 2; -1 3 1");
            Matrix d = Matrix.parse("3 1; 2 1; 1 0");
            assertEquals(Matrix.parse("5 1; 4 2"), c.multiply(d));
            Matrix dc = d.multiply(c);
            assertEquals(3, dc.rows());
            assertEquals(3, dc.cols());
        }

        @Test
        void isNotCommutative() {
            Matrix a = Matrix.parse("1 2; 3 4");
            Matrix b = Matrix.parse("5 6; 7 8");
            assertNotEquals(a.multiply(b), b.multiply(a));
        }

        @Test
        void identityIsNeutralAndZeroAbsorbs() {
            Matrix a = random(3, 3, 6);
            assertEquals(a, a.multiply(Matrix.identity(3)));
            assertEquals(a, Matrix.identity(3).multiply(a));
            assertEquals(Matrix.zeros(3, 3), a.multiply(Matrix.zeros(3, 3)));
        }

        @Test
        void isAssociativeAndDistributive() {
            Matrix a = random(2, 3, 7);
            Matrix b = random(3, 4, 8);
            Matrix c = random(4, 2, 9);
            Matrix d = random(3, 4, 10);
            assertTrue(a.multiply(b).multiply(c).approxEquals(a.multiply(b.multiply(c)), TOL));
            assertTrue(a.multiply(b.add(d)).approxEquals(a.multiply(b).add(a.multiply(d)), TOL));
        }

        @Test
        void rejectsIncompatibleSizes() {
            Matrix a = Matrix.parse("1 2; 3 4");
            Matrix d = Matrix.parse("3 1; 2 1; 1 0");
            assertThrows(MatrixDimensionException.class, () -> a.multiply(d));
        }
    }

    @Nested
    @DisplayName("Transpuesta")
    class Transpose {

        @Test
        void swapsRowsAndColumns() {
            Matrix c = Matrix.parse("1 0 2; -1 3 1");
            Matrix t = c.transpose();
            assertEquals(3, t.rows());
            assertEquals(2, t.cols());
            assertEquals(Matrix.parse("1 -1; 0 3; 2 1"), t);
        }

        @Test
        void hasTheClassicProperties() {
            Matrix a = random(3, 4, 11);
            Matrix b = random(4, 2, 12);
            Matrix c = random(3, 4, 13);
            assertEquals(a, a.transpose().transpose());
            assertEquals(a.add(c).transpose(), a.transpose().add(c.transpose()));
            assertEquals(a.multiply(b).transpose(), b.transpose().multiply(a.transpose()));
            assertEquals(a.scale(3).transpose(), a.transpose().scale(3));
        }
    }

    @Nested
    @DisplayName("Determinante e inversa")
    class DeterminantAndInverse {

        @Test
        void computesDeterminants() {
            assertEquals(-2, Matrix.parse("1 2; 3 4").determinant(), TOL);
            assertEquals(5, Matrix.parse("5").determinant(), TOL);
            assertEquals(4, Matrix.parse("2 -1 0; -1 2 -1; 0 -1 2").determinant(), TOL);
            assertEquals(0, Matrix.parse("1 2; 2 4").determinant(), TOL);
            assertEquals(1, Matrix.identity(5).determinant(), TOL);
            assertEquals(-2, Matrix.parse("0 1; 1 0").determinant() * 2, TOL, "un intercambio de filas cambia el signo");
        }

        @Test
        void determinantOfProductIsProductOfDeterminants() {
            Matrix a = invertible(4, 14);
            Matrix b = invertible(4, 15);
            assertEquals(a.determinant() * b.determinant(), a.multiply(b).determinant(), 1e-6);
            assertEquals(a.determinant(), a.transpose().determinant(), 1e-6);
        }

        @Test
        void invertsATwoByTwo() {
            Matrix inverse = Matrix.parse("1 2; 3 4").inverse();
            assertTrue(Matrix.parse("-2 1; 1.5 -0.5").approxEquals(inverse, TOL));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 8, 12})
        void productWithInverseIsIdentity(int n) {
            Matrix a = invertible(n, 100 + n);
            assertTrue(a.multiply(a.inverse()).approxEquals(Matrix.identity(n), 1e-9));
            assertTrue(a.inverse().multiply(a).approxEquals(Matrix.identity(n), 1e-9));
            assertTrue(a.inverse().inverse().approxEquals(a, 1e-8));
        }

        @Test
        void usesPivotingWhenTheFirstPivotIsZero() {
            Matrix a = Matrix.parse("0 1; 1 0");
            assertEquals(a, a.inverse());
            Matrix b = Matrix.parse("0 2 1; 1 0 0; 3 0 1");
            assertTrue(b.multiply(b.inverse()).approxEquals(Matrix.identity(3), TOL));
        }

        @Test
        void inverseOfProductReversesTheOrder() {
            Matrix a = invertible(3, 16);
            Matrix b = invertible(3, 17);
            assertTrue(a.multiply(b).inverse().approxEquals(b.inverse().multiply(a.inverse()), 1e-9));
        }

        @Test
        void detectsSingularMatrices() {
            assertThrows(SingularMatrixException.class, () -> Matrix.parse("1 2; 2 4").inverse());
            assertThrows(SingularMatrixException.class, () -> Matrix.zeros(3, 3).inverse());
            assertThrows(SingularMatrixException.class, () -> Matrix.parse("1 2 3; 4 5 6; 7 8 9").inverse());
        }

        @Test
        void requiresSquareMatrices() {
            Matrix c = Matrix.parse("1 0 2; -1 3 1");
            assertThrows(MatrixDimensionException.class, c::inverse);
            assertThrows(MatrixDimensionException.class, c::determinant);
        }
    }

    @Test
    void formatsWithoutNoiseOrNegativeZero() {
        assertEquals("┌ 1  2 ┐" + System.lineSeparator() + "└ 3  4 ┘", Matrix.parse("1 2; 3 4").toString());
        assertEquals("[ 0.5  -1.25 ]", Matrix.parse("0.5 -1.25").toString());
        assertFalse(Matrix.of(new double[] {-0.0, 0.00000001}).toString().contains("-0"));
        assertArrayEquals(new double[][] {{1, 2}}, Matrix.parse("1 2").toArray());
    }
}
