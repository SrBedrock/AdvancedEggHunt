package de.theredend2000.advancedegghunt.managers.eggmanager;

import de.theredend2000.advancedegghunt.Main;
import de.theredend2000.advancedegghunt.util.enums.DeletionTypes;
import de.theredend2000.advancedegghunt.util.messages.MessageKey;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerEggDataManager {
    private Main plugin;
    private File dataFolder;
    private HashMap<UUID, FileConfiguration> playerConfigs;
    private HashMap<UUID, File> playerFiles;

    public PlayerEggDataManager() {
        plugin = Main.getInstance();
        dataFolder = plugin.getDataFolder();
        playerConfigs = new HashMap<>();
        playerFiles = new HashMap<>();
    }

    public void initPlayers() {
        List<UUID> savedPlayers = new ArrayList<>(plugin.getEggDataManager().savedPlayers());
        for(UUID uuid : savedPlayers)
            loadPlayerData(uuid);
    }

    private void loadPlayerData(UUID uuid) {
        FileConfiguration config = getPlayerData(uuid);
        this.playerConfigs.put(uuid, config);
    }

    private File getFile(UUID uuid) {
        if(!playerFiles.containsKey(uuid))
            playerFiles.put(uuid,new File(this.dataFolder + "/playerdata/", uuid + ".yml"));
        return playerFiles.get(uuid);
    }

    public FileConfiguration getPlayerData(UUID uuid) {
        File playerFile = this.getFile(uuid);
        if(!playerConfigs.containsKey(uuid)) {
            this.playerConfigs.put(uuid, YamlConfiguration.loadConfiguration(playerFile));
        }
        return playerConfigs.get(uuid);
    }

    public void savePlayerData(UUID uuid, FileConfiguration config) {
        try {
            config.save(this.getFile(uuid));
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public void savePlayerSection(UUID uuid, String section) {
        FileConfiguration config = this.getPlayerData(uuid);
        config.set("SelectedSection", section);
        this.savePlayerData(uuid, config);
    }

    public void createPlayerFile(UUID uuid) {
        FileConfiguration config = this.getPlayerData(uuid);
        File playerFile = this.getFile(uuid);
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }
        this.playerConfigs.put(uuid, config);
        this.loadPlayerData(uuid);
        this.savePlayerData(uuid, config);
        this.setDeletionType(DeletionTypes.Noting, uuid);
        this.savePlayerSection(uuid, "default");
    }

    public void setDeletionType(DeletionTypes deletionType, UUID uuid) {
        FileConfiguration config = this.getPlayerData(uuid);
        config.set("DeletionType", deletionType.name());
        this.savePlayerData(uuid, config);
    }

    public DeletionTypes getDeletionType(UUID uuid) {
        FileConfiguration config = this.getPlayerData(uuid);
        return DeletionTypes.valueOf(config.getString("DeletionType"));
    }

    public void setResetTimer(UUID uuid, String section, String id) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(this.getFile(uuid));
        int currentSeconds = Main.getInstance().getRequirementsManager().getOverallTime(section);
        if (currentSeconds != 0) {
            long toSet = System.currentTimeMillis() + (long)currentSeconds * 1000L;
            cfg.set("FoundEggs." + section + "." + id + ".ResetCooldown", toSet);

            try {
                cfg.save(this.getFile(uuid));
            } catch (IOException var9) {
                var9.printStackTrace();
            }
        }

    }

    public long getResetTimer(UUID uuid, String section, String id) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(this.getFile(uuid));
        return !cfg.contains("FoundEggs." + section + "." + id + ".ResetCooldown") ? System.currentTimeMillis() + 1000000L : cfg.getLong("FoundEggs." + section + "." + id + ".ResetCooldown");
    }

    public boolean canReset(UUID uuid, String section, String id) {
        long current = System.currentTimeMillis();
        long millis = this.getResetTimer(uuid, section, id);
        return current > millis;
    }

    public void checkReset(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for(UUID uuids : plugin.getEggDataManager().savedPlayers()){
                    FileConfiguration cfg = YamlConfiguration.loadConfiguration(getFile(uuids));
                    if(!cfg.contains("FoundEggs.")) return;
                    for(String sections : cfg.getConfigurationSection("FoundEggs.").getKeys(false)) {
                        for(String id : cfg.getConfigurationSection("FoundEggs."+sections).getKeys(false)) {
                            if (id.equals("Count") || id.equals("Name")) continue;
                            if (canReset(uuids, sections, id))
                                plugin.getEggManager().resetStatsPlayerEgg(uuids,sections,id);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin,20,20);
    }
}