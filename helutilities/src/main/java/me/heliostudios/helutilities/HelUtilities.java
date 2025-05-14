package me.heliostudios.helutilities;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import static me.heliostudios.helutilities.RewardRegistry.messageCache;

public class HelUtilities extends JavaPlugin {

    private static HelUtilities instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        RewardRegistry.load();
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
    }
    @Override
    public void onDisable() {
        messageCache.clear();
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((label.equalsIgnoreCase("helutilitiesreload") || label.equalsIgnoreCase("hreload")) &&
                sender.hasPermission("helutilities.admin")) {

            reloadConfig();
            RewardRegistry.load();
            sender.sendMessage("§aHelUtilities config dosyası yeniden yüklendi!");
            return true;
        }
        return false;
    }

    public static HelUtilities getInstance() {
        return instance;
    }

}
