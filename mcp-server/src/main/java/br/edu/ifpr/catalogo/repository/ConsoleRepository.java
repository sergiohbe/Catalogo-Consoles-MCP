package br.edu.ifpr.catalogo.repository;

import java.util.List;

import br.edu.ifpr.catalogo.model.Console;

public class ConsoleRepository {

    private final List<Console> consoles;

    public ConsoleRepository() {
        this.consoles = List.of(
            new Console("ps1", "PlayStation", "Sony", 1994, "5ª geração"),
            new Console("ps2", "PlayStation 2", "Sony", 2000, "6ª geração"),
            new Console("ps2-slim", "PlayStation 2 Slim", "Sony", 2004, "6ª geração"),
            new Console("ps3", "PlayStation 3", "Sony", 2006, "7ª geração"),
            new Console("ps3-slim", "PlayStation 3 Slim", "Sony", 2009, "7ª geração"),
            new Console("ps3-super-slim", "PlayStation 3 Super Slim", "Sony", 2012, "7ª geração"),
            new Console("ps4", "PlayStation 4", "Sony", 2013, "8ª geração"),
            new Console("ps4-slim", "PlayStation 4 Slim", "Sony", 2016, "8ª geração"),
            new Console("ps4-pro", "PlayStation 4 Pro", "Sony", 2016, "8ª geração"),
            new Console("ps5", "PlayStation 5", "Sony", 2020, "9ª geração"),
            new Console("ps5-slim", "PlayStation 5 Slim", "Sony", 2023, "9ª geração"),
            new Console("ps5-pro", "PlayStation 5 Pro", "Sony", 2024, "9ª geração"),

            new Console("xbox", "Xbox", "Microsoft", 2001, "6ª geração"),
            new Console("xbox-360", "Xbox 360", "Microsoft", 2005, "7ª geração"),
            new Console("xbox-360-elite", "Xbox 360 Elite", "Microsoft", 2007, "7ª geração"),
            new Console("xbox-360-slim", "Xbox 360 Slim", "Microsoft", 2010, "7ª geração"),
            new Console("xbox-360-super-slim", "Xbox 360 Super Slim", "Microsoft", 2013, "7ª geração"),
            new Console("xbox-one", "Xbox One", "Microsoft", 2013, "8ª geração"),
            new Console("xbox-one-elite", "Xbox One Elite", "Microsoft", 2015, "8ª geração"),
            new Console("xbox-one-s", "Xbox One S", "Microsoft", 2016, "8ª geração"),
            new Console("xbox-one-x", "Xbox One X", "Microsoft", 2017, "8ª geração"),
            new Console("xbox-series-s", "Xbox Series S", "Microsoft", 2020, "9ª geração"),
            new Console("xbox-series-x", "Xbox Series X", "Microsoft", 2020, "9ª geração"),

            new Console("master-system", "Master System", "Sega", 1985, "3ª geração"),
            new Console("mega-drive", "Mega Drive (Genesis)", "Sega", 1988, "4ª geração"),
            new Console("saturn", "Sega Saturn", "Sega", 1994, "5ª geração"),
            new Console("dreamcast", "Dreamcast", "Sega", 1998, "6ª geração"),

            new Console("nes", "Nintendo Entertainment System (NES)", "Nintendo", 1983, "3ª geração"),
            new Console("snes", "Super Nintendo Entertainment System (SNES)", "Nintendo", 1990, "4ª geração"),
            new Console("n64", "Nintendo 64", "Nintendo", 1996, "5ª geração"),
            new Console("gamecube", "Nintendo GameCube", "Nintendo", 2001, "6ª geração"),
            new Console("wii", "Wii", "Nintendo", 2006, "7ª geração"),
            new Console("wii-u", "Wii U", "Nintendo", 2012, "8ª geração"),
            new Console("switch", "Nintendo Switch", "Nintendo", 2017, "8ª geração"),
            new Console("switch-lite", "Nintendo Switch Lite", "Nintendo", 2019, "8ª geração"),
            new Console("switch-oled", "Nintendo Switch OLED", "Nintendo", 2021, "8ª geração"),

            new Console("atari-2600", "Atari 2600", "Atari", 1977, "2ª geração"),
            new Console("atari-5200", "Atari 5200", "Atari", 1982, "2ª geração"),
            new Console("atari-7800", "Atari 7800", "Atari", 1986, "3ª geração"),
            new Console("atari-jaguar", "Atari Jaguar", "Atari", 1993, "5ª geração")
        );
    }

    public List<Console> listarTodos() {
        return consoles;
    }
}
