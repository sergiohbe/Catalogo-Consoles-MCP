package br.edu.ifpr.catalogo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ComandosContextoTest {

    @Test
    void deveAceitarMenuBuscaEComandosDeContexto() {
        for (String[] args : List.of(new String[] {}, new String[] { "Dreamcast" },
                new String[] { "--recursos" }, new String[] { "--recurso", "catalogo://consoles" },
                new String[] { "--prompts" }, new String[] { "--prompt", "orientar_comparacao", "primeiro=ps2", "segundo=Dreamcast" })) {
            assertDoesNotThrow(() -> ComandosContexto.validar(args));
        }
    }

    @Test
    void deveRejeitarComandosInvalidosEArgumentosDuplicados() {
        for (String[] args : List.of(new String[] { "--outro" }, new String[] { "--recurso" },
                new String[] { "--recursos", "extra" }, new String[] { "--prompt" },
                new String[] { "--prompt", "nome", "sem-separador" },
                new String[] { "--prompt", "nome", "primeiro=" },
                new String[] { "--prompt", "nome", "primeiro=ps2", "primeiro=ps3" })) {
            assertThrows(IllegalArgumentException.class, () -> ComandosContexto.validar(args));
        }
    }

    @Test
    void devePreservarTextoDosArgumentosDoPrompt() {
        assertEquals(Map.of("primeiro", "PlayStation 2", "segundo", "id=com=igual"),
                ComandosContexto.argumentos(new String[] { "--prompt", "nome", "primeiro=PlayStation 2", "segundo=id=com=igual" }));
    }
}
