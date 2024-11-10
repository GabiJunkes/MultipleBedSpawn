package me.gabij.multiplebedspawn.models;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.HashMap;

public class PlayerBedsData implements Serializable {
    private static final long serialVersionUID = 6158573570409965948L;

    private HashMap<String, BedData> bedData = new HashMap<String, BedData>();;

    public PlayerBedsData() {
    }

    public PlayerBedsData(Player p, Block bed, String bedUUID) {
        BedData tempBedData = new BedData(bed, p);
        this.bedData.put(bedUUID, tempBedData);
    }

    public void setNewBed(Player p, Block bed, String bedUUID) {
        BedData tempBedData = new BedData(bed, p);
        this.bedData.put(bedUUID, tempBedData);
    }

    public void shareBed(PlayerBedsData receiverPlayerBedsData, String bedUUID) {
        BedData bedToShare = bedData.remove(bedUUID);
        receiverPlayerBedsData.bedData.put(bedUUID, bedToShare);
    }

    public void removeBed(String bedUUID) {
        bedData.remove(bedUUID);
    }

    public boolean hasBed(String bedUUID) {
        return bedData.containsKey(bedUUID);
    }

    public HashMap<String, BedData> getPlayerBedData() {
        return bedData;
    }

}
