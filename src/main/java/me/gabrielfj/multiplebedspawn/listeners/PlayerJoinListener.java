package me.gabrielfj.multiplebedspawn.listeners;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static me.gabrielfj.multiplebedspawn.utils.PlayerUtils.undoPropPlayer;

public class PlayerJoinListener implements Listener {
    MultipleBedSpawn plugin;

    public PlayerJoinListener(MultipleBedSpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        Player p = e.getPlayer();
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        if (plugin.getConfig().getBoolean("spawn-on-sky") && playerData.has(new NamespacedKey(plugin, "spawnLoc"), PersistentDataType.STRING)) {
            undoPropPlayer(p);
            String spawnCoords[] = playerData.get(new NamespacedKey(plugin, "spawnLoc"), PersistentDataType.STRING).split(":");
            Location location = new Location(p.getWorld(), Double.parseDouble(spawnCoords[0]), Double.parseDouble(spawnCoords[1]), Double.parseDouble(spawnCoords[2]));
            playerData.remove(new NamespacedKey(plugin, "spawnLoc"));
            p.teleport(location);
        }else if(!p.getCanPickupItems()){
            undoPropPlayer(p);
        }
    }
}
