package fr.hoxys.plugin.atmosforge.models.effects;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Implémentation d'un effet météorologique basé sur des particules.
 */
public class ParticleEffect implements WeatherEffect {

    private final Main plugin;
    private final WeatherType weatherType;
    private final Particle particleType;
    private float intensity;
    private int duration;
    private final int particleCount;
    private final double particleSpread;
    private final double particleHeight;
    private final float particleSpeed;

    private final Map<UUID, BukkitTask> playerTasks;
    private final Map<UUID, BukkitTask> worldTasks;
    private final Random random;

    /**
     * Constructeur de ParticleEffect.
     *
     * @param plugin L'instance du plugin.
     * @param weatherType Le type de météo associé.
     * @param particleType Le type de particule Minecraft.
     * @param intensity L'intensité de l'effet (0.0 à 1.0).
     * @param duration La durée de l'effet en ticks.
     * @param particleCount Le nombre de particules par cycle.
     * @param particleSpread L'étendue des particules par rapport au centre.
     * @param particleHeight La hauteur des particules par rapport au joueur.
     * @param particleSpeed La vitesse des particules.
     */
    public ParticleEffect(Main plugin, WeatherType weatherType, Particle particleType,
                          float intensity, int duration, int particleCount,
                          double particleSpread, double particleHeight, float particleSpeed) {
        this.plugin = plugin;
        this.weatherType = weatherType;
        this.particleType = particleType;
        this.intensity = intensity;
        this.duration = duration;
        this.particleCount = particleCount;
        this.particleSpread = particleSpread;
        this.particleHeight = particleHeight;
        this.particleSpeed = particleSpeed;

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

            // Calculer le nombre réel de particules basé sur l'intensité
            int count = (int) (particleCount * intensity);

            // Créer des particules autour du joueur
            for (int i = 0; i < count; i++) {
                // Position aléatoire dans un rayon
                double x = player.getLocation().getX() + (random.nextDouble() * 2 - 1) * particleSpread;
                double z = player.getLocation().getZ() + (random.nextDouble() * 2 - 1) * particleSpread;
                double y = player.getLocation().getY() + particleHeight + random.nextDouble() * particleSpread;

                Location particleLocation = new Location(world, x, y, z);

                // Afficher la particule pour ce joueur uniquement
                player.spawnParticle(particleType, particleLocation, 1, 0, 0, 0, particleSpeed);
            }
        }, 0L, 5L); // Toutes les 5 ticks (1/4 de seconde)

        playerTasks.put(player.getUniqueId(), task);
    }

    @Override
    public void applyToWorld(World world) {
        if (isActiveForWorld(world)) {
            return; // Déjà actif
        }

        // Créer une tâche pour ce monde
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Calculer le nombre réel de particules basé sur l'intensité
            int count = (int) (particleCount * intensity);

            // Pour chaque joueur dans ce monde
            for (Player player : world.getPlayers()) {
                // Créer des particules autour du joueur
                for (int i = 0; i < count; i++) {
                    // Position aléatoire dans un rayon
                    double x = player.getLocation().getX() + (random.nextDouble() * 2 - 1) * particleSpread;
                    double z = player.getLocation().getZ() + (random.nextDouble() * 2 - 1) * particleSpread;
                    double y = player.getLocation().getY() + particleHeight + random.nextDouble() * particleSpread;

                    Location particleLocation = new Location(world, x, y, z);

                    // Afficher la particule pour tous les joueurs du monde
                    world.spawnParticle(particleType, particleLocation, 1, 0, 0, 0, particleSpeed);
                }
            }
        }, 0L, 5L); // Toutes les 5 ticks (1/4 de seconde)

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
     * Obtient le type de particule utilisé pour cet effet.
     *
     * @return Le type de particule.
     */
    public Particle getParticleType() {
        return particleType;
    }

    /**
     * Obtient l'étendue des particules par rapport au centre.
     *
     * @return L'étendue des particules.
     */
    public double getParticleSpread() {
        return particleSpread;
    }

    /**
     * Obtient la hauteur des particules par rapport au joueur.
     *
     * @return La hauteur des particules.
     */
    public double getParticleHeight() {
        return particleHeight;
    }

    /**
     * Annule toutes les tâches de particules pour tous les joueurs et mondes.
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