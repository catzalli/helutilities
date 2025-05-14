package me.heliostudios.helutilities;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class BlockBreakListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        Block block = event.getBlock();
        Location location = block.getLocation().add(0.5, 0.5, 0.5);
        
        if (!isValidLocation(location)) return;

        // Mahsul kontrolü
        if (block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() < ageable.getMaximumAge()) return;
        }

        String key = MaterialHandler.getMaterialName(block.getType());
        List<RewardRegistry.Reward> rewards = RewardRegistry.getRewards(key);
        if (rewards.isEmpty()) return;

        for (RewardRegistry.Reward reward : rewards) {
            if (reward.shouldDrop()) {
                reward.give(player);
            }
        }
    }

    private boolean isValidLocation(Location loc) {
        return loc != null && loc.getWorld() != null;
    }
}