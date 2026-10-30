package com.juniverse.matrices;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Programa principal. Sin argumentos muestra una demostración de todas las operaciones; con {@code -i} abre el modo
 * interactivo para calcular con tus propias matrices.
 */
public final class Main {

    private Main() {
    }

    private static void title(PrintStream out, String text) {
        out.println();
        out.println("=========== " + text + " ===========");
    }

    private static void show(PrintStream out, String label, Object value) {
        out.println(label + " =");
        out.println(value);
        out.println();
    }

    /**
     * Punto de entrada.
     *
     * @param args {@code -i} (o {@code --interactivo}) para el modo interactivo
     */
    public static void main(String[] args) {
        // Salida en UTF-8 para que se vean bien los acentos y los símbolos de matriz en cualquier consola
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        if (args.length > 0 && (args[0].equals("-i") || args[0].equals("--interactivo"))) {
            new ConsoleApp(new Scanner(System.in, StandardCharsets.UTF_8), out).run();
            return;
        }
        demo(out);
    }

    private static void demo(PrintStream out) {
        Matrix a = Matrix.parse("1 2; 3 4");
        Matrix b = Matrix.parse("5 6; 7 8");
        show(out, "A", a);
        show(out, "B", b);

        title(out, "SUMA y RESTA (mismas dimensiones)");
        show(out, "A + B", a.add(b));
        show(out, "A - B", a.subtract(b));

        title(out, "MULTIPLICACIÓN POR UN ESCALAR");
        show(out, "3 × A", a.scale(3));
        show(out, "0.5 × A", a.scale(0.5));

        title(out, "MULTIPLICACIÓN (no es conmutativa)");
        show(out, "A × B", a.multiply(b));
        show(out, "B × A", b.multiply(a));
        Matrix c = Matrix.parse("1 0 2; -1 3 1");
        Matrix d = Matrix.parse("3 1; 2 1; 1 0");
        show(out, "C (2x3)", c);
        show(out, "D (3x2)", d);
        show(out, "C × D (2x2)", c.multiply(d));
        show(out, "D × C (3x3)", d.multiply(c));

        title(out, "TRANSPUESTA (filas por columnas)");
        show(out, "Cᵀ (3x2)", c.transpose());
        show(out, "(A × B)ᵀ", a.multiply(b).transpose());
        show(out, "Bᵀ × Aᵀ  (igual que la anterior)", b.transpose().multiply(a.transpose()));

        title(out, "DETERMINANTE E INVERSA");
        out.println("det(A) = " + a.determinant());
        show(out, "A⁻¹ (decimal)", a.inverse());
        show(out, "A⁻¹ (exacta, con fracciones)", FractionMatrix.from(a).inverse());
        show(out, "A × A⁻¹  (la identidad)", a.multiply(a.inverse()));

        Matrix e = Matrix.parse("2 -1 0; -1 2 -1; 0 -1 2");
        show(out, "E (3x3)", e);
        show(out, "E⁻¹ (exacta)", FractionMatrix.from(e).inverse());
        out.println("det(E) = " + FractionMatrix.from(e).determinant());
        out.println();

        title(out, "ERRORES CONTROLADOS");
        attempt(out, "Sumar 2x2 con 2x3", () -> a.add(c));
        attempt(out, "Multiplicar 2x2 por 3x2", () -> a.multiply(d));
        attempt(out, "Inversa de una matriz no cuadrada", () -> c.inverse());
        attempt(out, "Inversa de una matriz singular [1 2; 2 4]", () -> Matrix.parse("1 2; 2 4").inverse());
        out.println();
        out.println("Para calcular con tus propias matrices: java -jar <jar> -i");
    }

    private static void attempt(PrintStream out, String what, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            out.println(what + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
