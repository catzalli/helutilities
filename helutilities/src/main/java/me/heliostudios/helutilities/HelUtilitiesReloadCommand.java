package me.heliostudios.helutilities;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class HelUtilitiesReloadCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public HelUtilitiesReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("helutilities.admin")) {
            sender.sendMessage("§cBu komutu kullanmak için yetkiniz yok.");
            return false;
        }
        plugin.reloadConfig();
        RewardRegistry.load();
        sender.sendMessage("§aHelUtilities config dosyası başarıyla yenilendi!");
        return true;
    }
}
