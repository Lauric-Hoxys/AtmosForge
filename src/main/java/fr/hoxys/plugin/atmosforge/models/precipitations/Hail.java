package fr.hoxys.plugin.atmosforge.models.precipitations;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Weather;
import fr.hoxys.plugin.atmosforge.models.WeatherType;
import fr.hoxys.plugin.atmosforge.models.effects.ParticleEffect;
import fr.hoxys.plugin.atmosforge.models.effects.SoundEffect;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

/**
 * Implémentation de la grêle pour AtmosForge.
 */
public class Hail extends Weather {

    private final WeatherType intensity;

    /**
     * Constructeur pour créer différents types de grêle.
     *
     * @param plugin L'instance du plugin.
     * @param intensity L'intensité de la grêle (SMALL_HAIL, MEDIUM_HAIL, LARGE_HAIL).
     */
    public Hail(Main plugin, WeatherType intensity) {
        super(plugin, intensity);
        this.intensity = intensity;

        // Ajouter des effets spécifiques en fonction du type de grêle
        initializeEffects();
    }

    /**
     * Initialise les effets spécifiques à ce type de grêle.
     */
    private void initializeEffects() {
        // Déterminer les paramètres en fonction de l'intensité
        float particleIntensity = 0.5f;
        int particleCount = 10;
        float soundVolume = 0.4f;
        double damage = 0.0;

        switch (intensity) {
            case SMALL_HAIL:
                particleIntensity = 0.4f;
                particleCount = 8;
                soundVolume = 0.3f;
                break;
            case MEDIUM_HAIL:
                particleIntensity = 0.6f;
                particleCount = 15;
                soundVolume = 0.5f;
                damage = 0.5;
                break;
            case LARGE_HAIL:
                particleIntensity = 0.9f;
                particleCount = 25;
                soundVolume = 0.7f;
                damage = 1.0;
                break;
            default:
                break;
        }

        // Créer un effet de particules de grêle
        ParticleEffect hailParticles = new ParticleEffect(
                getPlugin(),
                intensity,
                Particle.SNOWFLAKE,
                particleIntensity,
                24000, // Dure une journée Minecraft entière
                particleCount,
                20.0, // Rayon de 20 blocs
                15.0, // Hauteur au-dessus du joueur
                0.2f // Vitesse des particules
        );

        // Créer un effet sonore de grêle
        SoundEffect hailSound = new SoundEffect(
                getPlugin(),
                intensity,
                Sound.BLOCK_GLASS_BREAK,
                particleIntensity,
                24000, // Dure une journée Minecraft entière
                soundVolume,
                1.5f, // Plus aigu pour simuler l'impact de la grêle
                20 // Jouer toutes les secondes
        );

        // Ajouter les effets à cette météo
        addEffect(hailParticles);
        addEffect(hailSound);

        // Ajouter un effet de pluie en arrière-plan
        ParticleEffect rainParticles = new ParticleEffect(
                getPlugin(),
                intensity,
                Particle.FALLING_DRIPSTONE_WATER,
                particleIntensity * 0.5f,
                24000, // Dure une journée Minecraft entière
                particleCount / 2,
                20.0, // Rayon de 20 blocs
                15.0, // Hauteur au-dessus du joueur
                0.1f // Vitesse des particules
        );

        addEffect(rainParticles);

        // Ajouter un effet de dégâts pour la grêle moyenne et grande
        if (damage > 0) {
            addDamageEffect(damage, intensity == WeatherType.LARGE_HAIL ? 100 : 200); // Dégâts plus fréquents pour la grosse grêle
        }
    }

    /**
     * Obtient l'intensité de la grêle.
     *
     * @return Le type de météo représentant l'intensité.
     */
    public WeatherType getIntensity() {
        return intensity;
    }

    @Override
    public void applyToWorld(World world) {
        super.applyToWorld(world);

        // Activer la pluie dans Minecraft
        world.setStorm(true);

        // Activer le tonnerre pour la grosse grêle
        world.setThundering(intensity == WeatherType.LARGE_HAIL);

        // Définir une longue durée pour éviter que Minecraft ne change la météo
        world.setWeatherDuration(24000); // 20 minutes
        if (intensity == WeatherType.LARGE_HAIL) {
            world.setThunderDuration(24000); // 20 minutes
        }
    }

    @Override
    public void removeFromWorld(World world) {
        super.removeFromWorld(world);

        // Ne pas désactiver la pluie dans Minecraft, car elle pourrait être utilisée par une autre météo
    }
}