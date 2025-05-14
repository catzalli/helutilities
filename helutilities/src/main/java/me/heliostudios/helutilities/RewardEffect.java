package me.heliostudios.helutilities;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class RewardEffect {
    private Location location;  // final kaldırıldı çünkü setLocation kullanacağız
    private final String type;
    private final String data;
    private void logEffectError(String effectType, Exception e) {
        HelUtilities.getInstance().getLogger().warning(
                String.format("Geçersiz %s efekti: %s", effectType, e.getMessage())
        );
    }

    public RewardEffect(Location location, String type, String data) {
        this.location = location;
        this.type = type.toLowerCase();
        this.data = data;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void play() {
        if (location == null) return;

        switch (type) {
            case "firework" -> spawnFirework();
            case "particle" -> spawnParticles();
            case "sound" -> playSound();
        }
    }

    private void spawnFirework() {
        Firework fw = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.RED, Color.YELLOW, Color.BLUE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .build();

        meta.addEffect(effect);
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    private void spawnParticles() {
        try {
            Particle particle = Particle.valueOf(data.toUpperCase());
            location.getWorld().spawnParticle(particle, location, 50, 0.5, 0.5, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            logEffectError("particle", e);
        }
    }

    private void playSound() {
        try {
            Sound sound = Sound.valueOf(data.toUpperCase());
            location.getWorld().playSound(location, sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            logEffectError("sound", e);
        }
    }
}