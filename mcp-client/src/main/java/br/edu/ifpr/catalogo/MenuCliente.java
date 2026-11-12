package br.edu.ifpr.catalogo;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class MenuCliente {

    private final List<Tool> ferramentas;
    private final ConsultaCatalogo catalogo;
    private final Scanner entrada;
    private final PrintStream saida;

    public MenuCliente(List<Tool> ferramentas, ConsultaCatalogo catalogo) {
        this(ferramentas, catalogo, new Scanner(System.in), System.out);
    }

    MenuCliente(List<Tool> ferramentas, ConsultaCatalogo catalogo, Scanner entrada, PrintStream saida) {
        this.ferramentas = List.copyOf(ferramentas);
        this.catalogo = catalogo;
        this.entrada = entrada;
        this.saida = saida;
    }

    public void executar() {
        if (ferramentas.isEmpty()) {
            saida.println("O servidor não disponibilizou ferramentas.");
            return;
        }
        while (true) {
            saida.println("\nFerramentas disponíveis:");
            for (int i = 0; i < ferramentas.size(); i++) {
                Tool ferramenta = ferramentas.get(i);
                saida.println((i + 1) + " - " + ferramenta.name());
                if (ferramenta.description() != null) {
                    saida.println("    " + ferramenta.description());
                }
            }
            saida.println("0 - Sair");
            String opcao = lerLinha("Escolha uma opção: ");
            if (opcao == null || opcao.equals("0")) {
                return;
            }
            int indice;
            try {
                indice = Integer.parseInt(opcao) - 1;
            } catch (NumberFormatException e) {
                saida.println("Opção inválida. Escolha um número do menu.");
                continue;
            }
            if (indice < 0 || indice >= ferramentas.size()) {
                saida.println("Opção inválida. Escolha um número do menu.");
                continue;
            }
            Tool ferramenta = ferramentas.get(indice);
            Map<String, Object> argumentos = lerArgumentos(ferramenta);
            if (argumentos == null) {
                if (!entrada.hasNextLine()) {
                    return;
                }
                continue;
            }
            var resultado = catalogo.consultar(ferramenta.name(), argumentos);
            for (var conteudo : resultado.content()) {
                if (conteudo instanceof TextContent texto) {
                    saida.println(texto.text());
                }
            }
        }
    }

    private String lerLinha(String mensagem) {
        saida.print(mensagem);
        saida.flush();
        return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
    }

    private Map<String, Object> lerArgumentos(Tool ferramenta) {
        Map<String, Object> schema = ferramenta.inputSchema();
        if (!(schema.get("properties") instanceof Map<?, ?> propriedades)
                || !propriedades.values().stream().allMatch(valor ->
                        valor instanceof Map<?, ?> campo && "string".equals(campo.get("type")))
                || schema.containsKey("$ref") || schema.containsKey("allOf")
                || schema.containsKey("anyOf") || schema.containsKey("oneOf")) {
            return lerJson(schema);
        }
        List<?> obrigatorios = schema.get("required") instanceof List<?> lista ? lista : List.of();
        var argumentos = new LinkedHashMap<String, Object>();
        var nomes = propriedades.keySet().stream().map(Object::toString).sorted().toList();
        for (String nome : nomes) {
            Map<?, ?> campo = (Map<?, ?>) propriedades.get(nome);
            boolean obrigatorio = obrigatorios.contains(nome);
            saida.println(nome + (obrigatorio ? " (obrigatório)" : " (opcional; Enter para omitir)"));
            if (campo.get("description") != null) {
                saida.println(campo.get("description"));
            }
            String valor = lerLinha("> ");
            if (valor == null) {
                return null;
            }
            if (valor.isBlank()) {
                if (obrigatorio) {
                    saida.println("O parâmetro " + nome + " é obrigatório. Consulta cancelada.");
                    return null;
                }
            } else {
                argumentos.put(nome, valor);
            }
        }
        return argumentos;
    }

    private Map<String, Object> lerJson(Map<String, Object> schema) {
        try {
            saida.println("Parâmetros da ferramenta: " + McpJsonDefaults.getMapper().writeValueAsString(schema));
            String valor = lerLinha("Informe os argumentos como objeto JSON em uma linha: ");
            if (valor == null) {
                return null;
            }
            Map<String, Object> argumentos = McpJsonDefaults.getMapper().readValue(valor,
                    new TypeRef<Map<String, Object>>() { });
            if (argumentos == null) {
                throw new IOException("Objeto JSON esperado.");
            }
            return argumentos;
        } catch (IOException e) {
            saida.println("JSON inválido. Informe um objeto JSON. Consulta cancelada.");
            return null;
        }
    }
}
