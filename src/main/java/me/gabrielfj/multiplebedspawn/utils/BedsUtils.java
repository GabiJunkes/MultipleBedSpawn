package me.gabrielfj.multiplebedspawn.utils;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import me.gabrielfj.multiplebedspawn.models.BedData;
import me.gabrielfj.multiplebedspawn.models.BedsDataType;
import me.gabrielfj.multiplebedspawn.models.PlayerBedsData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.UUID;

public class BedsUtils{
    static MultipleBedSpawn plugin = MultipleBedSpawn.getInstance();
    public static void removePlayerBed(String bedUUID, String uuid){

        Player p = Bukkit.getPlayer(UUID.fromString(uuid));

        // checks if player object exists
        if (p != null) {

            PersistentDataContainer playerData = p.getPersistentDataContainer();
            // checks to see if player has beds
            if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
                PlayerBedsData playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
                HashMap<String, BedData> beds = playerBedsData.getPlayerBedData();
                if (beds.containsKey(bedUUID)){
                    playerBedsData.removeBed(bedUUID);
                    if (beds==null){
                        playerData.remove(new NamespacedKey(plugin, "beds"));
                    }else{
                        playerData.set(new NamespacedKey(plugin, "beds"), new BedsDataType(), playerBedsData);
                    }
                }
            }

        }
    }
    public static boolean checksIfBedExists(Location locBed, Player p, String bedUUID, String worldString){
        World world = Bukkit.getWorld(worldString);
        Block bed = world.getBlockAt(locBed);
        boolean isBed = false;
        if (bed.getBlockData() instanceof Bed bedPart){
            // since the data is in the head we need to set the Block bed to its head
            if (bedPart.getPart().toString()=="FOOT"){
                bed = (Block) bed.getRelative(bedPart.getFacing());
            }
            isBed = true;
        }

        if (!isBed){

            removePlayerBed(bedUUID, p.getUniqueId().toString());
            return false;

        }else{

            BlockState blockState = bed.getState();
            if (blockState instanceof TileState tileState){
                PersistentDataContainer container = tileState.getPersistentDataContainer();
                String uuid = container.get(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING);

                if (container==null || uuid==null || !uuid.equalsIgnoreCase(bedUUID)){
                    removePlayerBed(bedUUID, p.getUniqueId().toString());
                    return false;
                }

            }

        }

        return true;
    }

    public static int getMaxNumberOfBeds(Player player){
        int maxBeds = plugin.getConfig().getInt("max-beds");
        int maxBedsByPerms = 0;
        if (player.hasPermission("multiplebedspawn.maxcount")){
            player.chat("tem");
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions()){
                String permName = perm.getPermission();
                if (permName.contains("multiplebedspawn.maxcount.") && perm.getValue()){
                    String maxCount = (permName.split("multiplebedspawn.maxcount."))[1].trim();
                    player.chat(maxCount);
                    try{
                        int max = Integer.parseInt(maxCount);
                        if (max>53) {
                            plugin.getLogger().warning("Permission "+permName+" is invalid! Should be lower than 53. Value defaulted to 53, please remove this permission. Warning triggered by player "+player.getName());
                            max = 53;
                        }
                        if (max>maxBedsByPerms){
                            maxBedsByPerms = max;
                        }
                    }catch (Exception err){
                        plugin.getLogger().warning("Permission "+permName+" is invalid! Should be a number after 'maxcount.'. Warning triggered by player "+player.getName());
                    }
                }
            }
        }
        if (maxBeds>53) {
            plugin.getLogger().warning("Max bed count cant be over 53! Value defaulted to 53.");
            plugin.getConfig().set("max-beds", 53);
            plugin.saveConfig();
            maxBeds = 53;
        }
        if (maxBedsByPerms>0){
            maxBeds = maxBedsByPerms;
        }
        return maxBeds;
    }
}
