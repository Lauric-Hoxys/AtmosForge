package fr.hoxys.plugin.atmosforge.managers;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Gestionnaire du temps pour traiter les cycles jour/nuit et avancer les saisons.
 */
public class TimeManager {

    private final Main plugin;
    private final Logger logger;

    // Tâche principale de cycle de temps
    private BukkitTask timeCycleTask;

    // Stocke le dernier jour Minecraft connu pour chaque monde
    private final Map<UUID, Long> lastKnownDays;

    // Stocke si le cycle de nuit est actif pour chaque monde
    private final Map<UUID, Boolean> nightCycleActive;

    // Intervalle de vérification du temps en ticks
    private int timeCheckInterval;

    public TimeManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.lastKnownDays = new HashMap<>();
        this.nightCycleActive = new HashMap<>();

        // Charger la configuration
        loadConfiguration();
    }

    /**
     * Charge la configuration du gestionnaire.
     */
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        timeCheckInterval = config.getInt("time.check_interval", 200); // 10 secondes par défaut

        logger.info("Time check interval set to: " + timeCheckInterval + " ticks");
    }

    /**
     * Démarre le cycle de temps principal.
     */
    public void startTimeCycle() {
        // Initialiser le dernier jour connu pour tous les mondes
        for (World world : Bukkit.getWorlds()) {
            if (plugin.getConfigManager().isWorldEnabled(world.getName())) {
                lastKnownDays.put(world.getUID(), getCurrentMinecraftDay(world));
                nightCycleActive.put(world.getUID(), isNightTime(world));
            }
        }

        // Démarrer la tâche de surveillance du temps
        timeCycleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkTimeChanges, 0L, timeCheckInterval);

        logger.info("Time cycle started with interval: " + timeCheckInterval + " ticks");
    }

    /**
     * Arrête le cycle de temps.
     */
    public void stopTimeCycle() {
        if (timeCycleTask != null) {
            timeCycleTask.cancel();
            timeCycleTask = null;

            logger.info("Time cycle stopped");
        }
    }

    /**
     * Vérifie les changements de temps pour tous les mondes activés.
     */
    private void checkTimeChanges() {
        for (World world : Bukkit.getWorlds()) {
            if (plugin.getConfigManager().isWorldEnabled(world.getName())) {
                UUID worldId = world.getUID();

                // Vérifier le jour actuel
                long currentDay = getCurrentMinecraftDay(world);
                long lastDay = lastKnownDays.getOrDefault(worldId, 0L);

                // Si le jour a changé, mettre à jour les saisons et la météo
                if (currentDay > lastDay) {
                    // Le jour a avancé
                    logger.fine("Day changed in world " + world.getName() + ": " + lastDay + " -> " + currentDay);

                    // Avancer la saison d'un jour
                    plugin.getSeasonManager().advanceDay(world);

                    // Tenter de changer la météo en fonction de la chance configurée
                    plugin.getWeatherManager().tryChangeWeather(world);

                    // Mettre à jour le dernier jour connu
                    lastKnownDays.put(worldId, currentDay);
                }

                // Vérifier si c'est la nuit ou le jour
                boolean isNight = isNightTime(world);
                boolean wasNight = nightCycleActive.getOrDefault(worldId, false);

                // Si le statut jour/nuit a changé
                if (isNight != wasNight) {
                    if (isNight) {
                        // Passage au cycle de nuit
                        logger.fine("Night cycle started in world " + world.getName());
                        activateNightCycle(world);
                    } else {
                        // Retour au jour, restaurer la météo précédente
                        logger.fine("Day cycle resumed in world " + world.getName());
                        deactivateNightCycle(world);
                    }

                    // Mettre à jour le statut de nuit
                    nightCycleActive.put(worldId, isNight);
                }
            }
        }
    }

    /**
     * Calcule le jour Minecraft actuel pour un monde.
     * Un jour Minecraft complet est de 24000 ticks.
     *
     * @param world Le monde pour lequel calculer le jour.
     * @return Le jour actuel.
     */
    private long getCurrentMinecraftDay(World world) {
        return world.getFullTime() / 24000L;
    }

    /**
     * Vérifie s'il fait nuit dans un monde Minecraft.
     * La nuit dans Minecraft est approximativement entre 13000 et 23000 ticks.
     *
     * @param world Le monde à vérifier.
     * @return true s'il fait nuit, false sinon.
     */
    private boolean isNightTime(World world) {
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }

    /**
     * Active le cycle de nuit pour un monde.
     * Sauvegarde la météo actuelle et passe à la météo NIGHT_CYCLE.
     *
     * @param world Le monde pour lequel activer le cycle de nuit.
     */
    private void activateNightCycle(World world) {
        // Sauvegarder la météo actuelle pour la restaurer plus tard
        WeatherType currentWeather = plugin.getWeatherManager().getCurrentWeather(world);

        // Stocker la météo actuelle dans les données du monde
        Map<String, Object> worldData = plugin.getWorldManager().getWorldData(world.getName());
        if (worldData == null) {
            worldData = new HashMap<>();
        }
        worldData.put("previous_weather", currentWeather.getId());
        plugin.getWorldManager().saveWorldData(world.getName(), worldData);

        // Passer à la météo NIGHT_CYCLE
        plugin.getWeatherManager().setWeather(world, WeatherType.NIGHT_CYCLE);

        logger.fine("Switched to night cycle for world " + world.getName() + ", saved previous weather: " + currentWeather.getId());
    }

    /**
     * Désactive le cycle de nuit pour un monde.
     * Restaure la météo précédente.
     *
     * @param world Le monde pour lequel désactiver le cycle de nuit.
     */
    private void deactivateNightCycle(World world) {
        // Récupérer la météo précédente
        Map<String, Object> worldData = plugin.getWorldManager().getWorldData(world.getName());

        if (worldData != null && worldData.containsKey("previous_weather")) {
            String previousWeatherId = (String) worldData.get("previous_weather");
            WeatherType previousWeather = WeatherType.fromId(previousWeatherId);

            if (previousWeather != null) {
                // Restaurer la météo précédente
                plugin.getWeatherManager().setWeather(world, previousWeather);

                logger.fine("Night cycle ended for world " + world.getName() + ", restored weather: " + previousWeather.getId());
            } else {
                // Si la météo précédente est invalide, définir une météo aléatoire
                plugin.getWeatherManager().setRandomWeatherForSeason(
                        world, plugin.getSeasonManager().getCurrentSeason(world));

                logger.fine("Night cycle ended for world " + world.getName() + ", set random weather");
            }

            // Supprimer la météo précédente sauvegardée
            worldData.remove("previous_weather");
            plugin.getWorldManager().saveWorldData(world.getName(), worldData);
        } else {
            // Si aucune météo précédente n'est trouvée, définir une météo aléatoire
            plugin.getWeatherManager().setRandomWeatherForSeason(
                    world, plugin.getSeasonManager().getCurrentSeason(world));

            logger.fine("Night cycle ended for world " + world.getName() + ", no previous weather found, set random weather");
        }
    }

    /**
     * Obtient l'intervalle de vérification du temps.
     *
     * @return L'intervalle de vérification du temps en ticks.
     */
    public int getTimeCheckInterval() {
        return timeCheckInterval;
    }

    /**
     * Définit l'intervalle de vérification du temps.
     *
     * @param interval Le nouvel intervalle en ticks.
     */
    public void setTimeCheckInterval(int interval) {
        if (interval > 0) {
            this.timeCheckInterval = interval;

            // Mettre à jour la configuration
            FileConfiguration config = plugin.getConfigManager().getConfig();
            config.set("time.check_interval", interval);
            plugin.getConfigManager().saveConfig();

            // Redémarrer la tâche avec le nouvel intervalle
            if (timeCycleTask != null) {
                timeCycleTask.cancel();
                timeCycleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkTimeChanges, 0L, interval);
            }

            logger.info("Time check interval updated to: " + interval + " ticks");
        }
    }

    /**
     * Vérifie si le cycle de nuit est actuellement actif pour un monde.
     *
     * @param world Le monde à vérifier.
     * @return true si le cycle de nuit est actif, false sinon.
     */
    public boolean isNightCycleActive(World world) {
        UUID worldId = world.getUID();
        return nightCycleActive.getOrDefault(worldId, false);
    }

    /**
     * Force le jour ou la nuit dans un monde.
     *
     * @param world Le monde dans lequel forcer le temps.
     * @param day true pour forcer le jour, false pour forcer la nuit.
     */
    public void forceTimeOfDay(World world, boolean day) {
        long newTime = day ? 1000L : 14000L; // 1000 = matin, 14000 = début de nuit
        world.setTime(newTime);

        // Mettre à jour le statut de nuit
        UUID worldId = world.getUID();
        boolean isNight = !day;
        boolean wasNight = nightCycleActive.getOrDefault(worldId, false);

        // Appliquer les changements si nécessaire
        if (isNight != wasNight) {
            if (isNight) {
                activateNightCycle(world);
            } else {
                deactivateNightCycle(world);
            }
            nightCycleActive.put(worldId, isNight);
        }

        logger.info("Forced " + (day ? "day" : "night") + " for world " + world.getName());
    }
}