package br.edu.ifpr.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.ifpr.catalogo.repository.ConsoleRepository;

class ConsoleServiceTest {

    private ConsoleService service;

    @BeforeEach
    void configurar() {
        service = new ConsoleService(new ConsoleRepository());
    }

    @Test
    void deveListarTodosOsConsoles() {
        assertEquals(40, service.listarConsoles(null).size());
    }

    @Test
    void deveFiltrarPorFabricante() {
        assertEquals(11, service.listarConsoles("Microsoft").size());
        assertEquals(4, service.listarConsoles("Sega").size());
        assertEquals(4, service.listarConsoles("Atari").size());
    }

    @Test
    void deveBuscarPorNome() {
        assertEquals(12, service.buscarConsoles("PlayStation").size());
    }

    @Test
    void deveLocalizarConsolePorId() {
        var console = service.obterConsole("ps5-pro");

        assertTrue(console.isPresent());
        assertEquals("PlayStation 5 Pro", console.get().getNome());
    }

    @Test
    void deveIgnorarMaiusculasNaBuscaPorNomeCompleto() {
        var console = service.obterConsole("nintendo switch oled");

        assertTrue(console.isPresent());
        assertEquals("switch-oled", console.get().getId());
    }

    @Test
    void deveRetornarVazioQuandoConsoleNaoExiste() {
        assertFalse(service.obterConsole("console-inexistente").isPresent());
    }

    @Test
    void deveCompararDoisConsoles() {
        var consoles = service.compararConsoles("ps5-pro", "xbox-series-x");

        assertEquals(2, consoles.size());
        assertEquals("PlayStation 5 Pro", consoles.get(0).getNome());
        assertEquals("Xbox Series X", consoles.get(1).getNome());
    }

    @Test
    void deveContarConsolesPorFabricante() {
        var contagem = service.contarPorFabricante();

        assertEquals(12L, contagem.get("Sony"));
        assertEquals(11L, contagem.get("Microsoft"));
        assertEquals(9L, contagem.get("Nintendo"));
        assertEquals(4L, contagem.get("Sega"));
        assertEquals(4L, contagem.get("Atari"));
    }

    @Test
    void deveRejeitarBuscaVazia() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarConsoles(" ")
        );
    }
}
