package fr.hoxys.plugin.atmosforge.models.precipitations;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Weather;
import fr.hoxys.plugin.atmosforge.models.WeatherType;
import fr.hoxys.plugin.atmosforge.models.effects.ParticleEffect;
import fr.hoxys.plugin.atmosforge.models.effects.SoundEffect;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

/**
 * Implémentation de la pluie pour AtmosForge.
 */
public class Rain extends Weather {

    private final WeatherType intensity;
    private final boolean cold;
    private final boolean acid;

    /**
     * Constructeur pour créer différents types de pluie.
     *
     * @param plugin L'instance du plugin.
     * @param intensity L'intensité de la pluie (LIGHT_RAIN, MODERATE_RAIN, HEAVY_RAIN, etc.).
     * @param cold Si la pluie est froide (pluie verglaçante).
     * @param acid Si la pluie est acide.
     */
    public Rain(Main plugin, WeatherType intensity, boolean cold, boolean acid) {
        super(plugin, intensity);
        this.intensity = intensity;
        this.cold = cold;
        this.acid = acid;

        // Ajouter des effets spécifiques en fonction du type de pluie
        initializeEffects();
    }

    /**
     * Initialise les effets spécifiques à ce type de pluie.
     */
    private void initializeEffects() {
        // Déterminer les paramètres en fonction de l'intensité
        float particleIntensity = 0.5f;
        int particleCount = 10;
        float soundVolume = 0.3f;

        switch (intensity) {
            case LIGHT_RAIN:
                particleIntensity = 0.3f;
                particleCount = 5;
                soundVolume = 0.2f;
                break;
            case MODERATE_RAIN:
                particleIntensity = 0.5f;
                particleCount = 10;
                soundVolume = 0.4f;
                break;
            case HEAVY_RAIN:
            case SHOWER:
                particleIntensity = 0.8f;
                particleCount = 20;
                soundVolume = 0.7f;
                break;
            case DRIZZLE:
                particleIntensity = 0.2f;
                particleCount = 3;
                soundVolume = 0.1f;
                break;
            default:
                break;
        }

        // Créer un effet de particules de pluie
        ParticleEffect rainParticles = new ParticleEffect(
                getPlugin(),
                intensity,
                Particle.FALLING_DRIPSTONE_WATER,
                particleIntensity,
                24000, // Dure une journée Minecraft entière
                particleCount,
                20.0, // Rayon de 20 blocs
                15.0, // Hauteur au-dessus du joueur
                0.1f // Vitesse des particules
        );

        // Créer un effet sonore de pluie
        SoundEffect rainSound = new SoundEffect(
                getPlugin(),
                intensity,
                Sound.WEATHER_RAIN,
                particleIntensity,
                24000, // Dure une journée Minecraft entière
                soundVolume,
                1.0f, // Hauteur normale
                40 // Jouer toutes les 2 secondes
        );

        // Ajouter les effets à cette météo
        addEffect(rainParticles);
        addEffect(rainSound);

        // Ajouter des effets supplémentaires pour les types spéciaux de pluie
        if (cold) {
            // Effet de ralentissement pour la pluie froide
            addPotionEffect(PotionEffectType.SLOWNESS, 0, 100, true);
        }

        if (acid) {
            // Effet de dégâts pour la pluie acide
            addDamageEffect(1.0, 200); // 0.5 cœur toutes les 10 secondes
        }
    }

    /**
     * Vérifie si cette pluie est froide (pluie verglaçante).
     *
     * @return true si la pluie est froide, false sinon.
     */
    public boolean isCold() {
        return cold;
    }

    /**
     * Vérifie si cette pluie est acide.
     *
     * @return true si la pluie est acide, false sinon.
     */
    public boolean isAcid() {
        return acid;
    }

    /**
     * Obtient l'intensité de la pluie.
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
        world.setThundering(false);

        // Définir une longue durée pour éviter que Minecraft ne change la météo
        world.setWeatherDuration(24000); // 20 minutes
    }

    @Override
    public void removeFromWorld(World world) {
        super.removeFromWorld(world);

        // Ne pas désactiver la pluie dans Minecraft, car elle pourrait être utilisée par une autre météo
    }
}