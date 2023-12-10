package me.gabrielfj.multiplebedspawn.commands;

import static me.gabrielfj.multiplebedspawn.utils.BedsUtils.checkIfIsBed;
import static me.gabrielfj.multiplebedspawn.utils.BedsUtils.removePlayerBed;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import me.gabrielfj.multiplebedspawn.models.BedData;
import me.gabrielfj.multiplebedspawn.models.BedsDataType;
import me.gabrielfj.multiplebedspawn.models.PlayerBedsData;

public class ShareCommand extends BukkitCommand {
    static MultipleBedSpawn plugin;
    
    public ShareCommand(MultipleBedSpawn plugin, String name) {
        super(name);
        ShareCommand.plugin = plugin;
        this.description = "Gives bed to another player";
        this.usageMessage = "/sharebed <player>";
        this.setAliases(new ArrayList<String>());
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (args.length!=1) {
            return false;
        }
        if (sender instanceof Player) {
            Player ownerPlayer = (Player) sender;
            Player receiverPlayer = Bukkit.getPlayer(args[0]);
            if (receiverPlayer==null){
                return false;
            }
            if (receiverPlayer==ownerPlayer){
                return false;
            }
            Block bed = checkIfIsBed(ownerPlayer.getTargetBlockExact(4));
            if (bed!=null) {
                BlockState blockState = bed.getState();
                String bedUUID = null;
                if (blockState instanceof TileState tileState){ // gets the bed uuid
                    PersistentDataContainer container = tileState.getPersistentDataContainer();
                    if (container.has(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING)){
                        bedUUID = container.get(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING);
                    }
                }

                if (bedUUID==null){
                    ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-not-registered-message")));
                    return false;
                }

                PlayerBedsData playerBedsData = null;
                PersistentDataContainer playerData = ownerPlayer.getPersistentDataContainer();

                if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
                    playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
                    if (playerBedsData!=null && playerBedsData.getPlayerBedData()!=null && playerBedsData.hasBed(bedUUID)){
                        PlayerBedsData receiverBedsData;
                        PersistentDataContainer receiverData = receiverPlayer.getPersistentDataContainer();
                        if (receiverData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
                            receiverBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
                            receiverBedsData.setNewBed(receiverPlayer, bed, bedUUID);
                        }else{
                            receiverBedsData = new PlayerBedsData(receiverPlayer, bed, bedUUID.toString());
                        }
                        receiverData.set(new NamespacedKey(plugin, "beds"), new BedsDataType(), receiverBedsData);
                        receiverPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-registered-successfully-message")));
                        removePlayerBed(bedUUID, ownerPlayer);
                    }else{
                        ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-not-registered-message")));
                        return false;
                    }
                }
            }else{
                ownerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-not-found-message")));
                return false;
            }

        }
        return true;
    }
}
