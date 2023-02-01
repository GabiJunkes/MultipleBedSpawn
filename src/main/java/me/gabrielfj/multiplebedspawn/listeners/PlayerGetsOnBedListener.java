package me.gabrielfj.multiplebedspawn.listeners;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import me.gabrielfj.multiplebedspawn.models.BedData;
import me.gabrielfj.multiplebedspawn.models.BedsDataType;
import me.gabrielfj.multiplebedspawn.models.PlayerBedsData;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerGetsOnBedListener implements Listener {

    MultipleBedSpawn plugin;

    public PlayerGetsOnBedListener(MultipleBedSpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerGetOnBed(PlayerBedEnterEvent e){

        Player player = e.getPlayer();
        String world = player.getWorld().getName();
        List<String> denylist = plugin.getConfig().getStringList("denylist");
        List<String> allowlist = plugin.getConfig().getStringList("allowlist");
        boolean passLists = (!denylist.contains(world)) && (allowlist.contains(world) || allowlist.isEmpty());

        if (passLists) {
            Block bed = e.getBed();
            PersistentDataContainer playerData = player.getPersistentDataContainer();

            int maxBeds = plugin.getConfig().getInt("max-beds");
            int playerBedsCount = 0;
            PlayerBedsData playerBedsData = null;
            if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
                playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
                if (playerBedsData != null && playerBedsData.getPlayerBedData() != null) {
                    HashMap<String, BedData> beds = playerBedsData.getPlayerBedData();
                    if (!plugin.getConfig().getBoolean("link-worlds")) {
                        HashMap<String, BedData> bedsT = (HashMap<String, BedData>) beds.clone();
                        beds.forEach((uuid, bedData) -> {
                            // clear lists so beds are only from the world that player is in
                            if (!bedData.getBedWorld().equalsIgnoreCase(player.getWorld().getName())) {
                                bedsT.remove(uuid);
                            }
                        });
                        beds = bedsT;
                    }
                    playerBedsCount = beds.size();
                }
            }

            if (playerBedsCount < maxBeds) {

                UUID randomUUID = UUID.randomUUID();
                BlockState blockState = bed.getState();
                if (blockState instanceof TileState tileState) { // sets a randomUUID to the bed if the bed doesnt have it or get the bed uuid
                    PersistentDataContainer container = tileState.getPersistentDataContainer();

                    if (!container.has(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING)) {
                        container.set(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING, "" + randomUUID);
                    } else {
                        randomUUID = UUID.fromString(container.get(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING));
                    }

                    tileState.update();

                }

                boolean registerBed = false;
                if (playerBedsData == null) { // if the player doesnt have any bed

                    playerBedsData = new PlayerBedsData(player, bed, randomUUID.toString());
                    registerBed = true;

                } else if (!playerBedsData.hasBed(randomUUID.toString())) {

                    playerBedsData.setNewBed(player, bed, randomUUID.toString());
                    registerBed = true;

                }

                if (registerBed) {
                    playerData.set(new NamespacedKey(plugin, "beds"), new BedsDataType(), playerBedsData);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("bed-registered-successfully-message")));
                }


            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages("max-beds-message")));
            }

            e.setCancelled(plugin.getConfig().getBoolean("disable-sleeping"));
        }
    }


}
