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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static me.gabrielfj.multiplebedspawn.utils.BedsUtils.removePlayerBed;
import static me.gabrielfj.multiplebedspawn.utils.PlayerUtils.getPlayerBedsCount;

public class RemoveMenuHandler implements Listener {
    static MultipleBedSpawn plugin;

    public RemoveMenuHandler(MultipleBedSpawn plugin) {
        this.plugin = plugin;
    }

    public static void openRemoveMenu(Player p){

        // gets how much beds player has to use on for loop and for the if check
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        PlayerBedsData playerBedsData = null;

        int playerBedsCount = getPlayerBedsCount(p);

        if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
            playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
        }

        // if the player doesnt have any beds than dont open menu
        if (playerBedsCount>0){

            // create inventory
            int bedCount = playerBedsCount+1;
            Inventory gui = Bukkit.createInventory(p, 9 * ( (int) Math.ceil( bedCount / (Double) 9.0 ) ), ChatColor.translateAlternateColorCodes('&', plugin.getMessages("remove-menu-title")));

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

                data.set(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING, uuid);
                data.set(new NamespacedKey(plugin, "location"), PersistentDataType.STRING, bed.getBedCoords());
                data.set(new NamespacedKey(plugin, "world"), PersistentDataType.STRING, bed.getBedWorld());

                item_meta.setLore(lore);
                item.setItemMeta(item_meta);
                gui.addItem(item);
                cont.getAndIncrement();
            });

            ItemStack item = new ItemStack(Material.BARRIER,1);
            ItemMeta item_meta = item.getItemMeta();
            item_meta.setDisplayName(ChatColor.YELLOW+plugin.getMessages("close-menu"));
            item.setItemMeta(item_meta);
            gui.setItem(9 * ( (int) Math.ceil( bedCount / (Double) 9.0 )) -1, item);

            // I dont know why but if openInventory is not on a scheduler is does not open
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.openInventory(gui);
            }, 0L);

        }

    }

    public static void updateItens(Inventory gui, Player p) {

        if (gui.getViewers().toString().length() > 2) {

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
            if (playerBedsCount > 0) {

                // create inventory
                int bedCount = playerBedsCount + 1;
                gui.clear();
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
                AtomicInteger cont = new AtomicInteger(1);
                beds.forEach((uuid, bed) -> {
                    ItemStack item = new ItemStack(bed.getBedMaterial(), 1);
                    ItemMeta item_meta = item.getItemMeta();
                    String bedName = plugin.getMessages("default-bed-name").replace("{1}", cont.toString());
                    item_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', bedName));
                    if (bed.getBedName() != null) {
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

                    data.set(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING, uuid);
                    data.set(new NamespacedKey(plugin, "location"), PersistentDataType.STRING, bed.getBedCoords());
                    data.set(new NamespacedKey(plugin, "world"), PersistentDataType.STRING, bed.getBedWorld());

                    item_meta.setLore(lore);
                    item.setItemMeta(item_meta);
                    gui.addItem(item);
                    cont.getAndIncrement();
                });

                ItemStack item = new ItemStack(Material.BARRIER, 1);
                ItemMeta item_meta = item.getItemMeta();
                item_meta.setDisplayName(ChatColor.YELLOW + plugin.getMessages("close-menu"));
                item.setItemMeta(item_meta);
                gui.setItem(9 * ((int) Math.ceil(bedCount / (Double) 9.0)) - 1, item);


            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    p.closeInventory();
                }, 0L);
            }

        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.closeInventory();
            }, 0L);
        }
    }
    @EventHandler
    public void onMenuClick(InventoryClickEvent e){

        if (e.getView().getTitle().equalsIgnoreCase(plugin.getMessages("remove-menu-title"))){
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            if (e.getCurrentItem() != null){
                PersistentDataContainer playerData = p.getPersistentDataContainer();
                PlayerBedsData playerBedsData = null;
                if (playerData.has(new NamespacedKey(plugin, "beds"), new BedsDataType())) {
                    playerBedsData = playerData.get(new NamespacedKey(plugin, "beds"), new BedsDataType());
                }


                if (e.getCurrentItem().getType().toString().toLowerCase().contains("bed")){

                    ItemMeta item_meta = e.getCurrentItem().getItemMeta();
                    PersistentDataContainer data = item_meta.getPersistentDataContainer();

                    String uuid = data.get(new NamespacedKey(plugin, "uuid"), PersistentDataType.STRING);
                    removePlayerBed(uuid, p.getUniqueId().toString());
                    updateItens(e.getClickedInventory(), p);

                } else if (e.getCurrentItem().getType().toString().equalsIgnoreCase("BARRIER")) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        p.closeInventory();
                    }, 0L);
                }
            }

        }

    }

}
