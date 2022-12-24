package ca.adaptor.zombies.game.util;

import ca.adaptor.zombies.game.model.ZombiesTile;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.StringTokenizer;
import java.util.UUID;

@Component
public class ZombiesTileImporter implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesTileImporter.class);

    @Autowired
    private ZombiesTileRepository repository;

    @Override
    public void run(String... args) {
        try {
            importTiles();
        }
        catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void importTiles() throws IOException {
        var file = ResourceUtils.getFile("./src/main/resources/static/tiles.csv");
        try(var in = new BufferedReader(new FileReader(file))) {
            in.lines()
                .parallel()
                .forEach(this::parseLine)
                ;
        }
        repository.flush();
    }

    private void parseLine(String line) {
        line = line.trim();
        if(!line.isEmpty() && !line.startsWith("#")) {
            var st = new StringTokenizer(line, ",", false);
            var tile = new ZombiesTile(
                    UUID.randomUUID(),
                    new ZombiesTile.SquareType[] {
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                            ZombiesTile.SquareType.values()[Integer.parseInt(st.nextToken())],
                    },
                    Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()),
                    st.nextToken()
            );

            if(!repository.existsByName(tile.getName())) {
                LOGGER.debug("Creating tile: " + tile.getName());
                repository.save(tile);
            }
        }
    }
}
