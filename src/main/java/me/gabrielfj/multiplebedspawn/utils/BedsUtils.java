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
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class BedsUtils{
    static MultipleBedSpawn plugin = MultipleBedSpawn.getInstance();
    public static void removePlayerBed(String bedUUID, Player p){
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        // checks to see if player has beds
        if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
            PlayerBedsData playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
            HashMap<String, BedData> beds = playerBedsData.getPlayerBedData();
            if (beds.containsKey(bedUUID)){
                BedData bedData = beds.get(bedUUID);
                playerBedsData.removeBed(bedUUID);
                playerData.set(new NamespacedKey(plugin, "beds"), new BedsDataType(), playerBedsData);

                World world = Bukkit.getWorld(bedData.getBedWorld());
                String loc[] = bedData.getBedCoords().split(":");
                Location locBed = new Location(world, Double.parseDouble(loc[0]), Double.parseDouble(loc[1]),Double.parseDouble(loc[2]));
                Block bed = world.getBlockAt(locBed);
                if (bed.getBlockData() instanceof Bed bedPart){
                    // since the data is in the head we need to set the Block bed to its head
                    if (bedPart.getPart().toString()=="FOOT"){
                        bed = (Block) bed.getRelative(bedPart.getFacing());
                    }
                }
                BlockState blockState = bed.getState();
                if (blockState instanceof TileState tileState){
                    PersistentDataContainer container = tileState.getPersistentDataContainer();
                    container.remove(new NamespacedKey(plugin, "uuid"));
                    tileState.update();
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

            removePlayerBed(bedUUID, p);
            return false;

        }else{

            BlockState blockState = bed.getState();
            if (blockState instanceof TileState tileState){
                PersistentDataContainer container = tileState.getPersistentDataContainer();
                String uuid = container.get(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING);

                if (container==null || uuid==null || !uuid.equalsIgnoreCase(bedUUID)){
                    removePlayerBed(bedUUID, p);
                    return false;
                }

            }

        }

        return true;
    }

    public static Block checkIfIsBed(Block block){
        if (block!= null && block.getBlockData() instanceof Bed bedPart){
            // since the data is in the head we need to set the Block bed to its head
            if (bedPart.getPart().toString()=="FOOT"){
                block = block.getRelative(bedPart.getFacing());
            }
            return block;
        }
        return null;
    }

    public static int getMaxNumberOfBeds(Player player){
        int maxBeds = plugin.getConfig().getInt("max-beds");
        int maxBedsByPerms = 0;
        if (player.hasPermission("multiplebedspawn.maxcount")){
            for (PermissionAttachmentInfo perm : player.getEffectivePermissions()){
                String permName = perm.getPermission();
                if (permName.contains("multiplebedspawn.maxcount.") && perm.getValue()){
                    String maxCount = (permName.split("multiplebedspawn.maxcount."))[1].trim();
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
