package de.theredend2000.advancedegghunt.listeners;

import de.theredend2000.advancedegghunt.Main;
import de.theredend2000.advancedegghunt.managers.eggmanager.EggManager;
import de.theredend2000.advancedegghunt.managers.soundmanager.SoundManager;
import de.theredend2000.advancedegghunt.util.messages.MessageKey;
import de.theredend2000.advancedegghunt.util.messages.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;


public class BlockBreakEventListener implements Listener {

    private MessageManager messageManager;

    public BlockBreakEventListener(){
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        messageManager = Main.getInstance().getMessageManager();
    }

    @EventHandler
    public void onDestroyEgg(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();

        EggManager eggManager = Main.getInstance().getEggManager();
        SoundManager soundManager = Main.getInstance().getSoundManager();

        if(eggManager.containsEgg(block)){
            String section = eggManager.getEggSection(block);
            String permission = Main.getInstance().getConfig().getString("Permissions.BreakEggPermission");
            if(Main.getInstance().getPlaceEggsPlayers().contains(player)) {
                if(player.hasPermission(permission)){
                    eggManager.removeEgg(player, block,section);
                    player.playSound(player.getLocation(), soundManager.playEggBreakSound(), soundManager.getSoundVolume(), 1);
                }else {
                    player.sendMessage(messageManager.getMessage(MessageKey.PERMISSION_ERROR).replaceAll("%PERMISSION%", permission));
                    event.setCancelled(true);
                }
            }else {
                if(player.hasPermission(permission))
                    player.sendMessage(messageManager.getMessage(MessageKey.ONLY_IN_PLACEMODE));
                event.setCancelled(true);
            }
            eggManager.updateMaxEggs(section);
        }
    }

    @EventHandler
    public void onBlockFromToEvent(BlockFromToEvent event) {
        Block toblock = event.getToBlock();
        EggManager eggManager = Main.getInstance().getEggManager();

        if(eggManager.containsEgg(toblock))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketFillEvent(PlayerBucketEmptyEvent event) {
        Block block = event.getBlock();
        EggManager eggManager = Main.getInstance().getEggManager();

        if(eggManager.containsEgg(block))
            event.setCancelled(true);
    }
}
