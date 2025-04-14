package fr.hoxys.plugin.atmosforge.utils;

import fr.hoxys.plugin.atmosforge.models.WeatherType;
import fr.hoxys.plugin.atmosforge.models.effects.BlockEffect;
import fr.hoxys.plugin.atmosforge.models.effects.ParticleEffect;
import fr.hoxys.plugin.atmosforge.models.effects.PotionEffect;
import fr.hoxys.plugin.atmosforge.models.effects.SoundEffect;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Classe utilitaire pour manipuler les effets météorologiques.
 */
public class EffectUtils {

    /**
     * Joue un son à une location.
     *
     * @param location La location où jouer le son.
     * @param sound Le son à jouer.
     * @param volume Le volume du son (0.0 à 1.0).
     * @param pitch La hauteur du son (0.5 à 2.0).
     */
    public static void playSound(Location location, Sound sound, float volume, float pitch) {
        World world = location.getWorld();
        if (world == null) return;

        world.playSound(location, sound, volume, pitch);
    }

    /**
     * Joue un son pour un joueur.
     *
     * @param player Le joueur pour lequel jouer le son.
     * @param sound Le son à jouer.
     * @param volume Le volume du son (0.0 à 1.0).
     * @param pitch La hauteur du son (0.5 à 2.0).
     */
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Crée des particules à une location.
     *
     * @param location La location où créer les particules.
     * @param particle Le type de particule.
     * @param count Le nombre de particules.
     * @param offsetX Le décalage X.
     * @param offsetY Le décalage Y.
     * @param offsetZ Le décalage Z.
     * @param speed La vitesse des particules.
     */
    public static void spawnParticle(Location location, Particle particle, int count,
                                     double offsetX, double offsetY, double offsetZ, double speed) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    /**
     * Crée des particules colorées à une location.
     *
     * @param location La location où créer les particules.
     * @param color La couleur des particules.
     * @param count Le nombre de particules.
     */
    public static void spawnColoredParticle(Location location, Color color, int count) {
        World world = location.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        world.spawnParticle(Particle.DUST, location, count, 0.5, 0.5, 0.5, 0, dustOptions);
    }

    /**
     * Ajoute un effet de potion à un joueur.
     *
     * @param player Le joueur auquel ajouter l'effet.
     * @param effectType Le type d'effet.
     * @param duration La durée en ticks.
     * @param amplifier L'amplificateur (0 = niveau 1, 1 = niveau 2, etc.).
     * @param ambient Si l'effet est ambient (particules moins visibles).
     * @param particles Si les particules doivent être affichées.
     * @param icon Si l'icône doit être affichée.
     */
    public static void addPotionEffect(Player player, PotionEffectType effectType, int duration,
                                       int amplifier, boolean ambient, boolean particles, boolean icon) {
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                effectType, duration, amplifier, ambient, particles, icon));
    }

    /**
     * Crée un effet de pluie autour d'un joueur.
     *
     * @param player Le joueur autour duquel créer l'effet.
     * @param intensity L'intensité de la pluie (0.0 à 1.0).
     */
    public static void createRainEffect(Player player, float intensity) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int particleCount = (int) (20 * intensity);

        for (int i = 0; i < particleCount; i++) {
            double x = loc.getX() + (Math.random() * 20 - 10);
            double z = loc.getZ() + (Math.random() * 20 - 10);
            double y = loc.getY() + 10;

            Location particleLoc = new Location(world, x, y, z);
            world.spawnParticle(Particle.FALLING_DRIPSTONE_WATER, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Crée un effet de neige autour d'un joueur.
     *
     * @param player Le joueur autour duquel créer l'effet.
     * @param intensity L'intensité de la neige (0.0 à 1.0).
     */
    public static void createSnowEffect(Player player, float intensity) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int particleCount = (int) (20 * intensity);

        for (int i = 0; i < particleCount; i++) {
            double x = loc.getX() + (Math.random() * 20 - 10);
            double z = loc.getZ() + (Math.random() * 20 - 10);
            double y = loc.getY() + 10;

            Location particleLoc = new Location(world, x, y, z);
            world.spawnParticle(Particle.SNOWFLAKE, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Crée un effet de brouillard autour d'un joueur.
     *
     * @param player Le joueur autour duquel créer l'effet.
     * @param intensity L'intensité du brouillard (0.0 à 1.0).
     */
    public static void createFogEffect(Player player, float intensity) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int particleCount = (int) (15 * intensity);

        for (int i = 0; i < particleCount; i++) {
            double x = loc.getX() + (Math.random() * 10 - 5);
            double z = loc.getZ() + (Math.random() * 10 - 5);
            double y = loc.getY() + (Math.random() * 3 - 1);

            Location particleLoc = new Location(world, x, y, z);
            world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.5, 0.1, 0.5, 0);
        }
    }

    /**
     * Crée un effet de foudre à une location.
     *
     * @param location La location où créer l'éclair.
     */
    public static void createLightningEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.strikeLightningEffect(location);
    }
}