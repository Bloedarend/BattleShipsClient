package nl.gerwint.plugin;

import nl.gerwint.client.BattleShipsClient;
import nl.gerwint.plugin.models.Game;
import nl.gerwint.plugin.models.GridState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class BattleShipsPluginClient extends BattleShipsClient {

    private BattleShipsPlugin plugin;
    private Game game;
    private boolean serverIsOnline;
    private int id;
    private Location location;

    public boolean isHit;

    public BattleShipsPluginClient(String host, int port, String username, BattleShipsPlugin plugin) {
        super(host, port, username);

        this.isHit = false;
        this.plugin = plugin;
        this.game = plugin.getGame();
        this.serverIsOnline = false;
    }

    @Override
    protected void onPos(int x, int y) {
        game.updateGrid(x, y, id, id, GridState.ALLY);
        game.updateBlock(x, y, this);
    }

    @Override
    protected void onNewGame(int x, int y) {
        Runnable runnable = () -> {
            plugin.getPlayer(id).sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&4&lBATTLESHIPS&8] &cThe game has begun!"));
        };

        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void onWinner(int id) {
        Runnable runnable = () -> {
            String message = id == this.id ? "&7You have &cwon &7the game!" : "&7You have &clost &7the game!";

            plugin.getPlayer(this.id).kickPlayer(ChatColor.translateAlternateColorCodes('&', message));
            plugin.removePlayer(this.id);
            plugin.resetGame();
        };

        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    protected void onTurn(int playerNumber) {
        game.setTurn(playerNumber);

        System.out.println("turn: " + playerNumber + ", id:" + getId());
        Runnable runnable = () -> {
            if (id == playerNumber) {
                plugin.getPlayer(id).sendTitle(ChatColor.translateAlternateColorCodes('&', "&4&lBATTLESHIPS"), ChatColor.translateAlternateColorCodes('&', "&7It is &cyour &7turn to make a move!"), 5, 40, 5);
            }
        };

        Bukkit.getScheduler().runTask(plugin, runnable);

    }

    @Override
    protected void onMiss(int x, int y) {
        game.updateGrid(x, y, id, id, GridState.MISS);
        game.updateBlock(x, y, this);
    }

    @Override
    protected void onHit(int x, int y, int playerNumber) {
        String player = plugin.getPlayer(playerNumber) != null ? plugin.getPlayer(playerNumber).getName() : "" + playerNumber;

        plugin.getPlayer(id).sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&4&lBATTLESHIPS&8] &4" + player + " &chas been hit!"));

        game.updateGrid(x, y, id, playerNumber, GridState.ENEMY);
        game.updateBlock(x, y, this);
    }

    @Override
    protected void onHello(int PlayerNumber) {
        this.id = PlayerNumber;
    }

    @Override
    public void ping() {
        try {
            super.ping();
        } catch (NullPointerException e) {
            serverIsOnline = false;
        }
    }

    @Override
    protected void onPong() {
        serverIsOnline = true;
    }

    public boolean serverIsOnline() {
        return serverIsOnline;
    }

    public int getId() {
        return id;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

}
