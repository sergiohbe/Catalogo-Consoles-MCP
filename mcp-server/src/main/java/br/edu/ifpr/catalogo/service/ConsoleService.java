package br.edu.ifpr.catalogo.service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import br.edu.ifpr.catalogo.model.Console;
import br.edu.ifpr.catalogo.repository.ConsoleRepository;

public class ConsoleService {

    private final ConsoleRepository repository;

    public ConsoleService(ConsoleRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("O repositório é obrigatório.");
        }

        this.repository = repository;
    }

    public List<Console> listarConsoles(String fabricante) {
        if (fabricante == null || fabricante.isBlank()) {
            return repository.listarTodos();
        }

        String filtro = fabricante.trim();

        return repository.listarTodos().stream()
                .filter(console -> console.getFabricante().equalsIgnoreCase(filtro))
                .toList();
    }

    public List<Console> buscarConsoles(String termo) {
        if (termo == null || termo.isBlank()) {
            throw new IllegalArgumentException("Informe um termo para a busca.");
        }

        String filtro = termo.trim().toLowerCase(Locale.ROOT);

        return repository.listarTodos().stream()
                .filter(console -> textoParaBusca(console).contains(filtro))
                .toList();
    }

    private String textoParaBusca(Console console) {
        return String.join(" ",
                console.getId(),
                console.getNome(),
                console.getFabricante(),
                console.getGeracao(),
                console.getTipo(),
                console.getArmazenamento(),
                console.getDescricao()
        ).toLowerCase(Locale.ROOT);
    }

    public Optional<Console> obterConsole(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }

        String busca = identificador.trim();

        return repository.listarTodos().stream()
                .filter(console ->
                        console.getId().equalsIgnoreCase(busca)
                        || console.getNome().equalsIgnoreCase(busca))
                .findFirst();
    }

    public List<Console> compararConsoles(String primeiro, String segundo) {
        Console console1 = obterConsole(primeiro)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Console não encontrado: " + primeiro));

        Console console2 = obterConsole(segundo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Console não encontrado: " + segundo));

        return List.of(console1, console2);
    }

    public int contarConsoles() {
        return repository.listarTodos().size();
    }

    public Map<String, Long> contarPorFabricante() {
        Map<String, Long> contagem = repository.listarTodos().stream()
                .collect(Collectors.groupingBy(
                        Console::getFabricante,
                        TreeMap::new,
                        Collectors.counting()
                ));

        return Collections.unmodifiableMap(contagem);
    }
}