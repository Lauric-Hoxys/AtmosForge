package fr.hoxys.plugin.atmosforge.models.effects;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Implémentation d'un effet météorologique basé sur des sons.
 */
public class SoundEffect implements WeatherEffect {

    private final Main plugin;
    private final WeatherType weatherType;
    private final Sound soundType;
    private float intensity;
    private int duration;
    private final float volume;
    private final float pitch;
    private final int interval;

    private final Map<UUID, BukkitTask> playerTasks;
    private final Map<UUID, BukkitTask> worldTasks;
    private final Random random;

    /**
     * Constructeur de SoundEffect.
     *
     * @param plugin L'instance du plugin.
     * @param weatherType Le type de météo associé.
     * @param soundType Le type de son Minecraft.
     * @param intensity L'intensité de l'effet (0.0 à 1.0).
     * @param duration La durée de l'effet en ticks.
     * @param volume Le volume du son (0.0 à 1.0).
     * @param pitch La hauteur du son (0.5 à 2.0).
     * @param interval L'intervalle entre les sons en ticks.
     */
    public SoundEffect(Main plugin, WeatherType weatherType, Sound soundType,
                       float intensity, int duration, float volume, float pitch, int interval) {
        this.plugin = plugin;
        this.weatherType = weatherType;
        this.soundType = soundType;
        this.intensity = intensity;
        this.duration = duration;
        this.volume = volume;
        this.pitch = pitch;
        this.interval = interval;

        this.playerTasks = new HashMap<>();
        this.worldTasks = new HashMap<>();
        this.random = new Random();
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

            // Calculer le volume réel basé sur l'intensité
            float actualVolume = volume * intensity;

            // Jouer le son pour ce joueur uniquement
            player.playSound(player.getLocation(), soundType, actualVolume, pitch);

        }, 0L, interval); // Intervalle entre les sons

        playerTasks.put(player.getUniqueId(), task);
    }

    @Override
    public void applyToWorld(World world) {
        if (isActiveForWorld(world)) {
            return; // Déjà actif
        }

        // Créer une tâche pour ce monde
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Calculer le volume réel basé sur l'intensité
            float actualVolume = volume * intensity;

            // Pour chaque joueur dans ce monde
            for (Player player : world.getPlayers()) {
                // Jouer le son pour tous les joueurs du monde
                player.playSound(player.getLocation(), soundType, actualVolume, pitch);
            }
        }, 0L, interval); // Intervalle entre les sons

        worldTasks.put(world.getUID(), task);
    }

    @Override
    public void removeFromPlayer(Player player) {
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void removeFromWorld(World world) {
        BukkitTask task = worldTasks.remove(world.getUID());
        if (task != null) {
            task.cancel();
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
     * Obtient le type de son utilisé pour cet effet.
     *
     * @return Le type de son.
     */
    public Sound getSoundType() {
        return soundType;
    }

    /**
     * Obtient le volume du son.
     *
     * @return Le volume du son.
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Obtient la hauteur du son.
     *
     * @return La hauteur du son.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Annule toutes les tâches de sons pour tous les joueurs et mondes.
     */
    public void cancelAllTasks() {
        // Annuler les tâches de joueurs
        for (BukkitTask task : playerTasks.values()) {
            task.cancel();
        }
        playerTasks.clear();

        // Annuler les tâches de mondes
        for (BukkitTask task : worldTasks.values()) {
            task.cancel();
        }
        worldTasks.clear();
    }
}