package nl.gerwint.plugin.models;

import nl.gerwint.plugin.BattleShipsPlugin;
import nl.gerwint.plugin.BattleShipsPluginClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;


public class Game {

    private BattleShipsPlugin plugin;
    private int[][] grid;
    private int turn;

    public Game(BattleShipsPlugin plugin) {
        this.plugin = plugin;
        this.grid = new int[25][25];
        this.turn = 0;
    }

    public boolean canHit(int x, int y, int id) {
        Player player = plugin.getPlayer(id);
        Location location = new Location(player.getWorld(), player.getLocation().getX() - x + 12, player.getLocation().getY() - y + 18, player.getLocation().getZ() + 21);
        Material material = location.getWorld().getBlockAt(location).getType();

        return material == Material.BLACK_WOOL || material == Material.GRAY_WOOL;
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

    public void updateGrid(int x, int y, int id, int idHit, GridState state) {
        // Update the board.
        if (state == GridState.MISS) {
            grid[x][y] = -1;
        } else if (state == GridState.UNKNOWN) {
            grid[x][y] = 0;
        } else if (state == GridState.ALLY) {
            grid[x][y] = id;
        } else if (state == GridState.ENEMY) {
            grid[x][y] = idHit;

            if (idHit == id) {
                plugin.getClient(plugin.getPlayer(id)).isHit = true;
                plugin.getPlayer(id).sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&4&lBATTLESHIPS&8] &cYou have been hit!"));
            }
        }
    }

    public void updateBlock(int x, int y, BattleShipsPluginClient client) {
        Runnable runnable = () -> {
            Location location = client.getLocation();
            Material material = getMaterial(x, y, client.getId());

            if (client.isHit) {
                client.isHit = false;
                material = Material.YELLOW_WOOL;
            }

            Location block = new Location(location.getWorld(), location.getX() - x + 12, location.getY() - y + 18, location.getZ() + 21);
            location.getWorld().getBlockAt(block).setType(material);
        };

        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int id) {
        turn = id;
    }

    private boolean isEven(int i) {
        return (i % 2) == 0;
    }

}
