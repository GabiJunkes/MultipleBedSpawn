package me.gabrielfj.multiplebedspawn;

import me.gabrielfj.multiplebedspawn.commands.NameCommand;
import me.gabrielfj.multiplebedspawn.listeners.MenuHandler;
import me.gabrielfj.multiplebedspawn.listeners.PlayerDeathListener;
import me.gabrielfj.multiplebedspawn.listeners.PlayerGetsOnBedListener;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Logger;

public final class MultipleBedSpawn extends JavaPlugin {

    private File customConfigFile;
    private FileConfiguration customConfig;
    private Configuration messages;
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        createLanguageConfig();
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerGetsOnBedListener(this), this);
        getCommand("multibed").setExecutor(new NameCommand(this));
    }

    // get message of selected language
    public String getMessages(String path) {
        return this.messages.getString(path);
    }

    private void createLanguageConfig() {
        String lang = this.getConfig().getString("lang");
        InputStream input;
        try { // tries getting selected languages
            input = getClass().getClassLoader().getResourceAsStream("languages/{key}.yml".replace("{key}",lang));
            this.messages = YamlConfiguration.loadConfiguration(new InputStreamReader(input));
        }catch (Exception e){ // else sets enUS as default
            input = getClass().getClassLoader().getResourceAsStream("languages/enUS.yml");
            this.messages = YamlConfiguration.loadConfiguration(new InputStreamReader(input));
        }
    }
}