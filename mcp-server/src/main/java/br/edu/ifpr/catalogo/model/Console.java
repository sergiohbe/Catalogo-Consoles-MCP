package br.edu.ifpr.catalogo.model;

public class Console {

    private final String id;
    private final String nome;
    private final String fabricante;
    private final int anoLancamento;
    private final String geracao;
    private final String tipo;
    private final String armazenamento;
    private final String descricao;

    public Console(String id, String nome, String fabricante, int anoLancamento,
                   String geracao, String tipo, String armazenamento, String descricao) {
        this.id = validarTexto(id, "id");
        this.nome = validarTexto(nome, "nome");
        this.fabricante = validarTexto(fabricante, "fabricante");

        if (anoLancamento < 1970) {
            throw new IllegalArgumentException("Ano de lançamento inválido.");
        }

        this.anoLancamento = anoLancamento;
        this.geracao = validarTexto(geracao, "geração");
        this.tipo = validarTexto(tipo, "tipo");
        this.armazenamento = validarTexto(armazenamento, "armazenamento");
        this.descricao = validarTexto(descricao, "descrição");
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

    public String getTipo() {
        return tipo;
    }

    public String getArmazenamento() {
        return armazenamento;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return nome + " (" + fabricante + ", " + anoLancamento + ")";
    }
}