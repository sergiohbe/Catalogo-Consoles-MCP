package br.edu.ifpr.catalogo;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

@FunctionalInterface
public interface ConsultaCatalogo {

    CallToolResult consultar(String ferramenta, Map<String, Object> argumentos);
}
