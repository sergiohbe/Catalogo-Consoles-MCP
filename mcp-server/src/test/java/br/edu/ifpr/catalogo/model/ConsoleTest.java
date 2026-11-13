package br.edu.ifpr.catalogo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConsoleTest {

    @Test
    void deveNormalizarTextosEPreservarDadosDoConsole() {
        var console = new Console(" ps2 ", " PlayStation 2 ", " Sony ", 2000, " 6ª geração ");
        assertEquals("ps2", console.getId());
        assertEquals("PlayStation 2", console.getNome());
        assertEquals("Sony", console.getFabricante());
        assertEquals(2000, console.getAnoLancamento());
        assertEquals("6ª geração", console.getGeracao());
        assertEquals("PlayStation 2 (Sony, 2000)", console.toString());
    }

    @Test
    void deveRejeitarCadaCampoTextualNuloOuEmBranco() {
        String[] campos = { "id", "nome", "fabricante", "geração" };
        for (int campo = 0; campo < campos.length; campo++) {
            for (String invalido : new String[] { null, "", " \t\n" }) {
                String[] valores = { "ps2", "PlayStation 2", "Sony", "6ª geração" };
                valores[campo] = invalido;
                var erro = assertThrows(IllegalArgumentException.class,
                        () -> new Console(valores[0], valores[1], valores[2], 2000, valores[3]));
                assertEquals("O campo " + campos[campo] + " é obrigatório.", erro.getMessage());
            }
        }
    }

}
