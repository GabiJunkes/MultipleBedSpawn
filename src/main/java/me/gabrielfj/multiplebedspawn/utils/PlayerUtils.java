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
import java.util.concurrent.atomic.AtomicInteger;

import static me.gabrielfj.multiplebedspawn.utils.BedsUtils.checksIfBedExists;


public class PlayerUtils {

    static MultipleBedSpawn plugin = MultipleBedSpawn.getInstance();

    public static String locationToString(Location loc){
        return loc.getWorld().getName()+":"+loc.getX()+":"+loc.getY()+":"+loc.getZ();
    }

    public static Location stringToLocation(String locString){
        String[] loc = locString.split(":");
        return new Location(Bukkit.getWorld(loc[0]), Double.parseDouble(loc[1]), Double.parseDouble(loc[2]), Double.parseDouble(loc[3]));
    }

    public static void setPropPlayer(Player p){

        PersistentDataContainer playerData = p.getPersistentDataContainer();
        if (!playerData.has(new NamespacedKey(plugin, "hasProp"), PersistentDataType.BOOLEAN)) {
            p.setInvulnerable(true);

            playerData.set(new NamespacedKey(plugin, "isInvisible"), PersistentDataType.BOOLEAN, p.isInvisible());
            p.setInvisible(true);

            playerData.set(new NamespacedKey(plugin, "canPickupItems"), PersistentDataType.BOOLEAN, p.getCanPickupItems());
            p.setCanPickupItems(false);

            if (plugin.getConfig().getBoolean("spawn-on-sky")) {
                playerData.set(new NamespacedKey(plugin, "allowFly"), PersistentDataType.BOOLEAN, p.getAllowFlight());
                p.setAllowFlight(true);
                p.setFlying(true);
            }
            playerData.set(new NamespacedKey(plugin, "lastWalkspeed"), PersistentDataType.FLOAT, p.getWalkSpeed());
            p.setWalkSpeed(0);

            playerData.set(new NamespacedKey(plugin, "hasProp"), PersistentDataType.BOOLEAN, true);
        }
    }

    public static void undoPropPlayer(Player p){

        PersistentDataContainer playerData = p.getPersistentDataContainer();
        if (playerData.has(new NamespacedKey(plugin, "hasProp"), PersistentDataType.BOOLEAN)) {

            p.setInvulnerable(false);
            p.setInvisible(playerData.get(new NamespacedKey(plugin, "isInvisible"), PersistentDataType.BOOLEAN));
            p.setCanPickupItems(playerData.get(new NamespacedKey(plugin, "canPickupItems"), PersistentDataType.BOOLEAN));

            playerData.remove(new NamespacedKey(plugin, "isInvisible"));
            playerData.remove(new NamespacedKey(plugin, "canPickupItems"));

            p.setWalkSpeed(playerData.get(new NamespacedKey(plugin, "lastWalkspeed"), PersistentDataType.FLOAT));
            playerData.remove(new NamespacedKey(plugin, "lastWalkspeed"));


            if (plugin.getConfig().getBoolean("spawn-on-sky")) {
                p.setAllowFlight(playerData.get(new NamespacedKey(plugin, "allowFly"), PersistentDataType.BOOLEAN));
                p.setFlying(false);

                playerData.remove(new NamespacedKey(plugin, "allowFly"));
                playerData.remove(new NamespacedKey(plugin, "isFlying"));
            }

            playerData.remove(new NamespacedKey(plugin, "hasProp"));

            p.closeInventory();
        }

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
            playerData.remove(new NamespacedKey(plugin, "spawnLoc"));
            p.teleport(locSpawn);
        }
    }

    public static Location getPlayerRespawnLoc(Player p){
        Location loc = p.getLocation();
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        if (playerData.has(new NamespacedKey(plugin, "spawnLoc"), PersistentDataType.STRING)){
            Location playerRespawnLocation = stringToLocation(playerData.get(new NamespacedKey(plugin, "spawnLoc"), PersistentDataType.STRING));
            if (playerRespawnLocation!=null){
                loc = playerRespawnLocation;
            }
        }
        return loc;
    }

    public static Integer getPlayerBedsCount(Player p){
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        PlayerBedsData playerBedsData = null;
        AtomicInteger playerBedsCount = new AtomicInteger();
        playerBedsCount.set(0);
        if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
            playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
            if (playerBedsData != null && playerBedsData.getPlayerBedData() != null) {
                HashMap<String, BedData> beds = playerBedsData.getPlayerBedData();
                World world = getPlayerRespawnLoc(p).getWorld();
                String worldName = world.getName();
                if (!plugin.getConfig().getBoolean("link-worlds")) {
                    HashMap<String, BedData> bedsT = (HashMap<String, BedData>) beds.clone();
                    beds.forEach((uuid, bedData) -> {
                        // clear lists so beds are only from the world that player will respawn
                        if (!bedData.getBedWorld().equalsIgnoreCase(worldName)) {
                            bedsT.remove(uuid);
                        }
                    });
                    beds = bedsT;
                }
                playerBedsCount.set(beds.size());
                beds.forEach((uuid, bedData) -> {
                    String[] location = bedData.getBedCoords().split(":");
                    String bedWorld = bedData.getBedWorld();
                    Location bedLoc = new Location(Bukkit.getWorld(bedWorld), Double.parseDouble(location[0]), Double.parseDouble(location[1]), Double.parseDouble(location[2]));
                    if(!checksIfBedExists(bedLoc, p, uuid)){
                        playerBedsCount.addAndGet(-1);
                    }
                });

            }
        }
        return playerBedsCount.get();
    }

}
