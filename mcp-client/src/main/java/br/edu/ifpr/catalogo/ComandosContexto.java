package br.edu.ifpr.catalogo;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

class ComandosContexto {

    static void validar(String[] args) {
        if (args.length == 0 || (args.length == 1 && !args[0].startsWith("--"))) {
            return;
        }
        boolean valido = switch (args[0]) {
            case "--recursos", "--prompts" -> args.length == 1;
            case "--recurso" -> args.length == 2 && !args[1].isBlank();
            case "--prompt" -> args.length >= 2 && !args[1].isBlank();
            default -> false;
        };
        if (!valido) {
            throw new IllegalArgumentException("Use --recursos, --recurso URI, --prompts ou --prompt NOME [parametro=valor...].");
        }
        if (args[0].equals("--prompt")) {
            argumentos(args);
        }
    }

    static Map<String, Object> argumentos(String[] args) {
        var argumentos = new LinkedHashMap<String, Object>();
        for (int i = 2; i < args.length; i++) {
            int separador = args[i].indexOf('=');
            if (separador <= 0 || args[i].substring(separador + 1).isBlank()) {
                throw new IllegalArgumentException("Use parametro=valor para os argumentos do prompt.");
            }
            String nome = args[i].substring(0, separador);
            if (argumentos.putIfAbsent(nome, args[i].substring(separador + 1)) != null) {
                throw new IllegalArgumentException("Parâmetro repetido: " + nome);
            }
        }
        return argumentos;
    }

    static boolean executar(McpSyncClient cliente, String[] args, PrintStream saida) {
        if (args.length == 0 || !args[0].startsWith("--")) {
            return false;
        }
        switch (args[0]) {
            case "--recursos" -> {
                String cursor = null;
                do {
                    var pagina = cliente.listResources(cursor);
                    pagina.resources().forEach(resource -> saida.println(resource.uri() + " | " + resource.description()));
                    cursor = pagina.nextCursor();
                } while (cursor != null);
            }
            case "--recurso" -> cliente.readResource(new ReadResourceRequest(args[1])).contents().forEach(conteudo -> {
                if (conteudo instanceof TextResourceContents texto) {
                    saida.println(texto.text());
                }
            });
            case "--prompts" -> {
                String cursor = null;
                do {
                    var pagina = cliente.listPrompts(cursor);
                    pagina.prompts().forEach(prompt -> {
                        saida.println(prompt.name() + " | " + prompt.description());
                        if (prompt.arguments() != null) {
                            prompt.arguments().forEach(arg -> saida.println("- " + arg.name()
                                    + (Boolean.TRUE.equals(arg.required()) ? " (obrigatório)" : " (opcional)")
                                    + ": " + arg.description()));
                        }
                    });
                    cursor = pagina.nextCursor();
                } while (cursor != null);
            }
            case "--prompt" -> cliente.getPrompt(new GetPromptRequest(args[1], argumentos(args))).messages().forEach(mensagem -> {
                if (mensagem.content() instanceof TextContent texto) {
                    saida.println(mensagem.role() + ": " + texto.text());
                }
            });
            default -> throw new IllegalArgumentException("Comando desconhecido.");
        }
        return true;
    }
}
