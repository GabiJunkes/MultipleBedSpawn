package me.gabij.multiplebedspawn.listeners;

import me.gabij.multiplebedspawn.MultipleBedSpawn;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static me.gabij.multiplebedspawn.utils.PlayerUtils.stringToLocation;
import static me.gabij.multiplebedspawn.utils.PlayerUtils.undoPropPlayer;

public class PlayerJoinListener implements Listener {
    MultipleBedSpawn plugin;

    public PlayerJoinListener(MultipleBedSpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        NamespacedKey spawnLocName = new NamespacedKey(plugin, "spawnLoc");
        if (plugin.getConfig().getBoolean("spawn-on-sky") && playerData.has(spawnLocName, PersistentDataType.STRING)) {
            Location location = stringToLocation(playerData.get(spawnLocName, PersistentDataType.STRING));
            playerData.remove(spawnLocName);
            p.teleport(location);
        }
        undoPropPlayer(p);
    }
}
