package ca.adaptor.zombies.game.util;

import ca.adaptor.zombies.game.model.ZombiesMap;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Random;

public class ZombiesMapGenerator {
    private static Random RNG = new Random();

    @NonNull
    public static ZombiesMap create(int width, int height) {
        var ret = new ZombiesMap(width, height);
        for(int i = 0; i < 64; i++) {
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    ret.set(x, y, randomWeighted(ret, x, y));
                }
            }
        }
        cleanup(ret);
        return ret;
    }

    private static void cleanup(ZombiesMap map) {
        for (int x = 1; x < map.getWidth() - 1; x++) {
            for (int y = 1; y < map.getHeight() - 1; y++) {
                var area = map.getAdjacent(x, y);
                var ct = area[1][1];
                if(ct != area[0][0]
                        && ct != area[0][1]
                        && ct != area[0][2]
                        && ct != area[1][0]
                        && ct != area[1][2]
                        && ct != area[2][0]
                        && ct != area[2][1]
                        && ct != area[2][2]
                ) {
                    map.set(x,y,area[0][0]);
                }
            }
        }
    }

    private static ZombiesMap.Tile random() {
        return ZombiesMap.Tile.values()[RNG.nextInt(ZombiesMap.Tile.values().length)];
    }

    private static ZombiesMap.Tile randomWeighted(ZombiesMap map, int x, int y) {
        var area = map.getAdjacent(x, y);
        var weights = getWeights(area);
        var weight = Arrays.stream(weights).sum();
        var rand = RNG.nextInt(weight);
        int i = -1;
        while(rand >= 0) {
            rand -= weights[++i];
        }
        return ZombiesMap.Tile.values()[i];
    }

    private static int[] getWeights(ZombiesMap.Tile[][] area) {
        var ret = new int[ZombiesMap.Tile.values().length];
        for(int i = 0; i < ret.length; i++) {
            ret[i] = ZombiesMap.Tile.values()[i].getWeight();
        }
        for (var tiles : area) {
            for (var tile : tiles) {
                if (tile != null) {
                    ret[tile.ordinal()] += tile.getWeight();
                }
            }
        }
        return ret;
    }
}
