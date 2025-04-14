package fr.hoxys.plugin.atmosforge.managers;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.WeatherType;
import fr.hoxys.plugin.atmosforge.models.effects.WeatherEffect;
import fr.hoxys.plugin.atmosforge.models.effects.ParticleEffect;
import fr.hoxys.plugin.atmosforge.models.effects.SoundEffect;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Gestionnaire des effets météorologiques pour les joueurs et le monde.
 */
public class EffectManager {

    private final Main plugin;
    private final Logger logger;

    // Tâches d'effets par monde
    private final Map<UUID, Set<BukkitTask>> worldEffectTasks;

    // Intervalles de dégâts en ticks
    private int frostbiteDamageInterval;
    private int solderingIronDamageInterval;

    public EffectManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.worldEffectTasks = new HashMap<>();

        // Charger la configuration
        loadConfiguration();
    }

    /**
     * Charge la configuration du gestionnaire.
     */
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        frostbiteDamageInterval = config.getInt("effects.damage_intervals.frostbite", 100); // 5 secondes par défaut
        solderingIronDamageInterval = config.getInt("effects.damage_intervals.soldering_iron", 80); // 4 secondes par défaut

        logger.info("Frostbite damage interval set to: " + frostbiteDamageInterval + " ticks");
        logger.info("Soldering Iron damage interval set to: " + solderingIronDamageInterval + " ticks");
    }

    /**
     * Applique les effets météorologiques à un monde.
     *
     * @param world Le monde auquel appliquer les effets.
     * @param weatherType Le type de météo.
     */
    public void applyWeatherEffects(World world, WeatherType weatherType) {
        UUID worldId = world.getUID();

        // Annuler les tâches d'effets précédentes pour ce monde
        cancelWorldEffects(world);

        // Si c'est le cycle de nuit, aucun effet n'est appliqué
        if (weatherType == WeatherType.NIGHT_CYCLE) {
            logger.fine("Night cycle active for world " + world.getName() + ", no effects applied");
            return;
        }

        // Créer un ensemble pour stocker les nouvelles tâches
        Set<BukkitTask> tasks = new HashSet<>();

        // Appliquer les effets de particules si disponibles
        applyParticleEffects(world, weatherType, tasks);

        // Appliquer les effets sonores si disponibles
        applySoundEffects(world, weatherType, tasks);

        // Appliquer les effets de potion/statut aux joueurs
        applyPlayerEffects(world, weatherType, tasks);

        // Stocker les tâches créées
        worldEffectTasks.put(worldId, tasks);

        logger.fine("Applied " + tasks.size() + " weather effects for " + weatherType.getId() + " in world " + world.getName());
    }

    /**
     * Applique les effets de particules pour un type de météo.
     *
     * @param world Le monde auquel appliquer les effets.
     * @param weatherType Le type de météo.
     * @param tasks L'ensemble de tâches à mettre à jour.
     */
    private void applyParticleEffects(World world, WeatherType weatherType, Set<BukkitTask> tasks) {
        // Déterminer quels effets de particules appliquer en fonction du type de météo
        switch (weatherType) {
            case LIGHT_RAIN:
            case MODERATE_RAIN:
            case HEAVY_RAIN:
            case DRIZZLE:
            case SHOWER:
                // Particules de pluie avec intensité variable
                BukkitTask rainTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Créer des particules de pluie autour du joueur
                        // L'intensité dépend du type de météo
                        int intensity = 5; // Par défaut pour LIGHT_RAIN
                        if (weatherType == WeatherType.MODERATE_RAIN) intensity = 10;
                        else if (weatherType == WeatherType.HEAVY_RAIN || weatherType == WeatherType.SHOWER) intensity = 20;
                        else if (weatherType == WeatherType.DRIZZLE) intensity = 3;

                        // Utiliser ProtocolLib pour les effets de particules personnalisés si disponible
                        if (plugin.isProtocolLibEnabled()) {
                            spawnCustomRainParticles(player, intensity);
                        }
                    }
                }, 0L, 5L); // Exécuter toutes les 5 ticks (1/4 de seconde)
                tasks.add(rainTask);
                break;

            case LIGHT_SNOW:
            case MODERATE_SNOW:
            case HEAVY_SNOW:
            case BLIZZARD:
                // Particules de neige avec intensité variable
                BukkitTask snowTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Créer des particules de neige autour du joueur
                        // L'intensité dépend du type de météo
                        int intensity = 5; // Par défaut pour LIGHT_SNOW
                        if (weatherType == WeatherType.MODERATE_SNOW) intensity = 10;
                        else if (weatherType == WeatherType.HEAVY_SNOW) intensity = 15;
                        else if (weatherType == WeatherType.BLIZZARD) intensity = 25;

                        // Utiliser ProtocolLib pour les effets de particules personnalisés si disponible
                        if (plugin.isProtocolLibEnabled()) {
                            spawnCustomSnowParticles(player, intensity);
                        }
                    }
                }, 0L, 5L);
                tasks.add(snowTask);
                break;

            case FOG:
            case MIST:
            case FREEZING_FOG:
                // Particules de brouillard
                BukkitTask fogTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Créer des particules de brouillard autour du joueur
                        if (plugin.isProtocolLibEnabled()) {
                            spawnFogParticles(player);
                        }
                    }
                }, 0L, 10L);
                tasks.add(fogTask);
                break;

            // Ajouter d'autres cas pour les différents types de météo

            default:
                // Aucun effet de particule pour les autres types de météo
                break;
        }
    }

    /**
     * Applique les effets sonores pour un type de météo.
     *
     * @param world Le monde auquel appliquer les effets.
     * @param weatherType Le type de météo.
     * @param tasks L'ensemble de tâches à mettre à jour.
     */
    private void applySoundEffects(World world, WeatherType weatherType, Set<BukkitTask> tasks) {
        // Déterminer quels effets sonores appliquer en fonction du type de météo
        switch (weatherType) {
            case LIGHT_RAIN:
            case MODERATE_RAIN:
            case HEAVY_RAIN:
            case DRIZZLE:
            case SHOWER:
                // Sons de pluie avec intensité variable
                BukkitTask rainSoundTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Jouer un son de pluie
                        float volume = 0.2f; // Par défaut pour LIGHT_RAIN
                        if (weatherType == WeatherType.MODERATE_RAIN) volume = 0.4f;
                        else if (weatherType == WeatherType.HEAVY_RAIN || weatherType == WeatherType.SHOWER) volume = 0.8f;
                        else if (weatherType == WeatherType.DRIZZLE) volume = 0.1f;

                        player.playSound(player.getLocation(), org.bukkit.Sound.WEATHER_RAIN, volume, 1.0f);
                    }
                }, 0L, 40L); // Toutes les 2 secondes
                tasks.add(rainSoundTask);
                break;

            case THUNDERSTORM:
            case LIGHTNING:
            case THUNDER:
                // Sons d'orage aléatoires
                BukkitTask thunderSoundTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    // Jouer un son de tonnerre aléatoirement
                    if (Math.random() < 0.1) { // 10% de chance chaque fois
                        for (Player player : world.getPlayers()) {
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                        }
                    }
                }, 0L, 100L); // Toutes les 5 secondes
                tasks.add(thunderSoundTask);
                break;

            case STRONG_WIND:
            case GUST:
            case STORM:
            case HURRICANE:
            case TORNADO:
                // Sons de vent
                BukkitTask windSoundTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Jouer un son de vent
                        float volume = 0.3f; // Par défaut pour STRONG_WIND
                        if (weatherType == WeatherType.STORM || weatherType == WeatherType.GUST) volume = 0.6f;
                        else if (weatherType == WeatherType.HURRICANE || weatherType == WeatherType.TORNADO) volume = 1.0f;

                        // Utiliser un son qui ressemble au vent
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PHANTOM_AMBIENT, volume, 0.5f);
                    }
                }, 0L, 60L); // Toutes les 3 secondes
                tasks.add(windSoundTask);
                break;

            // Ajouter d'autres cas pour les différents types de météo

            default:
                // Aucun effet sonore pour les autres types de météo
                break;
        }
    }

    /**
     * Applique les effets aux joueurs pour un type de météo.
     *
     * @param world Le monde auquel appliquer les effets.
     * @param weatherType Le type de météo.
     * @param tasks L'ensemble de tâches à mettre à jour.
     */
    private void applyPlayerEffects(World world, WeatherType weatherType, Set<BukkitTask> tasks) {
        // Déterminer quels effets de joueur appliquer en fonction du type de météo
        switch (weatherType) {
            case HEAT_WAVE:
            case HEATSTROKE:
                // Effet de chaleur extrême - dommages périodiques "Soldering Iron"
                BukkitTask heatDamageTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Vérifier si le joueur est exposé au ciel (et donc à la chaleur)
                        if (isPlayerExposedToSky(player)) {
                            // Appliquer les effets de chaleur
                            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0));

                            // Dommages de chaleur "Soldering Iron" si c'est une canicule
                            if (weatherType == WeatherType.HEATSTROKE) {
                                player.damage(1.0); // 0.5 cœur de dommage
                                player.sendMessage(plugin.getLanguageManager().getMessage("effects.heat_damage"));
                            }
                        }
                    }
                }, 0L, solderingIronDamageInterval);
                tasks.add(heatDamageTask);
                break;

            case COLD_WAVE:
            case BLIZZARD:
                // Effet de froid extrême - dommages périodiques "Frostbite"
                BukkitTask coldDamageTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Vérifier si le joueur est exposé au ciel (et donc au froid)
                        if (isPlayerExposedToSky(player)) {
                            // Appliquer les effets de froid
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));

                            // Dommages de froid "Frostbite"
                            if (weatherType == WeatherType.BLIZZARD) {
                                player.damage(1.0); // 0.5 cœur de dommage
                                player.sendMessage(plugin.getLanguageManager().getMessage("effects.cold_damage"));
                            }
                        }
                    }
                }, 0L, frostbiteDamageInterval);
                tasks.add(coldDamageTask);
                break;

            case TORNADO:
            case HURRICANE:
                // Effet de vent extrême - mouvement aléatoire et dommages
                BukkitTask windEffectTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Vérifier si le joueur est exposé au ciel (et donc au vent)
                        if (isPlayerExposedToSky(player)) {
                            // Appliquer une force aléatoire au joueur (comme s'il était soufflé par le vent)
                            double strength = 0.5;
                            if (weatherType == WeatherType.TORNADO) strength = 1.0;

                            // Direction aléatoire
                            double angle = Math.random() * 2 * Math.PI;
                            double x = Math.cos(angle) * strength;
                            double z = Math.sin(angle) * strength;

                            player.setVelocity(player.getVelocity().add(org.bukkit.util.Vector.fromConfiguration(
                                    new org.bukkit.configuration.MemoryConfiguration()
                                            .set("x", x)
                                            .set("y", 0.2)
                                            .set("z", z)
                            )));

                            // Dommages si c'est une tornade
                            if (weatherType == WeatherType.TORNADO && Math.random() < 0.2) { // 20% de chance
                                player.damage(2.0); // 1 cœur de dommage
                                player.sendMessage(plugin.getLanguageManager().getMessage("effects.wind_damage"));
                            }
                        }
                    }
                }, 0L, 20L); // Toutes les secondes
                tasks.add(windEffectTask);
                break;

            // Ajouter d'autres cas pour les différents types de météo

            default:
                // Aucun effet de joueur spécifique pour les autres types de météo
                break;
        }
    }

    /**
     * Vérifie si un joueur est directement exposé au ciel (pas sous un abri).
     *
     * @param player Le joueur à vérifier.
     * @return true si le joueur est exposé, false sinon.
     */
    private boolean isPlayerExposedToSky(Player player) {
        return player.getWorld().getHighestBlockYAt(player.getLocation()) <= player.getLocation().getY();
    }

    /**
     * Annule tous les effets météorologiques en cours pour un monde.
     *
     * @param world Le monde pour lequel annuler les effets.
     */
    public void cancelWorldEffects(World world) {
        UUID worldId = world.getUID();
        if (worldEffectTasks.containsKey(worldId)) {
            Set<BukkitTask> tasks = worldEffectTasks.get(worldId);
            for (BukkitTask task : tasks) {
                task.cancel();
            }
            tasks.clear();
            worldEffectTasks.remove(worldId);

            logger.fine("Cancelled all weather effects for world " + world.getName());
        }
    }

    /**
     * Annule tous les effets météorologiques en cours pour tous les mondes.
     */
    public void cancelAllEffects() {
        for (Set<BukkitTask> tasks : worldEffectTasks.values()) {
            for (BukkitTask task : tasks) {
                task.cancel();
            }
            tasks.clear();
        }
        worldEffectTasks.clear();

        logger.fine("Cancelled all weather effects for all worlds");
    }

    /**
     * Crée des particules de pluie personnalisées autour d'un joueur.
     * Nécessite ProtocolLib.
     *
     * @param player Le joueur autour duquel créer les particules.
     * @param intensity L'intensité des particules.
     */
    private void spawnCustomRainParticles(Player player, int intensity) {
        // Cette méthode utiliserait ProtocolLib pour envoyer des paquets de particules personnalisés
        // Ceci est une implémentation simplifiée
        if (plugin.isProtocolLibEnabled()) {
            // Mise en œuvre spécifique pour ProtocolLib ici
            // En attendant, utiliser les particules standard de Bukkit
            org.bukkit.Location loc = player.getLocation();
            for (int i = 0; i < intensity; i++) {
                double x = loc.getX() + (Math.random() * 10 - 5);
                double y = loc.getY() + 10;
                double z = loc.getZ() + (Math.random() * 10 - 5);
                player.getWorld().spawnParticle(Particle.FALLING_DRIPSTONE_WATER, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Crée des particules de neige personnalisées autour d'un joueur.
     * Nécessite ProtocolLib.
     *
     * @param player Le joueur autour duquel créer les particules.
     * @param intensity L'intensité des particules.
     */
    private void spawnCustomSnowParticles(Player player, int intensity) {
        // Cette méthode utiliserait ProtocolLib pour envoyer des paquets de particules personnalisés
        // Ceci est une implémentation simplifiée
        if (plugin.isProtocolLibEnabled()) {
            // Mise en œuvre spécifique pour ProtocolLib ici
            // En attendant, utiliser les particules standard de Bukkit
            org.bukkit.Location loc = player.getLocation();
            for (int i = 0; i < intensity; i++) {
                double x = loc.getX() + (Math.random() * 10 - 5);
                double y = loc.getY() + 10;
                double z = loc.getZ() + (Math.random() * 10 - 5);
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Crée des particules de brouillard autour d'un joueur.
     * Nécessite ProtocolLib.
     *
     * @param player Le joueur autour duquel créer les particules.
     */
    private void spawnFogParticles(Player player) {
        // Cette méthode utiliserait ProtocolLib pour envoyer des paquets de particules personnalisés
        // Ceci est une implémentation simplifiée
        if (plugin.isProtocolLibEnabled()) {
            // Mise en œuvre spécifique pour ProtocolLib ici
            // En attendant, utiliser les particules standard de Bukkit
            org.bukkit.Location loc = player.getLocation();
            for (int i = 0; i < 10; i++) {
                double x = loc.getX() + (Math.random() * 20 - 10);
                double y = loc.getY() + (Math.random() * 4 - 2);
                double z = loc.getZ() + (Math.random() * 20 - 10);
                player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, x, y, z, 1, 0.5, 0.1, 0.5, 0);
            }
        }
    }

    /**
     * Obtient l'intervalle de dégâts de gelure.
     *
     * @return L'intervalle de dégâts de gelure en ticks.
     */
    public int getFrostbiteDamageInterval() {
        return frostbiteDamageInterval;
    }

    /**
     * Définit l'intervalle de dégâts de gelure.
     *
     * @param interval Le nouvel intervalle en ticks.
     */
    public void setFrostbiteDamageInterval(int interval) {
        if (interval > 0) {
            this.frostbiteDamageInterval = interval;

            // Mettre à jour la configuration
            FileConfiguration config = plugin.getConfigManager().getConfig();
            config.set("effects.damage_intervals.frostbite", interval);
            plugin.getConfigManager().saveConfig();

            logger.info("Frostbite damage interval updated to: " + interval + " ticks");
        }
    }

    /**
     * Obtient l'intervalle de dégâts de chaleur.
     *
     * @return L'intervalle de dégâts de chaleur en ticks.
     */
    public int getSolderingIronDamageInterval() {
        return solderingIronDamageInterval;
    }

    /**
     * Définit l'intervalle de dégâts de chaleur.
     *
     * @param interval Le nouvel intervalle en ticks.
     */
    public void setSolderingIronDamageInterval(int interval) {
        if (interval > 0) {
            this.solderingIronDamageInterval = interval;

            // Mettre à jour la configuration
            FileConfiguration config = plugin.getConfigManager().getConfig();
            config.set("effects.damage_intervals.soldering_iron", interval);
            plugin.getConfigManager().saveConfig();

            logger.info("Soldering Iron damage interval updated to: " + interval + " ticks");
        }
    }
}