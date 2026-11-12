package br.edu.ifpr.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.ifpr.catalogo.repository.ConsoleRepository;
import br.edu.ifpr.catalogo.model.Console;

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

    @Test
    void deveRejeitarRepositorioNulo() {
        assertThrows(IllegalArgumentException.class, () -> new ConsoleService(null));
    }

    @Test
    void deveAceitarFiltroEmBrancoENormalizarFabricante() {
        assertEquals(40, service.listarConsoles(" \t").size());
        assertEquals(4, service.listarConsoles(" sEgA ").size());
        assertTrue(service.listarConsoles("inexistente").isEmpty());
    }

    @Test
    void deveBuscarPorIdFabricanteEGeracaoIgnorandoEspacosEMaiusculas() {
        assertEquals("ps5-pro", service.buscarConsoles(" PS5-PRO ").getFirst().getId());
        assertEquals(4, service.buscarConsoles(" sEgA ").size());
        assertEquals(3, service.buscarConsoles("3ª geração").size());
        assertTrue(service.buscarConsoles("inexistente").isEmpty());
    }

    @Test
    void deveRejeitarBuscaNulaOuVazia() {
        for (String termo : new String[] { null, "", "\t" }) {
            assertThrows(IllegalArgumentException.class, () -> service.buscarConsoles(termo));
        }
    }

    @Test
    void deveRetornarOptionalVazioParaIdentificadorInvalido() {
        for (String identificador : new String[] { null, "", " \t" }) {
            assertTrue(service.obterConsole(identificador).isEmpty());
        }
    }

    @Test
    void deveNormalizarIdentificadorENomeNaConsultaIndividual() {
        assertEquals("ps2", service.obterConsole(" PS2 ").orElseThrow().getId());
        assertEquals("ps2", service.obterConsole(" PlayStation 2 ").orElseThrow().getId());
    }

    @Test
    void deveRejeitarPrimeiroOuSegundoConsoleInvalidoNaComparacao() {
        for (String identificador : new String[] { null, "", " ", "inexistente" }) {
            var primeiro = assertThrows(IllegalArgumentException.class,
                    () -> service.compararConsoles(identificador, "ps2"));
            var segundo = assertThrows(IllegalArgumentException.class,
                    () -> service.compararConsoles("ps2", identificador));
            assertEquals("Console não encontrado: " + identificador, primeiro.getMessage());
            assertEquals("Console não encontrado: " + identificador, segundo.getMessage());
        }
    }

    @Test
    void devePermitirCompararMesmoConsoleSemAlterarCatalogo() {
        var comparacao = service.compararConsoles("ps2", "PlayStation 2");
        assertEquals(List.of("ps2", "ps2"), comparacao.stream().map(Console::getId).toList());
        assertEquals(40, service.contarConsoles());
    }

    @Test
    void deveContarTotalEImpedirAlteracaoDasEstatisticas() {
        assertEquals(40, service.contarConsoles());
        assertEquals(40L, service.contarPorFabricante().values().stream().mapToLong(Long::longValue).sum());
        assertEquals(List.of("Atari", "Microsoft", "Nintendo", "Sega", "Sony"),
                List.copyOf(service.contarPorFabricante().keySet()));
        assertThrows(UnsupportedOperationException.class, () -> service.contarPorFabricante().put("Outra", 1L));
    }

    @Test
    void deveTratarCatalogoVazio() {
        var vazio = new ConsoleService(new ConsoleRepository() {
            @Override
            public List<Console> listarTodos() {
                return List.of();
            }
        });
        assertTrue(vazio.listarConsoles(null).isEmpty());
        assertTrue(vazio.listarConsoles("Sony").isEmpty());
        assertTrue(vazio.buscarConsoles("ps2").isEmpty());
        assertTrue(vazio.obterConsole("ps2").isEmpty());
        assertEquals(0, vazio.contarConsoles());
        assertEquals(Map.of(), vazio.contarPorFabricante());
        assertThrows(IllegalArgumentException.class, () -> vazio.compararConsoles("ps2", "ps3"));
    }
}
