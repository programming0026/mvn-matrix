package com.juniverse.matrix;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

/** Pruebas del modo interactivo con entrada guionizada. */
class ConsoleAppTest {

    private static String run(String input) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ConsoleApp(new Scanner(input), new PrintStream(bytes, true, StandardCharsets.UTF_8)).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void addsAndMultiplies() {
        String output = run("1\n1 2; 3 4\n5 6; 7 8\n3\n1 2; 3 4\n5 6; 7 8\n0\n");
        assertTrue(output.contains("10  12"), "suma: segunda fila 10 12");
        assertTrue(output.contains("19  22"));
        assertTrue(output.contains("¡Hasta luego!"));
    }

    @Test
    void showsDecimalAndExactInverse() {
        String output = run("6\n1 2; 3 4\n0\n");
        assertTrue(output.contains("-2"));
        assertTrue(output.contains("3/2"));
        assertTrue(output.contains("-1/2"));
    }

    @Test
    void showsDeterminantAndScalar() {
        String output = run("7\n1 2; 3 4\n4\n1 2; 3 4\n1/2\n5\n1 2 3; 4 5 6\n0\n");
        assertTrue(output.contains("det(A) = -2   (exacto: -2)"));
        assertTrue(output.contains("1/2"));
        assertTrue(output.contains("┌ 1  4 ┐"), "la transpuesta de una 2x3 es 3x2");
    }

    @Test
    void reportsErrorsAndKeepsRunning() {
        String output = run("1\n1 2\n1 2 3\n6\n1 2; 2 4\n2\nabc\n9\n0\n");
        assertTrue(output.contains("Error: No se pueden sumar matrices de distinto tamaño"));
        assertTrue(output.contains("Error: La matriz es singular"));
        assertTrue(output.contains("Error: 'abc' no es un número válido"));
        assertTrue(output.contains("Opción no válida"));
        assertTrue(output.contains("¡Hasta luego!"));
    }

    @Test
    void endsGracefullyWhenInputRunsOut() {
        assertTrue(run("1\n1 2\n").contains("Matriz B"));
        assertTrue(run("").contains("Opción:"));
    }
}
