package br.edu.ifpr.catalogo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Arrays;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class MainCliente {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java -jar mcp-client.jar <caminho-do-servidor.jar> [termo | comando]");
            System.exit(1);
        }

        try {
            var opcoes = Arrays.copyOfRange(args, 1, args.length);
            ComandosContexto.validar(opcoes);
            executar(Path.of(args[0]), opcoes);
        } catch (IllegalArgumentException e) {
            System.err.println("Não foi possível iniciar o cliente: " + e.getMessage());
            System.exit(1);
        } catch (FalhaComunicacao e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Falha na comunicação MCP: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void executar(Path servidorJar, String[] opcoes) {
        String termo = opcoes.length == 1 && !opcoes[0].startsWith("--") ? opcoes[0] : null;
        if (!Files.isRegularFile(servidorJar)) {
            throw new IllegalArgumentException("JAR do servidor não encontrado: " + servidorJar);
        }
        if (!Files.isReadable(servidorJar)) {
            throw new IllegalArgumentException("Sem permissão de leitura para o JAR: " + servidorJar);
        }
        if (termo != null && termo.isBlank()) {
            throw new IllegalArgumentException("Informe um termo para a busca.");
        }

        String executavel = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executavel);
        var parametros = ServerParameters.builder(java.toString())
                .args("-jar", servidorJar.toAbsolutePath().toString())
                .build();
        var transporte = new StdioClientTransport(parametros, McpJsonDefaults.getMapper());
        transporte.setStdErrorHandler(System.err::println);

        try (var cliente = McpClient.sync(transporte)
                .requestTimeout(Duration.ofSeconds(15))
                .initializationTimeout(Duration.ofSeconds(15))
                .build()) {
            try {
                var inicializacao = cliente.initialize();
                System.out.println("Conectado ao servidor: " + inicializacao.serverInfo().name());
            } catch (RuntimeException e) {
                throw new FalhaComunicacao("conectar ao servidor MCP e descobrir suas ferramentas", e);
            }

            if (ComandosContexto.executar(cliente, opcoes, System.out)) {
                System.out.println("Consulta MCP concluída.");
            } else {
                var catalogo = new ClienteCatalogo(cliente);
                var ferramentas = catalogo.listarFerramentas();
                if (termo == null) {
                    new MenuCliente(ferramentas, catalogo).executar();
                } else {
                    System.out.println("Ferramentas disponíveis:");
                    ferramentas.forEach(tool -> System.out.println("- " + tool.name()));
                    exibirResultado(catalogo.consultar("listar_consoles", Map.of()));
                    exibirResultado(catalogo.consultar("buscar_consoles", Map.of("termo", termo)));
                }
            }
        }
        System.out.println("Conexão MCP encerrada.");
    }

    static void exibirResultado(CallToolResult resultado) {
        if (Boolean.TRUE.equals(resultado.isError())) {
            throw new IllegalStateException("A ferramenta MCP retornou um erro: " + resultado.content());
        }
        for (var conteudo : resultado.content()) {
            if (conteudo instanceof TextContent texto) {
                System.out.println(texto.text());
            }
        }
    }
}
