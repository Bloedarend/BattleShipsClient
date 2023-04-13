package nl.gerwint.plugin.models;

import nl.gerwint.plugin.BattleShipsPlugin;
import nl.gerwint.plugin.BattleShipsPluginClient;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;


public class Game {

    private BattleShipsPlugin plugin;
    private int[][] grid = new int[25][25];
    private int turn;

    public Game(BattleShipsPlugin plugin) {
        this.plugin = plugin;
    }

    public GridState getGridState(int x, int y, int id) {
        int value = grid[x][y];

        if (value == -1) {
            return GridState.MISS;
        }

        if (value == 0) {
            return GridState.UNKNOWN;
        }

        if (value == id) {
            return GridState.ALLY;
        }

        return GridState.ENEMY;
    }

    public Material getMaterial(int x, int y, int id) {
        int value = grid[x][y];
        boolean isDark = isEven(x) && isEven(y) || !isEven(x) && !isEven(y);

        if (value == -1) {
            if (isDark) {
                return Material.LIGHT_GRAY_WOOL;
            } else {
                return Material.WHITE_WOOL;
            }
        }

        if (value == 0) {
            if (isDark) {
                return Material.BLACK_WOOL;
            } else {
                return Material.GRAY_WOOL;
            }
        }

        if (value == id) {
            return Material.LIGHT_BLUE_WOOL;
        } else {
            return Material.RED_WOOL;
        }
    }

    public void updateGrid(int x, int y, int id, GridState state) {
        // Update the board.
        if (state == GridState.MISS) {
            grid[x][y] = -1;
        } else if (state == GridState.UNKNOWN) {
            grid[x][y] = 0;
        } else if (state == GridState.ALLY) {
            grid[x][y] = id;
        } else if (state == GridState.ENEMY) {
            grid[x][y] = id;
        }
    }

    public void updateBlock(int x, int y, BattleShipsPluginClient client) {
        Runnable runnable = () -> {
            Location location = client.getLocation();
            Material material = getMaterial(x, y, client.getId());

            Location block = new Location(location.getWorld(), location.getX() - x + 12, location.getY() - y + 18, location.getZ() + 21);
            location.getWorld().getBlockAt(block).setType(material);
        };

        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public void setTurn(int id) {
        turn = id;
    }

    private boolean isEven(int i) {
        return (i % 2) == 0;
    }

}
