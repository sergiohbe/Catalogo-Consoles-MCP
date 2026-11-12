package br.edu.ifpr.catalogo.repository;

import java.util.List;

import br.edu.ifpr.catalogo.model.Console;

public class ConsoleRepository {

    private final List<Console> consoles;

    public ConsoleRepository() {
        this.consoles = List.of(
            new Console("ps1", "PlayStation", "Sony", 1994, "5ª geração", "Mesa", "Cartão de memória", "Primeiro console doméstico da linha PlayStation."),
            new Console("ps2", "PlayStation 2", "Sony", 2000, "6ª geração", "Mesa", "Cartão de memória", "Console compatível com jogos em mídia DVD."),
            new Console("ps3", "PlayStation 3", "Sony", 2006, "7ª geração", "Mesa", "Disco rígido, conforme versão", "Console com suporte a jogos em alta definição."),
            new Console("ps4", "PlayStation 4", "Sony", 2013, "8ª geração", "Mesa", "500 GB ou 1 TB, conforme versão", "Console focado em jogos em alta definição."),
            new Console("ps4-pro", "PlayStation 4 Pro", "Sony", 2016, "8ª geração", "Mesa", "1 TB ou 2 TB, conforme versão", "Versão aprimorada do PlayStation 4."),
            new Console("ps5", "PlayStation 5", "Sony", 2020, "9ª geração", "Mesa", "SSD de 825 GB no modelo original", "Console com armazenamento SSD e suporte a ray tracing."),
            new Console("ps5-slim", "PlayStation 5 Slim", "Sony", 2023, "9ª geração", "Mesa", "SSD de 1 TB", "Versão compacta do PlayStation 5."),
            new Console("ps5-pro", "PlayStation 5 Pro", "Sony", 2024, "9ª geração", "Mesa", "SSD de 2 TB", "Versão aprimorada do PlayStation 5 com maior capacidade gráfica."),

            new Console("xbox", "Xbox", "Microsoft", 2001, "6ª geração", "Mesa", "Disco rígido, conforme versão", "Primeiro console da linha Xbox."),
            new Console("xbox-360", "Xbox 360", "Microsoft", 2005, "7ª geração", "Mesa", "Armazenamento variável conforme versão", "Console com forte integração a serviços online."),
            new Console("xbox-one", "Xbox One", "Microsoft", 2013, "8ª geração", "Mesa", "500 GB ou 1 TB, conforme versão", "Console com integração a jogos e aplicações multimídia."),
            new Console("xbox-one-x", "Xbox One X", "Microsoft", 2017, "8ª geração", "Mesa", "1 TB", "Versão aprimorada do Xbox One."),
            new Console("xbox-series-s", "Xbox Series S", "Microsoft", 2020, "9ª geração", "Mesa", "SSD de 512 GB ou 1 TB, conforme versão", "Console compacto com foco em jogos digitais."),
            new Console("xbox-series-x", "Xbox Series X", "Microsoft", 2020, "9ª geração", "Mesa", "SSD de 1 TB ou 2 TB, conforme versão", "Console de alto desempenho da geração Xbox Series."),

            new Console("wii", "Wii", "Nintendo", 2006, "7ª geração", "Mesa", "Memória interna de 512 MB", "Console conhecido por seus controles com sensores de movimento."),
            new Console("switch", "Nintendo Switch", "Nintendo", 2017, "8ª geração", "Híbrido", "Memória interna de 32 GB", "Console que pode ser utilizado na televisão ou no modo portátil."),
            new Console("switch-lite", "Nintendo Switch Lite", "Nintendo", 2019, "8ª geração", "Portátil", "Memória interna de 32 GB", "Versão do Switch dedicada ao uso portátil."),
            new Console("switch-oled", "Nintendo Switch OLED", "Nintendo", 2021, "8ª geração", "Híbrido", "Memória interna de 64 GB", "Versão do Switch com tela OLED.")
        );
    }

    public List<Console> listarTodos() {
        return consoles;
    }
}