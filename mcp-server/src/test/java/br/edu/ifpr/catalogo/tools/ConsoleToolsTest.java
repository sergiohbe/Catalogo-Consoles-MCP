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

class ConsoleToolsTest {

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
    void deveRetornarErroParaConsoleInexistente() {
        var resultado = comparar(Map.of("primeiro", "ps5-pro", "segundo", "inexistente"));
        assertTrue(Boolean.TRUE.equals(resultado.isError()));
        assertEquals("Console não encontrado: inexistente",
                ((TextContent) resultado.content().getFirst()).text());
    }

    @Test
    void deveRejeitarArgumentosAusentesVaziosOuDeTipoInvalido() {
        for (Map<String, Object> argumentos : java.util.List.<Map<String, Object>>of(
                Map.of(), Map.of("primeiro", "ps5-pro"),
                Map.of("primeiro", " ", "segundo", "ps2"),
                Map.of("primeiro", "ps2", "segundo", 123))) {
            assertTrue(Boolean.TRUE.equals(comparar(argumentos).isError()));
        }
    }
}
