package br.edu.ifpr.catalogo;

import java.util.concurrent.TimeoutException;

class FalhaComunicacao extends RuntimeException {

    FalhaComunicacao(String operacao, RuntimeException causa) {
        super(mensagem(operacao, causa), causa);
    }

    private static String mensagem(String operacao, Throwable causa) {
        for (Throwable atual = causa; atual != null; atual = atual.getCause()) {
            if (atual instanceof TimeoutException) {
                return "Tempo limite excedido ao " + operacao
                        + ". O servidor não respondeu a tempo. Execute o cliente novamente.";
            }
        }
        return "Não foi possível " + operacao
                + ". Verifique se o JAR do servidor é válido e está atualizado. "
                + "O servidor pode ter sido encerrado ou a conexão interrompida. "
                + "Execute o cliente novamente.";
    }
}
