package br.edu.ifpr.catalogo.resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.catalogo.service.ConsoleService;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

public class CatalogoResource {

    public static final String URI = "catalogo://consoles";
    private final ConsoleService service;

    public CatalogoResource(ConsoleService service) {
        this.service = service;
    }

    public SyncResourceSpecification especificacao() {
        var resource = Resource.builder(URI, "catalogo-consoles")
                .description("Catálogo completo de consoles, somente para leitura.")
                .mimeType("application/json").build();
        return new SyncResourceSpecification(resource, (exchange, request) -> {
            var dados = service.listarConsoles(null).stream().map(console -> Map.of(
                    "id", console.getId(), "nome", console.getNome(),
                    "fabricante", console.getFabricante(), "anoLancamento", console.getAnoLancamento(),
                    "geracao", console.getGeracao())).toList();
            try {
                String json = McpJsonDefaults.getMapper().writeValueAsString(dados);
                return ReadResourceResult.builder(List.of(TextResourceContents.builder(URI, json).mimeType("application/json").build())).build();
            } catch (IOException e) {
                throw new IllegalStateException("Não foi possível gerar o catálogo JSON.", e);
            }
        });
    }
}
