package me.gabrielfj.multiplebedspawn.utils;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import me.gabrielfj.multiplebedspawn.models.BedData;
import me.gabrielfj.multiplebedspawn.models.BedsDataType;
import me.gabrielfj.multiplebedspawn.models.PlayerBedsData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;


public class PlayerUtils {

    static MultipleBedSpawn plugin = MultipleBedSpawn.getInstance();
    public static void setPropPlayer(Player p){

        p.setInvisible(false);
        p.setInvulnerable(false);
        p.setCanPickupItems(true);
        if (p.getPersistentDataContainer().has(new NamespacedKey(plugin, "lastWalkspeed"), PersistentDataType.FLOAT)){
            p.setWalkSpeed(p.getPersistentDataContainer().get(new NamespacedKey(plugin, "lastWalkspeed"), PersistentDataType.FLOAT));
            p.getPersistentDataContainer().remove(new NamespacedKey(plugin, "lastWalkspeed"));
        }else {
            p.setWalkSpeed(0.2F);
        }
        if (p.getWalkSpeed()==0.0){
            p.setWalkSpeed(0.2F);
        }
        p.closeInventory();

    }

    public static void teleportPlayer(Player p, PersistentDataContainer data, PersistentDataContainer playerData, PlayerBedsData playerBedsData, String uuid){
        boolean isOkayToTP = true;

        if (data.has(new NamespacedKey(plugin, "cooldown"), PersistentDataType.LONG) && data.has(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING)){

            long cooldown = data.get(new NamespacedKey(plugin, "cooldown"), PersistentDataType.LONG);
            if (cooldown>System.currentTimeMillis()){
                isOkayToTP = false;
            }

        }

        if (isOkayToTP) {
            HashMap<String, BedData> beds = playerBedsData.getPlayerBedData();
            setPropPlayer(p);
            String loc[] = beds.get(uuid).getBedSpawnCoords().split(":");
            World world = Bukkit.getWorld(beds.get(uuid).getBedWorld());
            Location locSpawn = new Location(world, Double.parseDouble(loc[0]), Double.parseDouble(loc[1]),Double.parseDouble(loc[2]));
            beds.get(uuid).setBedCooldown( System.currentTimeMillis() + (plugin.getConfig().getLong("bed-cooldown") * 1000) );
            playerData.set(new NamespacedKey(plugin, "beds"), new BedsDataType(), playerBedsData);
            p.teleport(locSpawn);
        }
    }

}
