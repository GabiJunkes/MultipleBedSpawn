package me.gabrielfj.multiplebedspawn.listeners;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import me.gabrielfj.multiplebedspawn.models.BedData;
import me.gabrielfj.multiplebedspawn.models.BedsDataType;
import me.gabrielfj.multiplebedspawn.models.PlayerBedsData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;

import static me.gabrielfj.multiplebedspawn.listeners.RespawnMenuHandler.openRespawnMenu;
import static me.gabrielfj.multiplebedspawn.utils.BedsUtils.checksIfBedExists;
import static me.gabrielfj.multiplebedspawn.utils.PlayerUtils.locationToString;

public class PlayerRespawnListener implements Listener {
    static MultipleBedSpawn plugin;

    public PlayerRespawnListener(MultipleBedSpawn plugin) {
        PlayerRespawnListener.plugin = plugin;
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){

        Player p = e.getPlayer();
        String world = p.getWorld().getName();
        List<String> denylist = plugin.getConfig().getStringList("denylist");
        List<String> allowlist = plugin.getConfig().getStringList("allowlist");
        boolean passLists = (!denylist.contains(world)) && (allowlist.contains(world) || allowlist.isEmpty());
        if (passLists) {
            PersistentDataContainer playerData = p.getPersistentDataContainer();
            PlayerBedsData playerBedsData;
            HashMap<String, BedData> beds;
            if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
                playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
                if (playerBedsData != null && playerBedsData.getPlayerBedData() != null) {
                    beds = playerBedsData.getPlayerBedData();
                    if (!plugin.getConfig().getBoolean("link-worlds")) {
                        HashMap<String, BedData> bedsT = (HashMap<String, BedData>) beds.clone();
                        beds.forEach((uuid, bed) -> {
                            if (!bed.getBedWorld().equalsIgnoreCase(world)) {
                                bedsT.remove(uuid);
                            }
                        });
                        beds = bedsT;
                    }
                    beds.forEach((uuid, bed) -> { // loops all beds to check if they still exist
                        String loc[] = bed.getBedCoords().split(":");
                        Location locBed = new Location(Bukkit.getWorld(bed.getBedWorld()), Double.parseDouble(loc[0]), Double.parseDouble(loc[1]),Double.parseDouble(loc[2]));
                        checksIfBedExists(locBed, p, uuid, bed.getBedWorld());
                    });

                }
            }
            if (plugin.getConfig().getBoolean("spawn-on-sky")) {
                Location respawnLoc = e.getRespawnLocation().clone();
                playerData.set(new NamespacedKey(plugin, "spawnLoc"), PersistentDataType.STRING, locationToString(respawnLoc));
                respawnLoc.setY(300);
                e.setRespawnLocation(respawnLoc);
            }
            openRespawnMenu(p);
        }
    }
}
