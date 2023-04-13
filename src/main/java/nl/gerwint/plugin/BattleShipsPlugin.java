package nl.gerwint.plugin;

import nl.gerwint.plugin.models.Game;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class BattleShipsPlugin extends JavaPlugin implements Listener {

    private HashMap<Integer, Player> players;
    private HashMap<String, BattleShipsPluginClient> clients;
    private Game game;

    @Override
    public void onEnable() {
        players = new HashMap<>();
        clients = new HashMap<>();
        game = new Game(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();
        BattleShipsPluginClient client;

        String exception;

        // Register the player to the server on port 55555.
        if (!clients.containsKey(username)) {
            client = new BattleShipsPluginClient(username, this);
            clients.put(username, client);

            // Determine the location for the arena.
            Location location = new Location(player.getWorld(), 0.5 - clients.size() * 30, 100, 0.5, 0, 0);
            client.setLocation(location);

            exception = "&7We are attempting to connect you to the BattleShips server.\nTry &creconnecting &7in a few seconds.";
        } else {
            client = clients.get(username);
            players.put(client.getId(), player);
            client.reconnect(username);

            exception = "&cUnable to connect.\nIs there a BattleShips server running on port &455555&c?";
        }

        // Make sure a connection has been made.
        client.ping();
        if (!client.serverIsOnline()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.translateAlternateColorCodes('&', exception));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();
        BattleShipsPluginClient client = clients.get(username);

        if (client.serverIsOnline()) {
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', "&8[&4&lBATTLESHIPS&8] &cPlayer &4" + username + " &chas joined the game!"));
        } else {
            event.setJoinMessage(null);
        }

        // Create arena.
        createArena(player, client.getLocation());

        // Teleport player to the arena.
        player.teleport(client.getLocation());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String username = event.getPlayer().getName();
        BattleShipsPluginClient client = clients.get(username);

        // Close connection to the server.
        if (client.serverIsOnline()) {
            client.close();
            event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "&8[&4&lBATTLESHIPS&8] &cPlayer &4" + username + " &chas left the game!"));
        } else {
            event.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = player.getTargetBlockExact(100, FluidCollisionMode.NEVER);

        // Ignore interact if it isn't the player's turn.
        if (game.getTurn() != getClient(player).getId()) return;

        int x = (int) Math.abs(block.getX() - 13 - player.getLocation().getX());
        int y = (int) Math.abs(block.getY() - 19 - player.getLocation().getY());

        getClient(player).launchTorpedo(x, y);

        player.sendMessage("" + x);
        player.sendMessage("" + y);
    }

    private void createArena(Player player, Location location) {
        double mx = location.getX();
        double my = location.getY();
        double mz = location.getZ();

        double dy = my - 7;
        double uy = my + 19;

        double lx = mx - 13;
        double rx = mx + 13;

        double rz = mz - 3;
        double fz = mz + 21;

        World world = player.getWorld();

        // Build the floor and ceiling.
        for (double i = lx; i <= rx; i++) {
            for (double j = rz; j <= fz; j++) {
                Location floor = new Location(world, i, dy, j);
                world.getBlockAt(floor).setType(Material.STONE_BRICKS);

                Location ceiling = new Location(world, i, uy, j);
                Material material = i == lx + 1 || j == rz + 1 || i == rx - 1 || j == fz - 1 ? Material.STONE_BRICKS : Material.GRAY_STAINED_GLASS;
                world.getBlockAt(ceiling).setType(material);
            }
        }

        // Build the left and right wall.
        for (double i = dy; i <= uy; i++) {
            for (double j = rz; j <= fz; j++) {
                Location left = new Location(world, lx, i, j);
                world.getBlockAt(left).setType(Material.STONE_BRICKS);

                Location right = new Location(world, rx, i, j);
                world.getBlockAt(right).setType(Material.STONE_BRICKS);
            }
        }

        // Build the rear and front wall.
        for (double i = dy; i <= uy; i++) {
            for (double j = lx; j <= rx; j++) {
                Location rear = new Location(world, j, i, rz);
                world.getBlockAt(rear).setType(Material.STONE_BRICKS);

                if (i != dy && j != lx && i != uy && j != rx) {
                    Location front = new Location(world, j, i , fz);
                    Material material = game.getMaterial((int) (j + 12 - location.getX()), (int) (i + 6 - location.getY()), getClient(player).getId());
                    world.getBlockAt(front).setType(material);
                }
            }
        }

        // Clear the inside of the box.
        for (double i = lx + 1; i < rx; i++) {
            for (double j = rz + 1; j < fz; j++) {
                for (double k = dy + 1; k < uy; k++) {
                    Location inside = new Location(world, i, k, j);
                    world.getBlockAt(inside).setType(Material.AIR);
                }
            }
        }

        // Build the platform.
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Location platform = new Location(world, mx + i, my - 1, mz + j);
                Location border = new Location(world, mx + i, my, mz + j);

                world.getBlockAt(platform).setType(Material.STONE_BRICKS);

                if (!(i == 0 && j == 0)) {
                    world.getBlockAt(border).setType(Material.STONE_BRICKS);
                }
            }
        }

        Location roof = new Location(world, mx, my + 2, mz);
        world.getBlockAt(roof).setType(Material.STONE_BRICKS);
    }

    public Player getPlayer(int id) {
        return players.get(id);
    }

    public BattleShipsPluginClient getClient(Player player) {
        return clients.get(player.getName());
    }

    public Game getGame() {
        return game;
    }

    public void resetGame() {
        this.game = new Game(this);
        this.clients.clear();
        this.players.clear();
    }

}
