package nl.gerwint.plugin;

import nl.gerwint.client.BattleShipsClient;
import nl.gerwint.plugin.models.Game;
import nl.gerwint.plugin.models.GridState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class BattleShipsPluginClient extends BattleShipsClient {

    private BattleShipsPlugin plugin;
    private Game grid;
    private boolean serverIsOnline;
    private int id;
    private Location location;

    public BattleShipsPluginClient(String username, BattleShipsPlugin plugin) {
        super(username);

        this.plugin = plugin;
        grid = plugin.grid;
        serverIsOnline = false;
    }

    @Override
    protected void onPos(int x, int y) {
        grid.updateGrid(x, y, id, GridState.ALLY);
        grid.updateBlock(x, y, this);
    }

    @Override
    protected void onNewGame(int x, int y) {
        Runnable runnable = () -> {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&4&lBATTLESHIPS&8] &cThe game has begun!"));
        };

        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    protected void onTurn(int playerNumber) {

    }

    @Override
    protected void onMiss(int x, int y) {
        grid.updateGrid(x, y, id, GridState.MISS);
        grid.updateBlock(x, y, this);
    }

    @Override
    protected void onHit(int x, int y, int playerNumber) {
        grid.updateGrid(x, y, playerNumber, GridState.ENEMY);
        grid.updateBlock(x, y, this);
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
