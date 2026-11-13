package br.edu.ifpr.catalogo.contexto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.catalogo.model.Console;
import br.edu.ifpr.catalogo.prompt.ComparacaoPrompt;
import br.edu.ifpr.catalogo.repository.ConsoleRepository;
import br.edu.ifpr.catalogo.resource.CatalogoResource;
import br.edu.ifpr.catalogo.service.ConsoleService;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

class ContextoCatalogoTest {

    private final ConsoleService service = new ConsoleService(new ConsoleRepository());

    @Test
    void devePublicarCatalogoJsonComSomenteOsCincoCampos() throws Exception {
        var spec = new CatalogoResource(service).especificacao();
        assertEquals("catalogo://consoles", spec.resource().uri());
        var resultado = spec.readHandler().apply(null, new ReadResourceRequest(CatalogoResource.URI));
        var conteudo = (TextResourceContents) resultado.contents().getFirst();
        assertEquals("application/json", conteudo.mimeType());
        var dados = McpJsonDefaults.getMapper().readValue(conteudo.text(), new TypeRef<List<Map<String, Object>>>() { });
        assertEquals(40, dados.size());
        dados.forEach(console -> assertEquals(Set.of("id", "nome", "fabricante", "anoLancamento", "geracao"), console.keySet()));
        assertEquals("ps1", dados.getFirst().get("id"));
        assertEquals(1994, dados.getFirst().get("anoLancamento"));
    }

    @Test
    void devePublicarListaJsonVaziaQuandoCatalogoVazio() {
        var vazio = new ConsoleService(new ConsoleRepository() {
            @Override
            public List<Console> listarTodos() { return List.of(); }
        });
        var resultado = new CatalogoResource(vazio).especificacao().readHandler()
                .apply(null, new ReadResourceRequest(CatalogoResource.URI));
        assertEquals("[]", ((TextResourceContents) resultado.contents().getFirst()).text());
    }

    @Test
    void deveGerarPromptComDadosCanonicosELimitesDaComparacao() {
        var resultado = new ComparacaoPrompt(service).especificacao().promptHandler().apply(null,
                new GetPromptRequest(ComparacaoPrompt.NOME, Map.of("primeiro", " PS2 ", "segundo", "Dreamcast")));
        String texto = ((TextContent) resultado.messages().getFirst().content()).text();
        assertTrue(texto.contains("PlayStation 2 | ID: ps2 | Fabricante: Sony | Lançamento: 2000 | Geração: 6ª geração"));
        assertTrue(texto.contains("Dreamcast | ID: dreamcast"));
        assertTrue(texto.contains("Não invente preço, desempenho"));
    }

    @Test
    void deveRejeitarPromptComArgumentosInvalidosOuConsolesAusentes() {
        var handler = new ComparacaoPrompt(service).especificacao().promptHandler();
        assertThrows(IllegalArgumentException.class, () -> handler.apply(null, new GetPromptRequest(ComparacaoPrompt.NOME, null)));
        for (Map<String, Object> args : List.<Map<String, Object>>of(Map.of(),
                Map.of("primeiro", "ps2"), Map.of("primeiro", " ", "segundo", "ps2"),
                Map.of("primeiro", 123, "segundo", "ps2"), Map.of("primeiro", "ps2", "segundo", " "),
                Map.of("primeiro", "inexistente", "segundo", "ps2"), Map.of("primeiro", "ps2", "segundo", "inexistente"))) {
            assertThrows(IllegalArgumentException.class,
                    () -> handler.apply(null, new GetPromptRequest(ComparacaoPrompt.NOME, args)));
        }
    }
}
