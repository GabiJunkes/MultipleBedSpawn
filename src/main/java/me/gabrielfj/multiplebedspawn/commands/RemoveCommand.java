package me.gabrielfj.multiplebedspawn.commands;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.gabrielfj.multiplebedspawn.listeners.RemoveMenuHandler.openRemoveMenu;

public class RemoveCommand implements CommandExecutor {
    static MultipleBedSpawn plugin;
    public RemoveCommand(MultipleBedSpawn plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (plugin.getConfig().getBoolean("remove-beds-gui")){
                openRemoveMenu(p);
            }else{
                p.sendMessage(ChatColor.RED+plugin.getMessages("command-not-enabled"));
                return false;
            }
        }
        return true;
    }
}
