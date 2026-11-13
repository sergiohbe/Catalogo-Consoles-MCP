package br.edu.ifpr.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

class MenuClienteTest {

    private record Chamada(String ferramenta, Map<String, Object> argumentos) { }

    private final List<Chamada> chamadas = new ArrayList<>();

    private CallToolResult responder(String ferramenta, Map<String, Object> argumentos) {
        chamadas.add(new Chamada(ferramenta, argumentos));
        return CallToolResult.builder().content(List.of(TextContent.builder("Resposta recebida").build())).build();
    }

    private String executar(String entrada, ConsultaCatalogo catalogo) {
        return executar(entrada, catalogo, List.of(
                ferramenta("buscar_consoles", Map.of("termo", Map.of("type", "string")), List.of("termo")),
                ferramenta("comparar_consoles", Map.of("primeiro", Map.of("type", "string"),
                        "segundo", Map.of("type", "string")), List.of("primeiro", "segundo")),
                ferramenta("estatisticas_consoles", Map.of(), List.of()),
                ferramenta("listar_consoles", Map.of("fabricante", Map.of("type", "string")), List.of())));
    }

    private Tool ferramenta(String nome, Map<String, Object> propriedades, List<String> obrigatorios) {
        return Tool.builder(nome, Map.of("type", "object", "properties", propriedades, "required", obrigatorios)).description("Descrição de " + nome).build();
    }

    private String executar(String entrada, ConsultaCatalogo catalogo, List<Tool> ferramentas) {
        var buffer = new ByteArrayOutputStream();
        try (var scanner = new Scanner(entrada);
                var saida = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            new MenuCliente(ferramentas, catalogo, scanner, saida).executar();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void deveEnviarFerramentasEArgumentosDasCincoOpcoes() {
        String saida = executar("4\n\n4\n Sega \n1\n Dreamcast \n2\n ps5-pro \nxbox-series-x\n3\n0\n", this::responder);
        assertEquals(List.of(
                new Chamada("listar_consoles", Map.of()),
                new Chamada("listar_consoles", Map.of("fabricante", "Sega")),
                new Chamada("buscar_consoles", Map.of("termo", "Dreamcast")),
                new Chamada("comparar_consoles", Map.of("primeiro", "ps5-pro", "segundo", "xbox-series-x")),
                new Chamada("estatisticas_consoles", Map.of())), chamadas);
        assertEquals(5, saida.lines().filter(linha -> linha.contains("Resposta recebida")).count());
    }

    @Test
    void deveRejeitarOpcoesInvalidasEConsultasVaziasSemEnviarRequisicoes() {
        String saida = executar("abc\n9\n-1\n2147483648\n1\n \n2\n\n2\nps2\n \n0\n", this::responder);
        assertTrue(chamadas.isEmpty());
        assertTrue(saida.contains("Opção inválida"));
        assertTrue(saida.contains("O parâmetro termo é obrigatório"));
        assertTrue(saida.contains("O parâmetro primeiro é obrigatório"));
        assertTrue(saida.contains("O parâmetro segundo é obrigatório"));
    }

    @Test
    void deveEncerrarSemConsultarAoSairOuAoAcabarAEntrada() {
        for (String entrada : List.of("0\n1\n", "", "1\n", "2\n", "4\n", "2\nps2\n")) {
            executar(entrada, this::responder);
        }
        assertTrue(chamadas.isEmpty());
    }

    @Test
    void deveManterMenuDisponivelAposErroDaFerramenta() {
        String saida = executar("2\nps2\ninexistente\n3\n0\n", (ferramenta, argumentos) -> {
            if (ferramenta.equals("comparar_consoles")) {
                return CallToolResult.builder().isError(true)
                        .content(List.of(TextContent.builder("Console não encontrado: inexistente").build())).build();
            }
            return responder(ferramenta, argumentos);
        });
        assertTrue(saida.contains("Console não encontrado: inexistente"));
        assertTrue(saida.contains("Resposta recebida"));
        assertEquals(List.of(new Chamada("estatisticas_consoles", Map.of())), chamadas);
    }

    @Test
    void devePropagarFalhaDeComunicacaoParaEncerramentoDaSessao() {
        var falha = new FalhaComunicacao("consultar", new IllegalStateException("Connection closed"));
        var resultado = assertThrows(FalhaComunicacao.class, () -> executar("3\n0\n",
                (ferramenta, argumentos) -> { throw falha; }));
        assertSame(falha, resultado);
    }

    @Test
    void deveExecutarFerramentaDesconhecidaUsandoSeuSchema() {
        Tool nova = ferramenta("consulta_nova", Map.of("codigo", Map.of(
                "type", "string", "description", "Código solicitado pelo servidor")), List.of("codigo"));
        String saida = executar("1\nabc-123\n0\n", this::responder, List.of(nova));
        assertEquals(List.of(new Chamada("consulta_nova", Map.of("codigo", "abc-123"))), chamadas);
        assertTrue(saida.contains("Descrição de consulta_nova"));
        assertTrue(saida.contains("Código solicitado pelo servidor"));
    }

    @Test
    void deveEncerrarQuandoServidorNaoOfereceFerramentas() {
        String saida = executar("", this::responder, List.of());
        assertTrue(saida.contains("não disponibilizou ferramentas"));
        assertTrue(chamadas.isEmpty());
    }

    @Test
    void devePreservarTiposNaEntradaJsonParaSchemasComplexos() {
        Tool complexa = ferramenta("consulta_complexa", Map.of("quantidade", Map.of("type", "integer")), List.of("quantidade"));
        executar("1\n{\"quantidade\":2,\"ativo\":true,\"nomes\":[\"ps2\"],\"filtro\":{\"geracao\":6}}\n0\n",
                this::responder, List.of(complexa));
        assertEquals(List.of(new Chamada("consulta_complexa", Map.of("quantidade", 2,
                "ativo", true, "nomes", List.of("ps2"), "filtro", Map.of("geracao", 6)))), chamadas);
    }

    @Test
    void deveRejeitarJsonInvalidoOuQueNaoSejaObjeto() {
        Tool complexa = ferramenta("complexa", Map.of("ano", Map.of("type", "integer")), List.of());
        for (String json : List.of("abc", "[]", "null", "123")) {
            String saida = executar("1\n" + json + "\n0\n", this::responder, List.of(complexa));
            assertTrue(saida.contains("JSON inválido"));
        }
        assertTrue(chamadas.isEmpty());
    }

    @Test
    void deveEncerrarAoAcabarEntradaDuranteColetaJson() {
        Tool complexa = ferramenta("complexa", Map.of("ano", Map.of("type", "integer")), List.of());
        executar("1\n", this::responder, List.of(complexa));
        assertTrue(chamadas.isEmpty());
    }
}
