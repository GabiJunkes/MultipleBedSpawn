package me.gabrielfj.multiplebedspawn.commands;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import me.gabrielfj.multiplebedspawn.models.BedData;
import me.gabrielfj.multiplebedspawn.models.BedsDataType;
import me.gabrielfj.multiplebedspawn.models.PlayerBedsData;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;


public class NameCommand implements CommandExecutor {
    static MultipleBedSpawn plugin;
    public NameCommand(MultipleBedSpawn plugin) {
        this.plugin = plugin;
    }
    private Block checkIfIsBed(Block block){
        if (block!= null && block.getBlockData() instanceof Bed bedPart){
            // since the data is in the head we need to set the Block bed to its head
            if (bedPart.getPart().toString()=="FOOT"){
                block = block.getRelative(bedPart.getFacing());
            }
            return block;
        }
        return null;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player){
            String name = "";
            for (String arg : args){
                name += arg+" ";
            }
            Player p = (Player) sender;
            Block bed = checkIfIsBed(p.getTargetBlockExact(4));
            if (bed!=null) {
                BlockState blockState = bed.getState();
                String bedUUID = null;
                if (blockState instanceof TileState tileState){ // sets a randomUUID to the bed if the bed doesnt have it or get the bed uuid
                    PersistentDataContainer container = tileState.getPersistentDataContainer();

                    if (container.has(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING)){
                        bedUUID = container.get(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING);
                    }

                    tileState.update();

                }

                if (bedUUID==null){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-not-registered-message")));
                    return false;
                }

                PlayerBedsData playerBedsData = null;
                PersistentDataContainer playerData = p.getPersistentDataContainer();

                if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
                    playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
                    if (playerBedsData!=null && playerBedsData.getPlayerBedData()!=null && playerBedsData.hasBed(bedUUID)){
                        BedData bedData = playerBedsData.getPlayerBedData().get(bedUUID);
                        bedData.setBedName(name);
                        playerData.set(new NamespacedKey(plugin, "beds"), new BedsDataType(), playerBedsData);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-name-registered-successfully-message")));
                    }else{
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-not-registered-message")));
                        return false;
                    }
                }
            }else{
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-not-found-message")));
                return false;
            }

        }
        return true;
    }
}
