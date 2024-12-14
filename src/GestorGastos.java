import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class GestorGastos {

    private static final String ARCHIVO_GASTOS = "gastos.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<Gasto> gastos = new ArrayList<>();

    public static void main(String[] args) {
        cargarGastos();
        Scanner scanner = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n--- Gestor de Gastos Personales ---");
            System.out.println("1. Añadir gasto");
            System.out.println("2. Ver todos los gastos");
            System.out.println("3. Calcular total de gastos");
            System.out.println("4. Ver gastos por categoría");
            System.out.println("5. Editar un gasto");
            System.out.println("6. Eliminar un gasto");
            System.out.println("7. Buscar gastos por rango de fechas");
            System.out.println("8. Exportar gastos a CSV");
            System.out.println("0. Salir");
            System.out.print("Elige una opción: ");
            opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea

            switch (opcion) {
                case 1 -> anadirGasto(scanner);
                case 2 -> verGastos();
                case 3 -> calcularTotalGastos();
                case 4 -> verGastosPorCategoria(scanner);
                case 5 -> editarGasto(scanner);
                case 6 -> eliminarGasto(scanner);
                case 7 -> buscarPorRangoDeFechas(scanner);
                case 8 -> exportarACSV();
                case 0 -> System.out.println("¡Hasta luego!");
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);

        guardarGastos();
        scanner.close();
    }

    private static void cargarGastos() {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(ARCHIVO_GASTOS))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                LocalDate fecha = LocalDate.parse(partes[0], DATE_FORMATTER);
                String categoria = partes[1];
                String descripcion = partes[2];
                double cantidad = Double.parseDouble(partes[3]);
                gastos.add(new Gasto(fecha, categoria, descripcion, cantidad));
            }
        } catch (IOException | DateTimeParseException e) {
            System.out.println("No se pudo cargar el archivo de gastos: " + e.getMessage());
        }
    }

    private static void guardarGastos() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_GASTOS))) {
            for (Gasto gasto : gastos) {
                writer.println(gasto.toCSV());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar los gastos: " + e.getMessage());
        }
    }

    private static void anadirGasto(Scanner scanner) {
        try {
            System.out.print("Introduce la fecha (dd/MM/yyyy): ");
            LocalDate fecha = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);
            System.out.print("Introduce la categoría: ");
            String categoria = scanner.nextLine();
            System.out.print("Introduce la descripción: ");
            String descripcion = scanner.nextLine();
            System.out.print("Introduce la cantidad: ");
            double cantidad = scanner.nextDouble();

            Gasto nuevoGasto = new Gasto(fecha, categoria, descripcion, cantidad);
            gastos.add(nuevoGasto);
            System.out.println("Gasto añadido correctamente.");
        } catch (DateTimeParseException e) {
            System.out.println("Formato de fecha inválido. Por favor, usa dd/MM/yyyy.");
        } catch (InputMismatchException e) {
            System.out.println("Cantidad inválida.");
            scanner.nextLine();
        }
    }

    private static void verGastos() {
        if (gastos.isEmpty()) {
            System.out.println("No hay gastos registrados.");
        } else {
            System.out.println("\n--- Todos los Gastos ---");
            gastos.forEach(System.out::println);
        }
    }

    private static void calcularTotalGastos() {
        double total = gastos.stream().mapToDouble(Gasto::getCantidad).sum();
        System.out.println("Total de gastos: €" + total);
    }

    private static void verGastosPorCategoria(Scanner scanner) {
        System.out.print("Introduce la categoría: ");
        String categoriaBuscada = scanner.nextLine().toLowerCase();
        List<Gasto> filtrados = gastos.stream()
                .filter(g -> g.getCategoria().equalsIgnoreCase(categoriaBuscada))
                .collect(Collectors.toList());

        if (filtrados.isEmpty()) {
            System.out.println("No se encontraron gastos en esta categoría.");
        } else {
            System.out.println("\n--- Gastos en la categoría '" + categoriaBuscada + "' ---");
            filtrados.forEach(System.out::println);
        }
    }

    private static void editarGasto(Scanner scanner) {
        System.out.print("Introduce el número del gasto a editar: ");
        int indice = scanner.nextInt() - 1;
        scanner.nextLine();

        if (indice >= 0 && indice < gastos.size()) {
            Gasto gasto = gastos.get(indice);
            System.out.println("Editando: " + gasto);

            System.out.print("Nueva fecha (dd/MM/yyyy, dejar en blanco para no cambiar): ");
            String nuevaFecha = scanner.nextLine();
            if (!nuevaFecha.isEmpty()) {
                gasto.setFecha(LocalDate.parse(nuevaFecha, DATE_FORMATTER));
            }

            System.out.print("Nueva categoría (dejar en blanco para no cambiar): ");
            String nuevaCategoria = scanner.nextLine();
            if (!nuevaCategoria.isEmpty()) {
                gasto.setCategoria(nuevaCategoria);
            }

            System.out.print("Nueva descripción (dejar en blanco para no cambiar): ");
            String nuevaDescripcion = scanner.nextLine();
            if (!nuevaDescripcion.isEmpty()) {
                gasto.setDescripcion(nuevaDescripcion);
            }

            System.out.print("Nueva cantidad (dejar en blanco para no cambiar): ");
            String nuevaCantidad = scanner.nextLine();
            if (!nuevaCantidad.isEmpty()) {
                gasto.setCantidad(Double.parseDouble(nuevaCantidad));
            }

            System.out.println("Gasto editado correctamente.");
        } else {
            System.out.println("Número de gasto inválido.");
        }
    }

    private static void eliminarGasto(Scanner scanner) {
        System.out.print("Introduce el número del gasto a eliminar: ");
        int indice = scanner.nextInt() - 1;
        scanner.nextLine();

        if (indice >= 0 && indice < gastos.size()) {
            Gasto eliminado = gastos.remove(indice);
            System.out.println("Gasto eliminado: " + eliminado);
        } else {
            System.out.println("Número de gasto inválido.");
        }
    }

    private static void buscarPorRangoDeFechas(Scanner scanner) {
        try {
            System.out.print("Introduce la fecha de inicio (dd/MM/yyyy): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);
            System.out.print("Introduce la fecha de fin (dd/MM/yyyy): ");
            LocalDate fin = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);

            List<Gasto> filtrados = gastos.stream()
                    .filter(g -> !g.getFecha().isBefore(inicio) && !g.getFecha().isAfter(fin))
                    .collect(Collectors.toList());

            if (filtrados.isEmpty()) {
                System.out.println("No se encontraron gastos en el rango especificado.");
            } else {
                System.out.println("\n--- Gastos del " + inicio + " al " + fin + " ---");
                filtrados.forEach(System.out::println);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Formato de fecha inválido. Por favor, usa dd/MM/yyyy.");
        }
    }

    private static void exportarACSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("export_gastos.csv"))) {
            writer.println("Fecha,Categoría,Descripción,Cantidad");
            gastos.forEach(g -> writer.println(g.toCSV()));
            System.out.println("Gastos exportados a 'export_gastos.csv'.");
        } catch (IOException e) {
            System.out.println("Error al exportar los gastos: " + e.getMessage());
        }
    }

    static class Gasto {
        private LocalDate fecha;
        private String categoria;
        private String descripcion;
        private double cantidad;

        public Gasto(LocalDate fecha, String categoria, String descripcion, double cantidad) {
            this.fecha = fecha;
            this.categoria = categoria;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public double getCantidad() {
            return cantidad;
        }

        public void setCantidad(double cantidad) {
            this.cantidad = cantidad;
        }

        public String toCSV() {
            return fecha.format(DATE_FORMATTER) + "," + categoria + "," + descripcion + "," + cantidad;
        }

        @Override
        public String toString() {
            return "Fecha: " + fecha.format(DATE_FORMATTER) + ", Categoría: " + categoria + ", Descripción: " + descripcion + ", Cantidad: €" + cantidad;
        }
    }
}
