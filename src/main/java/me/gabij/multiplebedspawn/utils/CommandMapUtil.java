package me.gabij.multiplebedspawn.utils;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;

// Code from https://www.spigotmc.org/threads/simple-tutorial-on-commandmap.623116/
public class CommandMapUtil {
    private static CommandMap commandMap = null;

    /**
     * This methods allows to use the CommandMap on different forks!
     *
     * @return the CommandMap
     * @throws NoSuchFieldException   if field isn't found
     * @throws IllegalAccessException if access is invalid
     */
    public static CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        if (commandMap == null) {
            final Class<? extends Server> serverClass = Bukkit.getServer().getClass();
            final Field commandMapField = serverClass.getDeclaredField("commandMap");
            commandMapField.setAccessible(true); // without this method is private & unaccessible!!!
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        }
        return commandMap;
    }
}
