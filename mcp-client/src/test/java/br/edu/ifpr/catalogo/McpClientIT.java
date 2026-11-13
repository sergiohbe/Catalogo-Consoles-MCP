package br.edu.ifpr.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class McpClientIT {

    private McpSyncClient cliente;
    private ClienteCatalogo catalogo;
    private Path servidorJar;
    private ProcessHandle servidor;
    private Set<Long> processosAnteriores;

    @BeforeAll
    void conectar() {
        servidorJar = Path.of(System.getProperty("catalogo.servidor.jar")).toAbsolutePath().normalize();
        assertTrue(Files.isRegularFile(servidorJar), "Gere o JAR do servidor usando mvn verify na raiz.");
        String executavel = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executavel);
        var parametros = ServerParameters.builder(java.toString())
                .args("-jar", servidorJar.toString()).build();
        var transporte = new StdioClientTransport(parametros, McpJsonDefaults.getMapper());
        transporte.setStdErrorHandler(System.err::println);
        cliente = McpClient.sync(transporte)
                .initializationTimeout(Duration.ofSeconds(15))
                .requestTimeout(Duration.ofSeconds(15)).build();
        try (var processos = ProcessHandle.current().children()) {
            processosAnteriores = processos.map(ProcessHandle::pid).collect(Collectors.toSet());
        }
        try {
            cliente.initialize();
        } finally {
            servidor = localizarServidor();
        }
        assertNotNull(servidor, "O teste deve identificar o processo filho do servidor.");
        catalogo = new ClienteCatalogo(cliente);
    }

    private ProcessHandle localizarServidor() {
        try (var processos = ProcessHandle.current().children()) {
            return processos.filter(processo -> !processosAnteriores.contains(processo.pid()))
                    .findFirst().orElse(null);
        }
    }

    @AfterAll
    void encerrar() throws Exception {
        try {
            if (cliente != null) {
                assertTrue(cliente.closeGracefully(), "A sessão MCP deve fechar corretamente.");
            }
            if (servidor != null) {
                servidor.onExit().get(5, TimeUnit.SECONDS);
                assertFalse(servidor.isAlive(), "O servidor deve encerrar junto com a conexão.");
            }
        } finally {
            if (servidor != null && servidor.isAlive()) {
                servidor.destroyForcibly();
                servidor.onExit().get(5, TimeUnit.SECONDS);
            }
        }
    }

    private String texto(CallToolResult resultado) {
        return resultado.content().stream().filter(TextContent.class::isInstance)
                .map(TextContent.class::cast).map(TextContent::text).collect(Collectors.joining("\n"));
    }

    @Test
    void deveDescobrirELerResourceDoCatalogo() throws Exception {
        assertNotNull(cliente.getServerCapabilities().resources());
        var recursos = cliente.listResources().resources();
        assertEquals(1, recursos.size());
        assertEquals("catalogo://consoles", recursos.getFirst().uri());
        var resultado = cliente.readResource(new io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest("catalogo://consoles"));
        var conteudo = (io.modelcontextprotocol.spec.McpSchema.TextResourceContents) resultado.contents().getFirst();
        var dados = McpJsonDefaults.getMapper().readValue(conteudo.text(),
                new io.modelcontextprotocol.json.TypeRef<java.util.List<Map<String, Object>>>() { });
        assertEquals(40, dados.size());
        assertEquals("application/json", conteudo.mimeType());
        assertTrue(dados.stream().anyMatch(console -> "dreamcast".equals(console.get("id"))));
    }

    @Test
    void deveDescobrirEObterPromptDeComparacao() {
        assertNotNull(cliente.getServerCapabilities().prompts());
        var prompts = cliente.listPrompts().prompts();
        assertEquals(1, prompts.size());
        assertEquals("orientar_comparacao", prompts.getFirst().name());
        var resultado = cliente.getPrompt(new io.modelcontextprotocol.spec.McpSchema.GetPromptRequest("orientar_comparacao",
                Map.of("primeiro", "ps5-pro", "segundo", "xbox-series-x")));
        String texto = ((TextContent) resultado.messages().getFirst().content()).text();
        assertTrue(texto.contains("PlayStation 5 Pro | ID: ps5-pro"));
        assertTrue(texto.contains("Xbox Series X | ID: xbox-series-x"));
        assertTrue(texto.contains("Não invente preço"));
    }

    @Test
    void deveRejeitarContextoInvalidoESeguirAtendendo() {
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> cliente.readResource(
                new io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest("catalogo://inexistente")));
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> cliente.getPrompt(
                new io.modelcontextprotocol.spec.McpSchema.GetPromptRequest("orientar_comparacao", Map.of("primeiro", "ps2"))));
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> cliente.getPrompt(
                new io.modelcontextprotocol.spec.McpSchema.GetPromptRequest("orientar_comparacao",
                        Map.of("primeiro", "ps2", "segundo", "inexistente"))));
        assertTrue(consultar("estatisticas_consoles", Map.of()).contains("Total de consoles: 40"));
    }

    @Test
    void deveExecutarComandosDeContextoPeloCliente() {
        var buffer = new ByteArrayOutputStream();
        try (var saida = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            for (String[] args : java.util.List.of(new String[] { "--recursos" },
                    new String[] { "--recurso", "catalogo://consoles" }, new String[] { "--prompts" },
                    new String[] { "--prompt", "orientar_comparacao", "primeiro=ps2", "segundo=Dreamcast" })) {
                assertTrue(ComandosContexto.executar(cliente, args, saida));
            }
        }
        String texto = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("catalogo://consoles"));
        assertTrue(texto.contains("orientar_comparacao"));
        assertTrue(texto.contains("PlayStation 2 | ID: ps2"));
        assertTrue(texto.contains("Dreamcast | ID: dreamcast"));
    }

    private String consultar(String ferramenta, Map<String, Object> argumentos) {
        var resultado = catalogo.consultar(ferramenta, argumentos);
        assertFalse(Boolean.TRUE.equals(resultado.isError()), texto(resultado));
        return texto(resultado);
    }

    @Test
    void deveNegociarConexaoEDescobrirTodasAsFerramentas() {
        assertTrue(cliente.isInitialized());
        assertEquals("catalogo-consoles-mcp", cliente.getServerInfo().name());
        var nomes = catalogo.listarFerramentas().stream().map(tool -> tool.name()).collect(Collectors.toSet());
        assertEquals(Set.of("listar_consoles", "buscar_consoles", "comparar_consoles", "estatisticas_consoles"), nomes);
    }

    @Test
    void deveExecutarMenuComFerramentasDescobertasNoServidorReal() {
        var ferramentas = catalogo.listarFerramentas();
        var buffer = new ByteArrayOutputStream();
        StringBuilder entrada = new StringBuilder();
        for (int i = 0; i < ferramentas.size(); i++) {
            entrada.append(i + 1).append('\n');
            switch (ferramentas.get(i).name()) {
                case "buscar_consoles" -> entrada.append("Dreamcast\n");
                case "comparar_consoles" -> entrada.append("ps5-pro\nxbox-series-x\n");
                case "listar_consoles" -> entrada.append("Sega\n");
                default -> { }
            }
        }
        entrada.append("0\n");
        try (var scanner = new Scanner(entrada.toString());
                var saida = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            new MenuCliente(ferramentas, catalogo, scanner, saida).executar();
        }
        String resultado = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(resultado.contains("Resultados da busca por \"Dreamcast\" (1)"));
        assertTrue(resultado.contains("PlayStation 5 Pro | ID: ps5-pro"));
        assertTrue(resultado.contains("Total de consoles: 40"));
        assertTrue(resultado.contains("Consoles do fabricante Sega (4)"));
    }

    @Test
    void deveListarCatalogoEFiltrarPorFabricante() {
        String todos = consultar("listar_consoles", Map.of());
        assertTrue(todos.contains("Consoles cadastrados (40)"));
        assertEquals(40, todos.lines().filter(linha -> linha.startsWith("- ")).count());
        String sega = consultar("listar_consoles", Map.of("fabricante", "Sega"));
        assertTrue(sega.contains("Consoles do fabricante Sega (4)"));
        assertEquals(4, sega.lines().filter(linha -> linha.startsWith("- ")).count());
        assertFalse(sega.contains("Fabricante: Sony"));
        assertTrue(consultar("listar_consoles", Map.of("fabricante", "inexistente"))
                .contains("Nenhum console encontrado"));
    }

    @Test
    void deveBuscarConsoleERetornarBuscaSemResultados() {
        String resultado = consultar("buscar_consoles", Map.of("termo", "Dreamcast"));
        assertTrue(resultado.contains("Resultados da busca por \"Dreamcast\" (1)"));
        assertTrue(resultado.contains("Dreamcast | Fabricante: Sega | Lançamento: 1998"));
        assertTrue(consultar("buscar_consoles", Map.of("termo", "inexistente"))
                .contains("Nenhum console encontrado"));
    }

    @Test
    void deveCompararConsolesPorIdENome() {
        String resultado = consultar("comparar_consoles", Map.of("primeiro", "ps5-pro", "segundo", "XBOX SERIES X"));
        assertTrue(resultado.contains("PlayStation 5 Pro | ID: ps5-pro | Fabricante: Sony | Lançamento: 2024 | Geração: 9ª geração"));
        assertTrue(resultado.contains("Xbox Series X | ID: xbox-series-x | Fabricante: Microsoft | Lançamento: 2020 | Geração: 9ª geração"));
    }

    @Test
    void deveRetornarEstatisticasDoCatalogo() {
        assertEquals("Total de consoles: 40\nConsoles por fabricante:\n"
                + "- Atari: 4\n- Microsoft: 11\n- Nintendo: 9\n- Sega: 4\n- Sony: 12",
                consultar("estatisticas_consoles", Map.of()));
    }

    @Test
    void deveOrientarQuandoTermoDaBuscaEstaVazio() {
        assertEquals("Informe um termo para realizar a busca.",
                consultar("buscar_consoles", Map.of("termo", " ")));
    }

    @Test
    void deveRetornarErrosDeComparacaoEContinuarAtendendo() {
        var inexistente = catalogo.consultar("comparar_consoles", Map.of("primeiro", "ps2", "segundo", "inexistente"));
        assertTrue(Boolean.TRUE.equals(inexistente.isError()));
        assertEquals("Console não encontrado: inexistente", texto(inexistente));
        var vazio = catalogo.consultar("comparar_consoles", Map.of("primeiro", " ", "segundo", "ps2"));
        assertTrue(Boolean.TRUE.equals(vazio.isError()));
        assertTrue(texto(vazio).contains("Informe o ID ou nome completo dos dois consoles"));
        var ausente = catalogo.consultar("comparar_consoles", Map.of("primeiro", "ps2"));
        assertTrue(Boolean.TRUE.equals(ausente.isError()));
        var tipoInvalido = catalogo.consultar("comparar_consoles", Map.of("primeiro", 123, "segundo", "ps2"));
        assertTrue(Boolean.TRUE.equals(tipoInvalido.isError()));
        assertTrue(consultar("estatisticas_consoles", Map.of()).contains("Total de consoles: 40"));
    }
}
