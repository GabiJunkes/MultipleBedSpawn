package me.gabrielfj.multiplebedspawn.listeners;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import me.gabrielfj.multiplebedspawn.models.BedData;
import me.gabrielfj.multiplebedspawn.models.BedsDataType;
import me.gabrielfj.multiplebedspawn.models.PlayerBedsData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static me.gabrielfj.multiplebedspawn.utils.BedsUtils.checksIfBedExists;
import static me.gabrielfj.multiplebedspawn.utils.PlayerUtils.*;
import static me.gabrielfj.multiplebedspawn.utils.PlayerUtils.undoPropPlayer;

public class RespawnMenuHandler implements Listener {

    static MultipleBedSpawn plugin;

    public RespawnMenuHandler(MultipleBedSpawn plugin) {
        this.plugin = plugin;
    }

    public static void updateItens(Inventory gui, Player p){

        if (gui.getViewers().toString().length()>2){

            ItemStack itens[] = gui.getContents();
            boolean hasActiveCooldown = false;
            for (ItemStack item : itens){

                if (item!=null && item.hasItemMeta()){

                    ItemMeta item_meta = item.getItemMeta();
                    PersistentDataContainer data = item_meta.getPersistentDataContainer();

                    if (data.has(new NamespacedKey(plugin, "cooldown"), PersistentDataType.LONG) && data.has(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING)){

                        long cooldown = data.get(new NamespacedKey(plugin, "cooldown"), PersistentDataType.LONG);
                        String uuid = data.get(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING);
                        List<String> lore = item_meta.getLore();

                        int optionsCount = 2;
                        if (plugin.getConfig().getBoolean("disable-bed-world-desc")) {
                            optionsCount--;
                        }
                        if (plugin.getConfig().getBoolean("disable-bed-coords-desc")) {
                            optionsCount--;
                        }
                        if (cooldown>System.currentTimeMillis()){
                            hasActiveCooldown = true;
                            long sec = ( cooldown - System.currentTimeMillis() ) / 1000;
                            String seconds = Long.toString(sec);
                            if (lore == null){
                                lore = new ArrayList<>();
                            }
                            if (lore.size()>optionsCount) {
                                lore.set(
                                        optionsCount,
                                        ChatColor.GOLD+""+ChatColor.BOLD+plugin.getMessages("cooldown-text").replace("{1}", seconds)
                                );
                            }else {
                                lore.add(
                                        ChatColor.GOLD+""+ChatColor.BOLD+plugin.getMessages("cooldown-text").replace("{1}", seconds)
                                );
                            }
                        }else{
                            if (lore.size()>optionsCount) {
                                lore.remove(optionsCount);
                            }
                        }

                        item_meta.setLore(lore);
                        item.setItemMeta(item_meta);
                    }
                }
            }

            if (hasActiveCooldown){
                Bukkit.getScheduler().runTaskLater(plugin, () -> {updateItens(gui, p);}, 10L);
            }

        }

    }

    public static void openRespawnMenu(Player p){

        // gets how much beds player has to use on for loop and for the if check
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        int playerBedsCount = 0;
        PlayerBedsData playerBedsData = null;
        if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
            playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
            if (playerBedsData != null && playerBedsData.getPlayerBedData() != null) {
                HashMap<String, BedData> beds = playerBedsData.getPlayerBedData();
                if (!plugin.getConfig().getBoolean("link-worlds")) {
                    HashMap<String, BedData> bedsT = (HashMap<String, BedData>) beds.clone();
                    beds.forEach((uuid, bed) -> {
                        // clear lists so beds are only from the world that player is in
                        if (!bed.getBedWorld().equalsIgnoreCase(p.getWorld().getName())) {
                            bedsT.remove(uuid);
                        }
                    });
                    beds = bedsT;
                }
                playerBedsCount = beds.size();
            }
        }

