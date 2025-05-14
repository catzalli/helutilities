package me.heliostudios.helutilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.ChatColor;
import net.milkbowl.vault.economy.Economy;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ödül sistemini yöneten ana sınıf
 */
public class RewardRegistry {
    private static final Logger LOGGER = HelUtilities.getInstance().getLogger();
    private static volatile FileConfiguration config;
    private static volatile Economy economy;
    static final Map<String, String> messageCache = new HashMap<>();
    private static final String REWARD_BLOCKS_PATH = "reward-blocks";

    private RewardRegistry() {
    }
    public static synchronized void load() {
        HelUtilities plugin = HelUtilities.getInstance();
        config = plugin.getConfig();
        messageCache.clear();
        setupEconomy();
    }
    private static void setupEconomy() {
        if (HelUtilities.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            LOGGER.warning("Vault bulunamadı! Ekonomi özellikleri devre dışı bırakıldı.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = HelUtilities.getInstance().getServer()
                .getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
            LOGGER.info("Vault ekonomi sistemi başarıyla yüklendi.");
        } else {
            LOGGER.warning("Vault provider bulunamadı! Ekonomi özellikleri devre dışı.");
        }
    }

    public static List<Reward> getRewards(String key) {
        if (key == null || key.isEmpty() || config == null) {
            return Collections.emptyList();
        }

        ConfigurationSection blockSection = config.getConfigurationSection(REWARD_BLOCKS_PATH + "." + key);
        if (blockSection == null) {
            return Collections.emptyList();
        }

        List<Reward> rewards = new ArrayList<>();
        List<String> rewardStrings = blockSection.getStringList("rewards");

        for (String rewardString : rewardStrings) {
            try {
                Reward reward = Reward.fromString(rewardString);
                if (reward != null) {
                    loadEffects(blockSection, reward);
                    rewards.add(reward);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Ödül yüklenirken hata oluştu: " + rewardString, e);
            }
        }

        return rewards;
    }

    private static void loadEffects(ConfigurationSection section, Reward reward) {
        if (!section.contains("effects")) return;

        List<String> effectStrings = section.getStringList("effects");
        for (String effectString : effectStrings) {
            try {
                String[] parts = effectString.split(" ", 2);
                String effectType = parts[0].toLowerCase();
                String effectData = parts.length > 1 ? parts[1] : "";
                reward.addEffect(new RewardEffect(null, effectType, effectData));
            } catch (Exception e) {
                LOGGER.warning("Efekt yüklenirken hata: " + effectString);
            }
        }
    }

    public static class Reward {
        private final String type;
        private final String material;
        private final double amount;
        private final int dropChance;
        private final List<RewardEffect> effects;
        private final String command;
        private static final int DEFAULT_DROP_CHANCE = 100;

        public Reward(String type, String material, double amount, int dropChance, String command) {
            this.type = type;
            this.material = material;
            this.amount = amount;
            this.dropChance = validateDropChance(dropChance);
            this.command = command;
            this.effects = new ArrayList<>();
        }

        private static int validateDropChance(int chance) {
            return Math.max(0, Math.min(100, chance));
        }

        public void addEffect(RewardEffect effect) {
            Objects.requireNonNull(effect, "Effect can not be null");
            this.effects.add(effect);
        }

        public boolean shouldDrop() {
            return ThreadLocalRandom.current().nextInt(100) < dropChance;
        }

        public void give(Player player) {
            if (player == null) return;

            Location location = player.getLocation();
            try {
                switch (type.toLowerCase()) {
                    case "item" -> giveItem(player);
                    case "money" -> giveMoney(player);
                    case "command" -> executeCommand(player);
                    default -> LOGGER.warning("Unknown reward type: " + type);
                }

                playEffects(location);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error while giving reward", e);
            }
        }

        private void playEffects(Location location) {
            if (location == null) return;

            for (RewardEffect effect : effects) {
                try {
                    effect.setLocation(location);
                    effect.play();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error while playing effects", e);
                }
            }
        }

        private void giveItem(Player player) {
            if (material == null || amount <= 0) return;

            try {
                ItemStack item = MaterialHandler.createItemStack(material, (int) amount);
                if (item == null) {
                    LOGGER.warning("Geçersiz item materyal: " + material);
                    return;
                }

                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), item);
                    sendMessage(player, "inventory-full");
                    return;
                }

                player.getInventory().addItem(item);
                sendFormattedMessage(player, "item-reward",
                        Map.of("%amount%", String.valueOf((int) amount),
                                "%item%", item.getType().toString().toLowerCase()));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Item verme hatası: " + e.getMessage());
            }
        }



