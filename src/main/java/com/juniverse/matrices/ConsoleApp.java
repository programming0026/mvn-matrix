package com.juniverse.matrices;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Modo interactivo: un menú para elegir la operación e introducir las matrices por consola. Cada matriz se escribe en una
 * línea con las filas separadas por {@code ;}, por ejemplo {@code 1 2; 3 4}. La entrada y la salida se reciben como
 * parámetros para poder probarlo con texto guionizado.
 */
public final class ConsoleApp {

    private final Scanner in;
    private final PrintStream out;

    /**
     * Crea la aplicación.
     *
     * @param in origen de la entrada
     * @param out destino de la salida
     */
    public ConsoleApp(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    /** Muestra el menú y atiende opciones hasta que se elija salir o se acabe la entrada. */
    public void run() {
        out.println("Calculadora de matrices (escribe cada matriz en una línea, filas separadas por ';', p. ej. 1 2; 3 4)");
        while (true) {
            out.println();
            out.println("1) Sumar  2) Restar  3) Multiplicar  4) Multiplicar por un escalar  5) Transpuesta  6) Inversa  7) Determinante  0) Salir");
            out.print("Opción: ");
            if (!in.hasNextLine()) {
                return;
            }
            String option = in.nextLine().strip();
            if (option.equals("0")) {
                out.println("¡Hasta luego!");
                return;
            }
            try {
                execute(option);
            } catch (IllegalArgumentException | ArithmeticException e) { // incluye MatrixDimensionException y SingularMatrixException
                out.println("Error: " + e.getMessage());
            } catch (java.util.NoSuchElementException e) {
                return;
            }
        }
    }

    private void execute(String option) {
        switch (option) {
            case "1" -> show("A + B", read("Matriz A").add(read("Matriz B")));
            case "2" -> show("A - B", read("Matriz A").subtract(read("Matriz B")));
            case "3" -> show("A × B", read("Matriz A").multiply(read("Matriz B")));
            case "4" -> {
                Matrix a = read("Matriz A");
                Fraction k = Fraction.parse(ask("Escalar (p. ej. 3, -2.5 o 1/2)"));
                show("k × A", a.scale(k.doubleValue()));
                out.println("Exacto:" + System.lineSeparator() + FractionMatrix.from(a).scale(k));
            }
            case "5" -> show("Aᵀ", read("Matriz A").transpose());
            case "6" -> {
                String text = ask("Matriz A");
                show("A⁻¹ (decimal)", Matrix.parse(text).inverse());
                out.println("A⁻¹ (exacta, con fracciones):" + System.lineSeparator() + FractionMatrix.parse(text).inverse());
            }
            case "7" -> {
                String text = ask("Matriz A");
                out.println("det(A) = " + format(Matrix.parse(text).determinant()) + "   (exacto: " + FractionMatrix.parse(text).determinant() + ")");
            }
            default -> out.println("Opción no válida");
        }
    }

    private Matrix read(String name) {
        return Matrix.parse(ask(name));
    }

    private String ask(String prompt) {
        out.print(prompt + ": ");
        return in.nextLine();
    }

    private void show(String title, Matrix result) {
        out.println(title + " =" + System.lineSeparator() + result);
    }

    private static String format(double value) {
        return java.math.BigDecimal.valueOf(value).setScale(6, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