        // if the player doesnt have any beds than dont open menu
        if (playerBedsCount>0){

            // sets stuff to player be invul and invis on spawn
            setPropPlayer(p);

            // create inventory
            int bedCount = playerBedsCount+1;
            Inventory gui = Bukkit.createInventory(p, 9 * ( (int) Math.ceil( bedCount / (Double) 9.0 ) ), ChatColor.translateAlternateColorCodes('&', plugin.getMessages("menu-title")));

            HashMap<String, BedData> beds = playerBedsData.getPlayerBedData();
            if (!plugin.getConfig().getBoolean("link-worlds")) {
                HashMap<String, BedData> bedsT = (HashMap<String, BedData>) beds.clone();
                beds.forEach((uuid, bed) -> {
                    // clear lists so beds are only from the world that player is in
                    if (!bed.getBedWorld().equalsIgnoreCase(p.getWorld().getName())) {
                        bedsT.remove(uuid);
                    }
                });
                beds = bedsT;
            }
            AtomicBoolean hasCooldown = new AtomicBoolean(false);
            AtomicInteger cont= new AtomicInteger(1);
            beds.forEach((uuid, bed) -> {
                ItemStack item = new ItemStack(bed.getBedMaterial(),1);
                ItemMeta item_meta = item.getItemMeta();
                String bedName = plugin.getMessages("default-bed-name").replace("{1}", cont.toString());
                item_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', bedName));
                if (bed.getBedName()!=null) {
                    item_meta.setDisplayName(bed.getBedName());
                }
                PersistentDataContainer data = item_meta.getPersistentDataContainer();

                List<String> lore = new ArrayList<>();
                if (!plugin.getConfig().getBoolean("disable-bed-world-desc")) {
                    lore.add(ChatColor.DARK_PURPLE + bed.getBedWorld().toUpperCase());
                }
                if (!plugin.getConfig().getBoolean("disable-bed-coords-desc")) {
                    String[] location = bed.getBedCoords().split(":");
                    String locText = "X: " + location[0].substring(0, location[0].length() - 2) +
                            " Y: " + location[1].substring(0, location[1].length() - 2) +
                            " Z: " + location[2].substring(0, location[2].length() - 2);
                    lore.add(ChatColor.GRAY + locText);
                }
                // checks if has any cooldowns
                if (bed.getBedCooldown()>0L){

                    long cooldown = bed.getBedCooldown();
                    if (cooldown>System.currentTimeMillis()){ // if cooldown isnt expired
                        hasCooldown.set(true);
                        data.set(new NamespacedKey(plugin, "cooldown"), PersistentDataType.LONG, cooldown);
                    }else{
                        bed.setBedCooldown(0L);
                    }

                }

                data.set(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING, uuid);
                data.set(new NamespacedKey(plugin, "location"), PersistentDataType.STRING, bed.getBedCoords());
                data.set(new NamespacedKey(plugin, "world"), PersistentDataType.STRING, bed.getBedWorld());

                item_meta.setLore(lore);
                item.setItemMeta(item_meta);
                gui.addItem(item);
                cont.getAndIncrement();
            });

            if (hasCooldown.get()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {updateItens(gui, p);}, 10L);
            }

            ItemStack item = new ItemStack(Material.GRASS_BLOCK,1);
            ItemMeta item_meta = item.getItemMeta();
            item_meta.setDisplayName(ChatColor.YELLOW+"SPAWN");
            item.setItemMeta(item_meta);
            gui.setItem(9 * ( (int) Math.ceil( bedCount / (Double) 9.0 )) -1, item);

            // I dont know why but if openInventory is not on a scheduler is does not open
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.openInventory(gui);
            }, 0L);

        }else{

            String spawnCoords[] = playerData.get(new NamespacedKey(plugin, "spawnLoc"), PersistentDataType.STRING).split(":");
            Location location = new Location(p.getWorld(), Double.parseDouble(spawnCoords[0]), Double.parseDouble(spawnCoords[1]), Double.parseDouble(spawnCoords[2]));
            playerData.remove(new NamespacedKey(plugin, "spawnLoc"));
            undoPropPlayer(p);
            boolean test = p.teleport(location);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.teleport(location);
            }, 1L);

        }

    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e){

        if (e.getView().getTitle().equalsIgnoreCase(plugin.getMessages("menu-title"))){
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            if (e.getCurrentItem() != null){
                PersistentDataContainer playerData = p.getPersistentDataContainer();
                int playerBedsCount = 0;
                PlayerBedsData playerBedsData = null;
                if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
                    playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
                    if (playerBedsData!=null && playerBedsData.getPlayerBedData()!=null){
                        playerBedsCount = playerBedsData.getPlayerBedData().size();
                    }
                }
                double bedCount = playerBedsCount + 1;
                int index = e.getSlot();
                if (e.getCurrentItem().getType().toString().toLowerCase().contains("bed")){

                    ItemMeta item_meta = e.getCurrentItem().getItemMeta();
                    PersistentDataContainer data = item_meta.getPersistentDataContainer();

                    String bedCoord[] = data.get(new NamespacedKey(plugin, "location"), PersistentDataType.STRING).split(":");
                    String world = data.get(new NamespacedKey(plugin, "world"), PersistentDataType.STRING);
                    Location location = new Location(p.getWorld(), Double.parseDouble(bedCoord[0]), Double.parseDouble(bedCoord[1]), Double.parseDouble(bedCoord[2]));
                    String uuid = data.get(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING);

                    if (checksIfBedExists(location , p, uuid, world)){

                        teleportPlayer(p, data, playerData, playerBedsData, uuid);

                    }else{
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            p.closeInventory();
                        }, 0L);
                    }


                }else if(index==9 * ( (int) Math.ceil( bedCount / (Double) 9.0 ) )-1){
                    if (plugin.getConfig().getBoolean("spawn-on-sky") && playerData.has(new NamespacedKey(plugin, "spawnLoc"), PersistentDataType.STRING)) {
                        String spawnCoords[] = playerData.get(new NamespacedKey(plugin, "spawnLoc"), PersistentDataType.STRING).split(":");
                        Location location = new Location(p.getWorld(), Double.parseDouble(spawnCoords[0]), Double.parseDouble(spawnCoords[1]), Double.parseDouble(spawnCoords[2]));
                        playerData.remove(new NamespacedKey(plugin, "spawnLoc"));
                        undoPropPlayer(p);
                        p.teleport(location);
                    }else{
                        undoPropPlayer(p);
                    }
                }
            }

        }

    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e){

        if (e.getView().getTitle().equalsIgnoreCase(plugin.getMessages("menu-title"))){

            Player p = (Player) e.getPlayer();
            if (!p.getCanPickupItems()){
                openRespawnMenu(p);
            }

        }

    }

}
