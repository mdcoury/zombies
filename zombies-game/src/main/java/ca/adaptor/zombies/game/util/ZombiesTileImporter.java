package ca.adaptor.zombies.game.util;

import ca.adaptor.zombies.game.model.ZombiesTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.UUID;

@Component
public class ZombiesTileImporter implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesTileImporter.class);

    private final ZombiesEntityManagerHelper entityManager;

    public ZombiesTileImporter(@Autowired ZombiesEntityManagerHelper entityManager) {
        this.entityManager = entityManager;
    }

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

            if(!entityManager.existsByName(tile.getName(), ZombiesTile.class)) {
                tile = entityManager.save(tile);
//                LOGGER.trace("Created tile: " + tile);
            }
        }
    }
}
