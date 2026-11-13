package br.edu.ifpr.catalogo.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.catalogo.repository.ConsoleRepository;
import br.edu.ifpr.catalogo.service.ConsoleService;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;

class ConsoleToolsTest {

    private CallToolResult chamar(SyncToolSpecification ferramenta, Map<String, Object> argumentos) {
        return ferramenta.callHandler().apply(null, CallToolRequest.builder()
                .name(ferramenta.tool().name()).arguments(argumentos).build());
    }

    private String texto(CallToolResult resultado) {
        assertFalse(Boolean.TRUE.equals(resultado.isError()));
        return ((TextContent) resultado.content().getFirst()).text();
    }

    @Test
    void deveListarSemFiltroOuComFiltroVazio() {
        var ferramenta = new ConsoleTools(new ConsoleService(new ConsoleRepository())).listarConsolesTool();
        assertTrue(texto(chamar(ferramenta, null)).contains("Consoles cadastrados (40)"));
        assertTrue(texto(chamar(ferramenta, Map.of())).contains("Consoles cadastrados (40)"));
        assertTrue(texto(chamar(ferramenta, Map.of("fabricante", " "))).contains("Consoles cadastrados (40)"));
    }

    @Test
    void deveExibirListaFiltradaOuMensagemSemFabricanteEncontrado() {
        var ferramenta = new ConsoleTools(new ConsoleService(new ConsoleRepository())).listarConsolesTool();
        String lista = texto(chamar(ferramenta, Map.of("fabricante", "Sega")));
        assertTrue(lista.contains("Consoles da fabricante Sega (4)"));
        assertTrue(lista.contains("Dreamcast | Fabricante: Sega | Lançamento: 1998"));
        assertFalse(lista.contains("Fabricante: Sony"));
        assertEquals("Nenhum console encontrado para o fabricante informado.",
                texto(chamar(ferramenta, Map.of("fabricante", "inexistente"))));
    }

    @Test
    void deveExibirResultadoDaBuscaOuMensagemSemCorrespondencia() {
        var ferramenta = new ConsoleTools(new ConsoleService(new ConsoleRepository())).buscarConsolesTool();
        String busca = texto(chamar(ferramenta, Map.of("termo", "Dreamcast")));
        assertTrue(busca.contains("Resultados da busca por \"Dreamcast\" (1)"));
        assertTrue(busca.contains("Dreamcast | Fabricante: Sega | Lançamento: 1998 | Geração: 6ª geração"));
        assertEquals("Nenhum console encontrado para o termo informado.",
                texto(chamar(ferramenta, Map.of("termo", "inexistente"))));
    }

    @Test
    void deveExibirGeracaoQueJustificaResultadoDaBuscaNumerica() {
        var ferramenta = new ConsoleTools(new ConsoleService(new ConsoleRepository())).buscarConsolesTool();
        String busca = texto(chamar(ferramenta, Map.of("termo", "6")));
        assertTrue(busca.contains("PlayStation 2 | Fabricante: Sony | Lançamento: 2000 | Geração: 6ª geração"));
        assertTrue(busca.lines().filter(linha -> linha.startsWith("- ")).allMatch(linha -> linha.contains(" | Geração: ")));
    }

    @Test
    void deveOrientarBuscaSemTermoValidoNoHandler() {
        var ferramenta = new ConsoleTools(new ConsoleService(new ConsoleRepository())).buscarConsolesTool();
        var semArgumentos = chamar(ferramenta, null);
        assertTrue(Boolean.TRUE.equals(semArgumentos.isError()));
        assertEquals("Informe um termo para realizar a busca.", ((TextContent) semArgumentos.content().getFirst()).text());
        for (Map<String, Object> argumentos : java.util.List.<Map<String, Object>>of(
                Map.of(), Map.of("termo", ""), Map.of("termo", " \t"), Map.of("termo", 123))) {
            var resultado = chamar(ferramenta, argumentos);
            assertTrue(Boolean.TRUE.equals(resultado.isError()));
            assertEquals("Informe um termo para realizar a busca.", ((TextContent) resultado.content().getFirst()).text());
        }
    }

    @Test
    void deveExibirTotalEContagensPorFabricante() {
        var ferramenta = new ConsoleTools(new ConsoleService(new ConsoleRepository()))
                .estatisticasConsolesTool();
        var resultado = ferramenta.callHandler().apply(null, CallToolRequest.builder()
                .name("estatisticas_consoles").arguments(Map.of()).build());

        assertFalse(Boolean.TRUE.equals(resultado.isError()));
        assertEquals("Total de consoles: 40\nConsoles por fabricante:\n"
                + "- Atari: 4\n- Microsoft: 11\n- Nintendo: 9\n- Sega: 4\n- Sony: 12",
                ((TextContent) resultado.content().getFirst()).text());
    }

    private CallToolResult comparar(Map<String, Object> argumentos) {
        var ferramenta = new ConsoleTools(new ConsoleService(new ConsoleRepository()))
                .compararConsolesTool();
        return ferramenta.callHandler().apply(null, CallToolRequest.builder()
                .name("comparar_consoles").arguments(argumentos).build());
    }

    @Test
    void deveCompararPorIdENomeIgnorandoMaiusculas() {
        var resultado = comparar(Map.of("primeiro", "ps5-pro", "segundo", "XBOX SERIES X"));
        String texto = ((TextContent) resultado.content().getFirst()).text();
        assertFalse(Boolean.TRUE.equals(resultado.isError()));
        assertTrue(texto.contains("PlayStation 5 Pro | ID: ps5-pro | Fabricante: Sony | Lançamento: 2024 | Geração: 9ª geração"));
        assertTrue(texto.contains("Xbox Series X | ID: xbox-series-x | Fabricante: Microsoft | Lançamento: 2020 | Geração: 9ª geração"));
    }

    @Test
    void deveRetornarMensagemNormalParaConsoleInexistente() {
        var resultado = comparar(Map.of("primeiro", "ps5-pro", "segundo", "inexistente"));
        assertFalse(Boolean.TRUE.equals(resultado.isError()));
        assertEquals("Console não encontrado: inexistente",
                ((TextContent) resultado.content().getFirst()).text());
        assertEquals("Console não encontrado: 3do", texto(comparar(Map.of("primeiro", "ps3", "segundo", "3do"))));
        assertEquals("Console não encontrado: 3do", texto(comparar(Map.of("primeiro", "3do", "segundo", "ps3"))));
    }

    @Test
    void deveRejeitarArgumentosAusentesVaziosOuDeTipoInvalido() {
        assertTrue(Boolean.TRUE.equals(comparar(null).isError()));
        for (Map<String, Object> argumentos : java.util.List.<Map<String, Object>>of(
                Map.of(), Map.of("primeiro", "ps5-pro"),
                Map.of("primeiro", " ", "segundo", "ps2"),
                Map.of("primeiro", "ps2", "segundo", " "),
                Map.of("primeiro", 123, "segundo", "ps2"),
                Map.of("primeiro", "ps2", "segundo", 123))) {
            assertTrue(Boolean.TRUE.equals(comparar(argumentos).isError()));
        }
    }
}
