package br.edu.ifpr.catalogo;

import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ClienteCatalogo implements ConsultaCatalogo {

    private final McpSyncClient cliente;

    public ClienteCatalogo(McpSyncClient cliente) {
        this.cliente = cliente;
    }

    public List<Tool> listarFerramentas() {
        try {
            var ferramentas = new ArrayList<Tool>();
            String cursor = null;
            do {
                var pagina = cliente.listTools(cursor);
                ferramentas.addAll(pagina.tools());
                cursor = pagina.nextCursor();
            } while (cursor != null);
            return ferramentas.stream().sorted(Comparator.comparing(Tool::name)).toList();
        } catch (RuntimeException e) {
            throw new FalhaComunicacao("descobrir as ferramentas do servidor", e);
        }
    }

    @Override
    public CallToolResult consultar(String ferramenta, Map<String, Object> argumentos) {
        try {
            return cliente.callTool(CallToolRequest.builder(ferramenta)
                    .arguments(argumentos)
                    .build());
        } catch (RuntimeException e) {
            throw new FalhaComunicacao("consultar a ferramenta " + ferramenta, e);
        }
    }
}
