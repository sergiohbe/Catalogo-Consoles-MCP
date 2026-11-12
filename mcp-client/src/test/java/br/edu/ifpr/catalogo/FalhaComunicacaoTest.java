package br.edu.ifpr.catalogo;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

class FalhaComunicacaoTest {

    @Test
    void deveIdentificarTimeoutDentroDaCadeiaDeCausas() {
        var causa = new RuntimeException(new IllegalStateException(new TimeoutException()));
        var falha = new FalhaComunicacao("consultar a ferramenta listar_consoles", causa);
        assertTrue(falha.getMessage().contains("Tempo limite excedido"));
        assertTrue(falha.getMessage().contains("listar_consoles"));
        assertSame(causa, falha.getCause());
    }

    @Test
    void deveOrientarSobreFalhaDeInicializacao() {
        var falha = new FalhaComunicacao("conectar ao servidor MCP",
                new RuntimeException(new IOException("Processo indisponível")));
        assertTrue(falha.getMessage().contains("conectar ao servidor MCP"));
        assertTrue(falha.getMessage().contains("JAR do servidor"));
        assertTrue(falha.getMessage().contains("Execute o cliente novamente"));
    }

    @Test
    void deveOrientarSobreFalhaDuranteConsulta() {
        var falha = new FalhaComunicacao("consultar a ferramenta buscar_consoles",
                new IllegalStateException("Connection closed"));
        assertTrue(falha.getMessage().contains("buscar_consoles"));
        assertTrue(falha.getMessage().contains("conexão interrompida"));
    }
}
