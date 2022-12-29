package me.gabrielfj.multiplebedspawn.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.Serializable;

public class BedData implements Serializable {

    private String bedName;
    private Material bedMaterial;
    private String bedCoords;
    private String bedSpawnCoords;
    private long bedCooldown = 0;

    public BedData(Block bed, Player p){
        this.bedMaterial = bed.getType();
        this.bedCoords = locationToString(bed.getLocation());
        this.bedSpawnCoords = locationToString(p.getLocation());
    }

    public String getBedName() {
        return bedName;
    }

    public void setBedName(String bedName) {
        this.bedName = bedName;
    }

    private String locationToString(Location loc){
        return loc.getX()+":"+loc.getY()+":"+loc.getZ();
    }

    public Material getBedMaterial() {
        return bedMaterial;
    }

    public String getBedCoords() {
        return bedCoords;
    }

    public String getBedSpawnCoords() {
        return bedSpawnCoords;
    }

    public long getBedCooldown(){
        return bedCooldown;
    }

    public void setBedCooldown(long cooldown){
        this.bedCooldown = cooldown;
    }

}
