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

class MenuClienteTest {

    private record Chamada(String ferramenta, Map<String, Object> argumentos) { }

    private final List<Chamada> chamadas = new ArrayList<>();

    private CallToolResult responder(String ferramenta, Map<String, Object> argumentos) {
        chamadas.add(new Chamada(ferramenta, argumentos));
        return CallToolResult.builder().content(List.of(new TextContent("Resposta recebida"))).build();
    }

    private String executar(String entrada, ConsultaCatalogo catalogo) {
        var buffer = new ByteArrayOutputStream();
        try (var scanner = new Scanner(entrada);
                var saida = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            new MenuCliente(catalogo, scanner, saida).executar();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void deveEnviarFerramentasEArgumentosDasCincoOpcoes() {
        String saida = executar("1\n2\n Sega \n3\n Dreamcast \n4\n ps5-pro \nxbox-series-x\n5\n0\n", this::responder);
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
        String saida = executar("abc\n9\n2\n \n3\n\n4\n\nps2\n4\nps2\n \n0\n", this::responder);
        assertTrue(chamadas.isEmpty());
        assertTrue(saida.contains("Opção inválida"));
        assertTrue(saida.contains("Informe um valor"));
        assertTrue(saida.contains("Informe o ID ou nome completo dos dois consoles"));
    }

    @Test
    void deveEncerrarSemConsultarAoSairOuAoAcabarAEntrada() {
        for (String entrada : List.of("0\n1\n", "", "2\n", "3\n", "4\n", "4\nps2\n")) {
            executar(entrada, this::responder);
        }
        assertTrue(chamadas.isEmpty());
    }

    @Test
    void deveManterMenuDisponivelAposErroDaFerramenta() {
        String saida = executar("4\nps2\ninexistente\n5\n0\n", (ferramenta, argumentos) -> {
            if (ferramenta.equals("comparar_consoles")) {
                return CallToolResult.builder().isError(true)
                        .content(List.of(new TextContent("Console não encontrado: inexistente"))).build();
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
        var resultado = assertThrows(FalhaComunicacao.class, () -> executar("1\n5\n0\n",
                (ferramenta, argumentos) -> { throw falha; }));
        assertSame(falha, resultado);
    }
}
