package me.gabrielfj.multiplebedspawn.listeners;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static me.gabrielfj.multiplebedspawn.utils.PlayerUtils.undoPropPlayer;

public class PlayerJoinListener implements Listener {
    MultipleBedSpawn plugin;

    public PlayerJoinListener(MultipleBedSpawn plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        Player p = e.getPlayer();
        undoPropPlayer(p);
    }
}
