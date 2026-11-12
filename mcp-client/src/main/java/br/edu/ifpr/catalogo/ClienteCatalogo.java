package br.edu.ifpr.catalogo;

import java.util.Map;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

public class ClienteCatalogo implements ConsultaCatalogo {

    private final McpSyncClient cliente;

    public ClienteCatalogo(McpSyncClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public CallToolResult consultar(String ferramenta, Map<String, Object> argumentos) {
        try {
            return cliente.callTool(CallToolRequest.builder()
                    .name(ferramenta)
                    .arguments(argumentos)
                    .build());
        } catch (RuntimeException e) {
            throw new FalhaComunicacao("consultar a ferramenta " + ferramenta, e);
        }
    }
}
