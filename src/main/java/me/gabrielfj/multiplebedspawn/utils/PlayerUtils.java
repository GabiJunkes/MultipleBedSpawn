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

    public static String locationToString(Location loc){
        return loc.getX()+":"+loc.getY()+":"+loc.getZ();
    }

    public static void setPropPlayer(Player p){

        PersistentDataContainer playerData = p.getPersistentDataContainer();
        p.setInvulnerable(true);
        p.setInvisible(true);
        p.setCanPickupItems(false);
        if (plugin.getConfig().getBoolean("spawn-on-sky")) {
            int playerAllowFly = (p.getAllowFlight()) ? 1 : 0;
            playerData.set(new NamespacedKey(plugin, "allowFly"), PersistentDataType.INTEGER, playerAllowFly);
            p.setAllowFlight(true);
            p.setFlying(true);
        }
        playerData.set(new NamespacedKey(plugin, "lastWalkspeed"), PersistentDataType.FLOAT, p.getWalkSpeed());
        p.setWalkSpeed(0);

    }

    public static void undoPropPlayer(Player p){

        PersistentDataContainer playerData = p.getPersistentDataContainer();
        p.setInvisible(false);
        p.setInvulnerable(false);
        p.setCanPickupItems(true);
        if (playerData.has(new NamespacedKey(plugin, "lastWalkspeed"), PersistentDataType.FLOAT)){
            p.setWalkSpeed(playerData.get(new NamespacedKey(plugin, "lastWalkspeed"), PersistentDataType.FLOAT));
            playerData.remove(new NamespacedKey(plugin, "lastWalkspeed"));
        }else {
            p.setWalkSpeed(0.2F);
        }
        if (p.getWalkSpeed()==0.0){
            p.setWalkSpeed(0.2F);
        }

        if (plugin.getConfig().getBoolean("spawn-on-sky")) {
            boolean playerAllowFly = (playerData.get(new NamespacedKey(plugin, "allowFly"), PersistentDataType.INTEGER) == 1) ? true : false;
            playerData.remove(new NamespacedKey(plugin, "allowFly"));
            p.setAllowFlight(playerAllowFly);
            p.setFlying(false);
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
            undoPropPlayer(p);
            String loc[] = beds.get(uuid).getBedSpawnCoords().split(":");
            World world = Bukkit.getWorld(beds.get(uuid).getBedWorld());
            Location locSpawn = new Location(world, Double.parseDouble(loc[0]), Double.parseDouble(loc[1]),Double.parseDouble(loc[2]));
            if (!p.hasPermission("multiplebedspawn.skipcooldown")) {
                beds.get(uuid).setBedCooldown(System.currentTimeMillis() + (plugin.getConfig().getLong("bed-cooldown") * 1000));
            }
            playerData.set(new NamespacedKey(plugin, "beds"), new BedsDataType(), playerBedsData);
            p.teleport(locSpawn);
        }
    }

}
