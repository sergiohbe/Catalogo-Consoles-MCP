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

    public SyncToolSpecification listarConsolesTool() {
        Map<String, Object> schema = Map.of(
            "type", "object",
            "properties", Map.of(
                "fabricante", Map.of(
                    "type", "string",
                    "description", "Fabricante para filtrar os consoles, como Sony, Microsoft ou Nintendo."
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
                                : "Consoles do fabricante " + fabricante;

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
                    "description", "Termo utilizado para buscar consoles por nome, fabricante ou características."
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
                        resultado = "Informe um termo para realizar a busca.";
                    } else {
                        List<Console> consoles = service.buscarConsoles(termo);

                        if (consoles.isEmpty()) {
                            resultado = "Nenhum console encontrado para o termo informado.";
                        } else {
                            String lista = consoles.stream()
                                    .map(console -> "- " + console.getNome()
                                            + " | Fabricante: " + console.getFabricante()
                                            + " | Lançamento: " + console.getAnoLancamento())
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