package fr.hoxys.plugin.atmosforge.models.precipitations;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Weather;
import fr.hoxys.plugin.atmosforge.models.WeatherType;
import fr.hoxys.plugin.atmosforge.models.effects.BlockEffect;
import fr.hoxys.plugin.atmosforge.models.effects.ParticleEffect;
import fr.hoxys.plugin.atmosforge.models.effects.SoundEffect;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

/**
 * Implémentation de la neige pour AtmosForge.
 */
public class Snow extends Weather {

    private final WeatherType intensity;
    private final boolean colored;
    private final boolean blizzard;

    /**
     * Constructeur pour créer différents types de neige.
     *
     * @param plugin L'instance du plugin.
     * @param intensity L'intensité de la neige (LIGHT_SNOW, MODERATE_SNOW, HEAVY_SNOW, etc.).
     * @param colored Si la neige est colorée.
     * @param blizzard Si c'est un blizzard.
     */
    public Snow(Main plugin, WeatherType intensity, boolean colored, boolean blizzard) {
        super(plugin, intensity);
        this.intensity = intensity;
        this.colored = colored;
        this.blizzard = blizzard;

        // Ajouter des effets spécifiques en fonction du type de neige
        initializeEffects();
    }

    /**
     * Initialise les effets spécifiques à ce type de neige.
     */
    private void initializeEffects() {
        // Déterminer les paramètres en fonction de l'intensité
        float particleIntensity = 0.5f;
        int particleCount = 10;
        float soundVolume = 0.3f;

        switch (intensity) {
            case LIGHT_SNOW:
                particleIntensity = 0.3f;
                particleCount = 5;
                soundVolume = 0.2f;
                break;
            case MODERATE_SNOW:
                particleIntensity = 0.5f;
                particleCount = 10;
                soundVolume = 0.3f;
                break;
            case HEAVY_SNOW:
                particleIntensity = 0.8f;
                particleCount = 20;
                soundVolume = 0.4f;
                break;
            case BLIZZARD:
                particleIntensity = 1.0f;
                particleCount = 30;
                soundVolume = 0.6f;
                break;
            default:
                break;
        }

        // Créer un effet de particules de neige
        ParticleEffect snowParticles = new ParticleEffect(
                getPlugin(),
                intensity,
                colored ? Particle.DUST : Particle.SNOWFLAKE,
                particleIntensity,
                24000, // Dure une journée Minecraft entière
                particleCount,
                20.0, // Rayon de 20 blocs
                15.0, // Hauteur au-dessus du joueur
                0.05f // Vitesse des particules
        );

        // Créer un effet sonore de neige
        SoundEffect snowSound = new SoundEffect(
                getPlugin(),
                intensity,
                Sound.BLOCK_SNOW_PLACE,
                particleIntensity,
                24000, // Dure une journée Minecraft entière
                soundVolume,
                1.0f, // Hauteur normale
                60 // Jouer toutes les 3 secondes
        );

        // Ajouter les effets à cette météo
        addEffect(snowParticles);
        addEffect(snowSound);

        // Effet de ralentissement pour la neige
        if (intensity == WeatherType.MODERATE_SNOW || intensity == WeatherType.HEAVY_SNOW || blizzard) {
            addPotionEffect(PotionEffectType.SLOWNESS, blizzard ? 1 : 0, 100, true);
        }

        // Effet de tempête pour le blizzard
        if (blizzard) {
            // Ajouter un effet de vent
            SoundEffect windSound = new SoundEffect(
                    getPlugin(),
                    intensity,
                    Sound.ENTITY_PHANTOM_AMBIENT,
                    1.0f,
                    24000, // Dure une journée Minecraft entière
                    0.8f,
                    0.5f, // Plus grave pour simuler le vent
                    40 // Jouer toutes les 2 secondes
            );

            addEffect(windSound);

            // Ajouter un effet de dégâts pour le blizzard
            addDamageEffect(1.0, 200); // 0.5 cœur toutes les 10 secondes
        }

        // Effet de transformation des blocs en neige
        if (intensity == WeatherType.HEAVY_SNOW || blizzard) {
            BlockEffect snowCover = new BlockEffect(
                    getPlugin(),
                    intensity,
                    particleIntensity,
                    24000, // Dure une journée Minecraft entière
                    15, // Rayon de 15 blocs
                    100, // Appliquer toutes les 5 secondes
                    Material.GRASS_BLOCK, // Affecte les blocs d'herbe
                    Material.SNOW_BLOCK // Transforme en bloc de neige
            );

            addEffect(snowCover);
        }
    }

    /**
     * Vérifie si cette neige est colorée.
     *
     * @return true si la neige est colorée, false sinon.
     */
    public boolean isColored() {
        return colored;
    }

    /**
     * Vérifie si c'est un blizzard.
     *
     * @return true si c'est un blizzard, false sinon.
     */
    public boolean isBlizzard() {
        return blizzard;
    }

    /**
     * Obtient l'intensité de la neige.
     *
     * @return Le type de météo représentant l'intensité.
     */
    public WeatherType getIntensity() {
        return intensity;
    }

    @Override
    public void applyToWorld(World world) {
        super.applyToWorld(world);

        // Activer la pluie dans Minecraft pour simuler la neige
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