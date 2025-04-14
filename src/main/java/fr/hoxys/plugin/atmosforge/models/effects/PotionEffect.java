package fr.hoxys.plugin.atmosforge.models.effects;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation d'un effet météorologique basé sur des effets de potion.
 */
public class PotionEffect implements WeatherEffect {

    private final Main plugin;
    private final WeatherType weatherType;
    private float intensity;
    private int duration;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int effectDuration;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;

    private final Map<UUID, BukkitTask> playerTasks;
    private final Map<UUID, BukkitTask> worldTasks;

    /**
     * Constructeur de PotionEffect.
     *
     * @param plugin L'instance du plugin.
     * @param weatherType Le type de météo associé.
     * @param intensity L'intensité de l'effet (0.0 à 1.0).
     * @param duration La durée de l'effet en ticks.
     * @param effectType Le type d'effet de potion.
     * @param amplifier L'amplificateur de l'effet (0 = niveau 1, 1 = niveau 2, etc.).
     * @param effectDuration La durée de l'effet de potion en ticks.
     * @param ambient Si l'effet est ambient (particules moins visibles).
     * @param particles Si les particules doivent être affichées.
     * @param icon Si l'icône doit être affichée.
     */
    public PotionEffect(Main plugin, WeatherType weatherType, float intensity, int duration,
                        PotionEffectType effectType, int amplifier, int effectDuration,
                        boolean ambient, boolean particles, boolean icon) {
        this.plugin = plugin;
        this.weatherType = weatherType;
        this.intensity = intensity;
        this.duration = duration;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.effectDuration = effectDuration;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;

        this.playerTasks = new HashMap<>();
        this.worldTasks = new HashMap<>();
    }

    @Override
    public WeatherType getWeatherType() {
        return weatherType;
    }

    @Override
    public void applyToPlayer(Player player, World world) {
        if (isActiveForPlayer(player)) {
            return; // Déjà actif
        }

        // Créer une tâche pour ce joueur
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Ne pas appliquer l'effet si le joueur n'est plus en ligne ou a changé de monde
            if (!player.isOnline() || !player.getWorld().equals(world)) {
                removeFromPlayer(player);
                return;
            }

            // Calculer l'amplificateur réel basé sur l'intensité
            int realAmplifier = Math.max(0, (int) (amplifier * intensity));

            // Appliquer l'effet de potion
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    effectType, effectDuration, realAmplifier, ambient, particles, icon));

        }, 0L, effectDuration / 2); // Renouveler l'effet avant qu'il n'expire

        playerTasks.put(player.getUniqueId(), task);
    }

    @Override
    public void applyToWorld(World world) {
        if (isActiveForWorld(world)) {
            return; // Déjà actif
        }

        // Créer une tâche pour ce monde
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Calculer l'amplificateur réel basé sur l'intensité
            int realAmplifier = Math.max(0, (int) (amplifier * intensity));

            // Pour chaque joueur dans ce monde
            for (Player player : world.getPlayers()) {
                // Appliquer l'effet de potion
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        effectType, effectDuration, realAmplifier, ambient, particles, icon));
            }
        }, 0L, effectDuration / 2); // Renouveler l'effet avant qu'il n'expire

        worldTasks.put(world.getUID(), task);
    }

    @Override
    public void removeFromPlayer(Player player) {
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        // Supprimer l'effet de potion
        player.removePotionEffect(effectType);
    }

    @Override
    public void removeFromWorld(World world) {
        BukkitTask task = worldTasks.remove(world.getUID());
        if (task != null) {
            task.cancel();
        }

        // Supprimer l'effet de potion pour tous les joueurs dans ce monde
        for (Player player : world.getPlayers()) {
            player.removePotionEffect(effectType);
        }
    }

    @Override
    public boolean isActiveForPlayer(Player player) {
        return playerTasks.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isActiveForWorld(World world) {
        return worldTasks.containsKey(world.getUID());
    }

    @Override
    public float getIntensity() {
        return intensity;
    }

    @Override
    public void setIntensity(float intensity) {
        this.intensity = Math.max(0.0f, Math.min(1.0f, intensity));
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Annule toutes les tâches et supprime tous les effets de potion.
     */
    public void cancelAllTasks() {
        // Annuler les tâches de joueurs et supprimer les effets
        for (Map.Entry<UUID, BukkitTask> entry : playerTasks.entrySet()) {
            entry.getValue().cancel();

            // Supprimer l'effet de potion pour le joueur
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                player.removePotionEffect(effectType);
            }
        }
        playerTasks.clear();

        // Annuler les tâches de mondes et supprimer les effets pour tous les joueurs
        for (Map.Entry<UUID, BukkitTask> entry : worldTasks.entrySet()) {
            entry.getValue().cancel();

            // Supprimer l'effet de potion pour tous les joueurs dans ce monde
            World world = plugin.getServer().getWorld(entry.getKey());
            if (world != null) {
                for (Player player : world.getPlayers()) {
                    player.removePotionEffect(effectType);
                }
            }
        }
        worldTasks.clear();
    }
}