# Operaciones con matrices en Java 21

Calculadora de matrices con **Maven** y **Java 21**, sin librerías externas: suma, resta, multiplicación, multiplicación por un escalar, transpuesta, determinante e **inversa**. Incluye una versión con números de coma flotante (`double`) y otra con **fracciones exactas**, y un modo interactivo para usar tus propias matrices.

## Las operaciones

| Operación | Fórmula | Requisito | Método |
|-----------|---------|-----------|--------|
| Suma | `C[i][j] = A[i][j] + B[i][j]` | Mismas dimensiones | `add` |
| Resta | `C[i][j] = A[i][j] − B[i][j]` | Mismas dimensiones | `subtract` |
| Por un escalar | `C[i][j] = k · A[i][j]` | Ninguno | `scale` |
| Multiplicación | `C[i][j] = Σₖ A[i][k] · B[k][j]` | Columnas de A = filas de B | `multiply` |
| Transpuesta | `Aᵀ[j][i] = A[i][j]` | Ninguno (m×n pasa a n×m) | `transpose` |
| Determinante | eliminación gaussiana, O(n³) | Cuadrada | `determinant` |
| Inversa | Gauss-Jordan sobre `[A \| I]` → `[I \| A⁻¹]` | Cuadrada y det ≠ 0 | `inverse` |

Ejemplo:

```text
A = ┌ 1  2 ┐    B = ┌ 5  6 ┐    A × B = ┌ 19  22 ┐    A⁻¹ = ┌  -2     1 ┐
    └ 3  4 ┘        └ 7  8 ┘            └ 43  50 ┘          └ 3/2  -1/2 ┘
```

Cosas que conviene recordar:

- **La multiplicación no es conmutativa**: en general `A·B ≠ B·A` (la demostración lo muestra). Sí es asociativa y distributiva.
- `(A·B)ᵀ = Bᵀ·Aᵀ` y `(A·B)⁻¹ = B⁻¹·A⁻¹`: **se invierte el orden**.
- Una matriz tiene inversa solo si es cuadrada y su determinante es distinto de cero; si no, es **singular**.
- La inversa se calcula con **pivoteo parcial** (se elige el mayor valor de la columna como pivote) para reducir el error numérico, y se calcula como `Gauss-Jordan`, no con la fórmula de cofactores, que es O(n!) y inservible para matrices medianas.

## `double` frente a fracciones

| | `Matrix` (`double`) | `FractionMatrix` (`Fraction`) |
|---|---|---|
| Precisión | Aproximada (errores de redondeo) | **Exacta** |
| Velocidad y memoria | Rápida | Más lenta (números enteros de precisión ilimitada) |
| Inversa de [[1,2],[3,4]] | `-2.0000000000000004 ...` (según el caso) | `[[-2, 1], [3/2, -1/2]]` |
| Uso típico | Cálculo numérico, matrices grandes | Álgebra, ejemplos, matrices pequeñas y mal condicionadas |

Con `double`, `0.1 + 0.2 != 0.3`, por eso `Matrix.approxEquals(otra, tolerancia)` compara con margen de error. Con `Fraction`, `1/10 + 2/10 = 3/10` exactamente. Las pruebas incluyen la **matriz de Hilbert** 6×6, famosa por su mal condicionamiento: con fracciones `H · H⁻¹` da exactamente la identidad.

## Estructura

```text
src/main/java/com/juniverse/matrix/
├── Matrix.java                    # matriz de double, inmutable, con todas las operaciones
├── FractionMatrix.java            # misma API con fracciones exactas
├── Fraction.java                  # número racional (BigInteger), siempre simplificado
├── MatrixParser.java              # texto -> celdas ("1 2; 3 4")
├── MatrixDimensionException.java  # dimensiones incompatibles
├── SingularMatrixException.java   # matriz sin inversa
├── ConsoleApp.java                # modo interactivo (menú)
└── Main.java                      # demostración o modo interactivo
```

Decisiones de diseño: las matrices son **inmutables** (cada operación devuelve una nueva y se copian los datos al construir), se validan las dimensiones antes de operar con mensajes que indican los tamaños, y `-0.0` se normaliza a `0.0` para que `equals` y `hashCode` sean coherentes.

## Requisitos

- JDK 21 y Maven 3.9 o superior.

## Ejecutar

```bash
mvn clean package
java -jar target/mvn-matrix-1.0.0-SNAPSHOT-launcher.jar          # demostración de todas las operaciones
java -jar target/mvn-matrix-1.0.0-SNAPSHOT-launcher.jar -i       # modo interactivo
```

En el modo interactivo eliges la operación en un menú y escribes cada matriz en una línea, con las filas separadas por `;` y los elementos por espacios o comas. Se aceptan enteros, decimales (`-2.5`) y fracciones (`1/2`):

```text
Opción: 6
Matriz A: 2 -1 0; -1 2 -1; 0 -1 2
A⁻¹ (decimal) = ...
A⁻¹ (exacta, con fracciones): ┌ 3/4 1/2 1/4 ┐ ...
```

Los errores (dimensiones incompatibles, matriz singular, texto que no es un número) se muestran como mensajes y el programa sigue funcionando.

También puedes usarlo como biblioteca:

```java
Matrix a = Matrix.parse("1 2; 3 4");
Matrix b = Matrix.of(new double[] {5, 6}, new double[] {7, 8});

Matrix suma = a.add(b);
Matrix producto = a.multiply(b);
Matrix inversa = a.inverse();
FractionMatrix exacta = FractionMatrix.from(a).inverse();   // [[-2, 1], [3/2, -1/2]]
```

## Pruebas

```bash
mvn test
```

57 pruebas JUnit 5. Además de casos concretos, comprueban **propiedades matemáticas** con matrices aleatorias (con semilla fija para que sean reproducibles): la suma es conmutativa y asociativa, el escalar distribuye sobre la suma, la multiplicación es asociativa y distributiva, `A·I = A`, `(A·B)ᵀ = Bᵀ·Aᵀ`, `det(A·B) = det(A)·det(B)`, `A·A⁻¹ = I` para tamaños de 1 a 12, `(A·B)⁻¹ = B⁻¹·A⁻¹`, y que la versión exacta coincide con la de `double`. También cubren los errores (dimensiones, matrices singulares, texto inválido) y el modo interactivo con entrada guionizada.

## Para ir más lejos

- Para matrices grandes, la multiplicación ingenua es O(n³): existen algoritmos como Strassen (O(n^2.81), ver el ejemplo `mvn-strassen`) o librerías con BLAS optimizado (EJML, ND4J).
- Para resolver `A·x = b` es mejor una descomposición LU que calcular la inversa.
- Una matriz dispersa (casi todo ceros) se guarda mejor en un formato especial (CSR) que en un arreglo de `double[][]`.

## Licencia

Código de ejemplo de uso libre con fines educativos.
