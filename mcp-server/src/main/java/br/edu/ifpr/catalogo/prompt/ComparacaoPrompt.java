package br.edu.ifpr.catalogo.prompt;

import java.util.List;
import java.util.stream.Collectors;

import br.edu.ifpr.catalogo.service.ConsoleService;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class ComparacaoPrompt {

    public static final String NOME = "orientar_comparacao";
    private final ConsoleService service;

    public ComparacaoPrompt(ConsoleService service) {
        this.service = service;
    }

    public SyncPromptSpecification especificacao() {
        var prompt = new Prompt(NOME, "Orienta uma comparação baseada nos dados do catálogo.", List.of(
                new PromptArgument("primeiro", "ID ou nome completo do primeiro console.", true),
                new PromptArgument("segundo", "ID ou nome completo do segundo console.", true)));
        return new SyncPromptSpecification(prompt, (exchange, request) -> {
            var args = request.arguments();
            if (args == null || !(args.get("primeiro") instanceof String primeiro) || primeiro.isBlank()
                    || !(args.get("segundo") instanceof String segundo) || segundo.isBlank()) {
                throw new IllegalArgumentException("Informe o ID ou nome completo dos dois consoles.");
            }
            String dados = service.compararConsoles(primeiro, segundo).stream()
                    .map(console -> "- " + console.getNome() + " | ID: " + console.getId()
                            + " | Fabricante: " + console.getFabricante()
                            + " | Lançamento: " + console.getAnoLancamento()
                            + " | Geração: " + console.getGeracao())
                    .collect(Collectors.joining("\n"));
            String texto = "Compare os dois consoles usando exclusivamente estes dados do catálogo:\n"
                    + dados + "\nApresente uma tabela com nome, ID, fabricante, ano de lançamento e geração. "
                    + "Explique semelhanças e diferenças verificáveis. Não invente preço, desempenho, "
                    + "jogos ou especificações técnicas ausentes. Informe que esses dados não permitem "
                    + "determinar qual console é melhor para um perfil de uso.";
            return new GetPromptResult("Comparação orientada pelo catálogo.",
                    List.of(new PromptMessage(Role.USER, new TextContent(texto))));
        });
    }
}