        private void giveMoney(Player player) {
            if (amount <= 0) return;

            if (economy != null) {
                economy.depositPlayer(player, amount);
                sendFormattedMessage(player, "money-reward",
                        Map.of("%amount%", String.format("%.2f", amount)));
            } else {
                sendMessage(player, "economy-error");
                LOGGER.warning("Ekonomi sistemi aktif değil, para ödülü verilemedi!");
            }
        }

        private void executeCommand(Player player) {
            if (command == null || command.isEmpty()) return;

            String sanitizedCommand = sanitizeCommand(command, player);
            try {
                HelUtilities.getInstance().getServer().getScheduler().runTask(
                        HelUtilities.getInstance(),
                        () -> HelUtilities.getInstance().getServer().dispatchCommand(
                                HelUtilities.getInstance().getServer().getConsoleSender(),
                                sanitizedCommand
                        )
                );
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Komut çalıştırılırken hata: " + sanitizedCommand, e);
            }
        }

        private String sanitizeCommand(String cmd, Player player) {
            return cmd.replace("%player%", player.getName())
                    .replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                    .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                    .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));
        }

        public static Reward fromString(String str) {
            if (str == null || str.trim().isEmpty()) {
                LOGGER.warning("Boş ödül string'i");
                return null;
            }

            String[] parts = str.split(" ");
            if (parts.length < 2) {
                LOGGER.warning("Yetersiz ödül parametreleri: " + str);
                return null;
            }

            try {
                String type = parts[0].toLowerCase();
                String material = null;
                double amount = 0;
                int dropChance = DEFAULT_DROP_CHANCE;
                String command = null;

                switch (type) {
                    case "item" -> {
                        if (parts.length < 4) {
                            LOGGER.warning("Yetersiz item parametreleri: " + str);
                            return null;
                        }
                        material = parts[1];
                        amount = Double.parseDouble(parts[2]);
                        dropChance = Integer.parseInt(parts[3]);
                    }
                    case "money" -> {
                        if (parts.length < 3) {
                            LOGGER.warning("Para miktarı veya şans belirtilmemiş: " + str);
                            return null;
                        }
                        amount = Double.parseDouble(parts[1]);
                        dropChance = Integer.parseInt(parts[2]);
                    }
                    case "command" -> {
                        if (parts.length < 3) {
                            LOGGER.warning("Komut parametreleri eksik: " + str);
                            return null;
                        }
                        dropChance = Integer.parseInt(parts[1]);
                        command = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
                        amount = 1;
                    }
                    default -> {
                        LOGGER.warning("Geçersiz ödül tipi: " + type);
                        return null;
                    }
                }

                // Şans değerini doğrula
                if (dropChance < 0 || dropChance > 100) {
                    LOGGER.warning("Geçersiz şans değeri (0-100 arası olmalı): " + dropChance);
                    dropChance = DEFAULT_DROP_CHANCE;
                }

                return new Reward(type, material, amount, dropChance, command);
            } catch (NumberFormatException e) {
                LOGGER.warning("Geçersiz sayısal değer: " + str);
                return null;
            } catch (Exception e) {
                LOGGER.warning("Ödül oluşturulurken hata: " + str + " - " + e.getMessage());
                return null;
            }
        }

        private void sendMessage(Player player, String key) {
            String message = getMessage(key);
            if (message != null) {
                player.sendMessage(message);
            }
        }

        private void sendFormattedMessage(Player player, String key, Map<String, String> replacements) {
            String message = getMessage(key);
            if (message != null) {
                for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                    message = message.replace(replacement.getKey(), replacement.getValue());
                }
                player.sendMessage(message);
            }
        }

        private static String getMessage(String path) {
            return messageCache.computeIfAbsent(path, k -> {
                if (config == null) return null;

                if (!config.getBoolean("messages." + k + ".enabled", true)) {
                    return null;
                }

                String prefix = config.getString("prefix", "&8[&6HelUtilities&8] ");
                String message = config.getString("messages." + k + ".text");

                if (message == null) return null;

                return ChatColor.translateAlternateColorCodes('&',
                        (prefix != null ? prefix : "") + message);
            });
        }
    }
}