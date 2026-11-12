package br.edu.ifpr.catalogo;

import java.util.Map;
import java.util.Scanner;
import java.io.PrintStream;

import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class MenuCliente {

    private final ConsultaCatalogo catalogo;
    private final Scanner entrada;
    private final PrintStream saida;

    public MenuCliente(ConsultaCatalogo catalogo) {
        this(catalogo, new Scanner(System.in), System.out);
    }

    MenuCliente(ConsultaCatalogo catalogo, Scanner entrada, PrintStream saida) {
        this.catalogo = catalogo;
        this.entrada = entrada;
        this.saida = saida;
    }

    public void executar() {
        while (true) {
            saida.println("\nCatálogo de consoles");
            saida.println("1 - Listar todos os consoles");
            saida.println("2 - Filtrar por fabricante");
            saida.println("3 - Buscar consoles");
            saida.println("4 - Comparar dois consoles");
            saida.println("5 - Estatísticas do catálogo");
            saida.println("0 - Sair");
            saida.print("Escolha uma opção: ");
            saida.flush();

            if (!entrada.hasNextLine()) {
                return;
            }
            String opcao = entrada.nextLine().trim();
            switch (opcao) {
                case "0" -> { return; }
                case "1" -> consultar("listar_consoles", Map.of());
                case "5" -> consultar("estatisticas_consoles", Map.of());
                case "2", "3" -> {
                    boolean filtrar = opcao.equals("2");
                    saida.print(filtrar
                            ? "Fabricante (Sony, Microsoft, Sega, Nintendo ou Atari): "
                            : "Termo de busca: ");
                    saida.flush();
                    if (!entrada.hasNextLine()) {
                        return;
                    }
                    String valor = entrada.nextLine().trim();
                    if (valor.isBlank()) {
                        saida.println("Informe um valor para realizar a consulta.");
                        continue;
                    }
                    consultar(filtrar ? "listar_consoles" : "buscar_consoles",
                            Map.of(filtrar ? "fabricante" : "termo", valor));
                }
                case "4" -> {
                    saida.print("Primeiro console (ID ou nome completo): ");
                    saida.flush();
                    if (!entrada.hasNextLine()) {
                        return;
                    }
                    String primeiro = entrada.nextLine().trim();
                    saida.print("Segundo console (ID ou nome completo): ");
                    saida.flush();
                    if (!entrada.hasNextLine()) {
                        return;
                    }
                    String segundo = entrada.nextLine().trim();
                    if (primeiro.isBlank() || segundo.isBlank()) {
                        saida.println("Informe o ID ou nome completo dos dois consoles.");
                        continue;
                    }
                    consultar("comparar_consoles", Map.of("primeiro", primeiro, "segundo", segundo));
                }
                default -> saida.println("Opção inválida. Escolha 1, 2, 3, 4, 5 ou 0.");
            }
        }
    }

    private void consultar(String ferramenta, Map<String, Object> argumentos) {
        var resultado = catalogo.consultar(ferramenta, argumentos);
        for (var conteudo : resultado.content()) {
            if (conteudo instanceof TextContent texto) {
                saida.println(texto.text());
            }
        }
    }
}
