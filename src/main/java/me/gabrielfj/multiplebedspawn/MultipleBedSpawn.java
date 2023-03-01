package me.gabrielfj.multiplebedspawn;

import me.gabrielfj.multiplebedspawn.commands.NameCommand;
import me.gabrielfj.multiplebedspawn.commands.RemoveCommand;
import me.gabrielfj.multiplebedspawn.listeners.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;


public final class MultipleBedSpawn extends JavaPlugin {

    private File customConfigFile;
    private FileConfiguration customConfig;
    private Configuration messages;

    private static MultipleBedSpawn instance;
    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults();
        createLanguageConfig();
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new RespawnMenuHandler(this), this);
        getServer().getPluginManager().registerEvents(new RemoveMenuHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerGetsOnBedListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getCommand("renamebed").setExecutor(new NameCommand(this));
        getCommand("removebed").setExecutor(new RemoveCommand(this));
    }

    public static MultipleBedSpawn getInstance() { return instance; }

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