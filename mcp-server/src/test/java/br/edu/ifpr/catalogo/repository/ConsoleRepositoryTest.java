package br.edu.ifpr.catalogo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConsoleRepositoryTest {

    @Test
    void deveFornecerIdsUnicosParaTodosOsConsoles() {
        var consoles = new ConsoleRepository().listarTodos();
        assertEquals(40, consoles.size());
        assertEquals(consoles.size(), consoles.stream().map(console -> console.getId()).distinct().count());
    }

    @Test
    void deveImpedirQueConsultaAltereCatalogo() {
        var repository = new ConsoleRepository();
        assertThrows(UnsupportedOperationException.class, () -> repository.listarTodos().clear());
        assertEquals(40, repository.listarTodos().size());
    }
}
