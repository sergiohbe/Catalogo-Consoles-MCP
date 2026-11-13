package br.edu.ifpr.catalogo.model;

public class Console {

    private final String id;
    private final String nome;
    private final String fabricante;
    private final int anoLancamento;
    private final String geracao;

    public Console(String id, String nome, String fabricante, int anoLancamento,
                   String geracao) {
        this.id = validarTexto(id, "id");
        this.nome = validarTexto(nome, "nome");
        this.fabricante = validarTexto(fabricante, "fabricante");

        this.anoLancamento = anoLancamento;
        this.geracao = validarTexto(geracao, "geração");
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo " + campo + " é obrigatório.");
        }

        return valor.trim();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getFabricante() {
        return fabricante;
    }

    public int getAnoLancamento() {
        return anoLancamento;
    }

    public String getGeracao() {
        return geracao;
    }

    @Override
    public String toString() {
        return nome + " (" + fabricante + ", " + anoLancamento + ")";
    }
}
