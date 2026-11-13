package br.edu.ifpr.catalogo.tools;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.edu.ifpr.catalogo.model.Console;
import br.edu.ifpr.catalogo.service.ConsoleService;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ConsoleTools {

    private final ConsoleService service;

    public ConsoleTools(ConsoleService service) {
        this.service = service;
    }

    public SyncToolSpecification estatisticasConsolesTool() {
        Tool tool = Tool.builder()
                .name("estatisticas_consoles")
                .description("Mostra o total de consoles e a quantidade por fabricante.")
                .inputSchema(Map.of("type", "object", "properties", Map.of()))
                .build();

        return SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    String fabricantes = service.contarPorFabricante().entrySet().stream()
                            .map(entrada -> "- " + entrada.getKey() + ": " + entrada.getValue())
                            .collect(Collectors.joining("\n"));
                    String resultado = "Total de consoles: " + service.contarConsoles()
                            + "\nConsoles por fabricante:\n" + fabricantes;
                    return CallToolResult.builder()
                            .content(List.of(new TextContent(resultado)))
                            .build();
                })
                .build();
    }

    public SyncToolSpecification compararConsolesTool() {
        Map<String, Object> identificador = Map.of(
                "type", "string",
                "description", "ID ou nome completo do console.");
        Tool tool = Tool.builder()
                .name("comparar_consoles")
                .description("Compara os dados de dois consoles pelo ID ou nome completo.")
                .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of("primeiro", identificador, "segundo", identificador),
                        "required", List.of("primeiro", "segundo")))
                .build();

        return SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    var argumentos = request.arguments();
                    Object primeiro = argumentos == null ? null : argumentos.get("primeiro");
                    Object segundo = argumentos == null ? null : argumentos.get("segundo");
                    if (!(primeiro instanceof String primeiroId) || primeiroId.isBlank()
                            || !(segundo instanceof String segundoId) || segundoId.isBlank()) {
                        return CallToolResult.builder()
                                .isError(true)
                                .content(List.of(new TextContent("Informe o ID ou nome completo dos dois consoles.")))
                                .build();
                    }
                    try {
                        var consoles = service.compararConsoles(primeiroId, segundoId);
                        String comparacao = consoles.stream()
                                .map(console -> "- " + console.getNome()
                                        + " | ID: " + console.getId()
                                        + " | Fabricante: " + console.getFabricante()
                                        + " | Lançamento: " + console.getAnoLancamento()
                                        + " | Geração: " + console.getGeracao())
                                .collect(Collectors.joining("\n"));
                        return CallToolResult.builder()
                                .content(List.of(new TextContent("Comparação de consoles:\n" + comparacao)))
                                .build();
                    } catch (IllegalArgumentException e) {
                        return CallToolResult.builder()
                                .content(List.of(new TextContent(e.getMessage())))
                                .build();
                    }
                })
                .build();
    }

    public SyncToolSpecification listarConsolesTool() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "fabricante", Map.of(
                    "type", "string",
                    "description", "Digite uma fabricante para exibir os os consoles dela, fabricantes: Sony, Microsoft, Sega, Nintendo ou Atari."
                )
            )
        );

        Tool tool = Tool.builder()
                .name("listar_consoles")
                .description("Lista os consoles cadastrados, opcionalmente filtrando por fabricante.")
                .inputSchema(schema)
                .build();

        return SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    Object argumento = request.arguments() == null
                            ? null
                            : request.arguments().get("fabricante");

                    String fabricante = argumento instanceof String
                            ? (String) argumento
                            : null;

                    List<Console> consoles = service.listarConsoles(fabricante);

                    String resultado;

                    if (consoles.isEmpty()) {
                        resultado = "Nenhum console encontrado para o fabricante informado.";
                    } else {
                        String titulo = fabricante == null || fabricante.isBlank()
                                ? "Consoles cadastrados"
                                : "Consoles da fabricante " + fabricante;

                        String lista = consoles.stream()
                                .map(console -> "- " + console.getNome()
                                        + " | Fabricante: " + console.getFabricante()
                                        + " | Lançamento: " + console.getAnoLancamento())
                                .collect(Collectors.joining("\n"));

                        resultado = titulo + " (" + consoles.size() + "):\n" + lista;
                    }

                    return CallToolResult.builder()
                            .content(List.of(new TextContent(resultado)))
                            .build();
                })
                .build();
    }
    public SyncToolSpecification buscarConsolesTool() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "termo", Map.of(
                    "type", "string",
                    "description", "Termo utilizado para buscar consoles por nome, fabricante ou geração (4ª, 5ª, 6ª...)."
                )
            ),
            "required", List.of("termo")
        );

        Tool tool = Tool.builder()
                .name("buscar_consoles")
                .description("Busca consoles no catálogo utilizando um termo de pesquisa.")
                .inputSchema(schema)
                .build();

        return SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> {
                    Object argumento = request.arguments() == null
                            ? null
                            : request.arguments().get("termo");

                    String termo = argumento instanceof String
                            ? (String) argumento
                            : null;

                    String resultado;

                    if (termo == null || termo.isBlank()) {
                        return CallToolResult.builder()
                                .isError(true)
                                .content(List.of(new TextContent("Informe um termo para realizar a busca.")))
                                .build();
                    } else {
                        List<Console> consoles = service.buscarConsoles(termo);

                        if (consoles.isEmpty()) {
                            resultado = "Nenhum console encontrado para o termo informado.";
                        } else {
                            String lista = consoles.stream()
                                    .map(console -> "- " + console.getNome()
                                            + " | Fabricante: " + console.getFabricante()
                                            + " | Lançamento: " + console.getAnoLancamento()
                                            + " | Geração: " + console.getGeracao())
                                    .collect(Collectors.joining("\n"));

                            resultado = "Resultados da busca por \"" + termo
                                    + "\" (" + consoles.size() + "):\n" + lista;
                        }
                    }

                    return CallToolResult.builder()
                            .content(List.of(new TextContent(resultado)))
                            .build();
                })
                .build();
    }
}
