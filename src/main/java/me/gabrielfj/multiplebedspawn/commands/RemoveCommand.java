package me.gabrielfj.multiplebedspawn.commands;

import me.gabrielfj.multiplebedspawn.MultipleBedSpawn;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import static me.gabrielfj.multiplebedspawn.listeners.RemoveMenuHandler.openRemoveMenu;

import java.lang.module.ModuleDescriptor.Opens;
import java.util.ArrayList;

public class RemoveCommand extends BukkitCommand {
    static MultipleBedSpawn plugin;
    
    public RemoveCommand(MultipleBedSpawn plugin, String name) {
        super(name);
        RemoveCommand.plugin = plugin;
        this.description = "Opens a menu to remove saved beds";
        this.usageMessage = "/removebed";
        this.setAliases(new ArrayList<String>());
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            openRemoveMenu(p);
        }
        return true;
    }
}
