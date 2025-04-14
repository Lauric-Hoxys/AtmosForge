package fr.hoxys.plugin.atmosforge.config;

import fr.hoxys.plugin.atmosforge.Main;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire des données spécifiques à chaque monde.
 */
public class WorldManager {

    private final Main plugin;
    private final Logger logger;

    private FileConfiguration worldsConfig;
    private File worldsFile;
    private Map<String, Map<String, Object>> worldsData;

    /**
     * Constructeur du gestionnaire de monde.
     *
     * @param plugin L'instance du plugin principal.
     */
    public WorldManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.worldsData = new HashMap<>();

        // S'assurer que le dossier du plugin existe
        if (!plugin.getDataFolder().exists()) {
            boolean created = plugin.getDataFolder().mkdirs();
            if (!created) {
                logger.warning("Failed to create plugin data folder!");
            }
        }

        // Initialiser le fichier de données de monde
        worldsFile = new File(plugin.getDataFolder(), "worlds_data.yml");
    }

    /**
     * Charge les données de tous les mondes.
     */
    public void loadWorldData() {
        if (!worldsFile.exists()) {
            try {
                boolean created = worldsFile.createNewFile();
                if (!created) {
                    logger.warning("Failed to create worlds data file!");
                }
                worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
                worldsConfig.save(worldsFile);
                logger.info("Created empty worlds data file.");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not create worlds data file", e);
                return;
            }
        } else {
            worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
        }

        // Charger les données pour chaque monde
        worldsData.clear();
        ConfigurationSection worldsSection = worldsConfig.getConfigurationSection("worlds");

        if (worldsSection != null) {
            for (String worldName : worldsSection.getKeys(false)) {
                ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
                if (worldSection != null) {
                    Map<String, Object> worldData = new HashMap<>();

                    for (String key : worldSection.getKeys(false)) {
                        worldData.put(key, worldSection.get(key));
                    }

                    worldsData.put(worldName, worldData);
                }
            }
        }

        logger.info("Loaded data for " + worldsData.size() + " worlds.");
    }

    /**
     * Sauvegarde les données de tous les mondes.
     */
    public void saveAllWorldData() {
        if (worldsConfig == null) {
            loadWorldData();
        }

        // Effacer les données actuelles
        worldsConfig.set("worlds", null);

        // Sauvegarder les données pour chaque monde
        for (Map.Entry<String, Map<String, Object>> entry : worldsData.entrySet()) {
            String worldName = entry.getKey();
            Map<String, Object> worldData = entry.getValue();

            for (Map.Entry<String, Object> dataEntry : worldData.entrySet()) {
                worldsConfig.set("worlds." + worldName + "." + dataEntry.getKey(), dataEntry.getValue());
            }
        }

        try {
            worldsConfig.save(worldsFile);
            logger.info("Saved data for " + worldsData.size() + " worlds.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save worlds data to " + worldsFile, e);
        }
    }

    /**
     * Obtient les données pour un monde spécifique.
     *
     * @param worldName Le nom du monde.
     * @return Les données du monde, ou null si aucune n'est trouvée.
     */
    public Map<String, Object> getWorldData(String worldName) {
        return worldsData.get(worldName);
    }

    /**
     * Sauvegarde les données pour un monde spécifique.
     *
     * @param worldName Le nom du monde.
     * @param data Les données à sauvegarder.
     */
    public void saveWorldData(String worldName, Map<String, Object> data) {
        worldsData.put(worldName, data);

        // Sauvegarder immédiatement dans le fichier
        if (worldsConfig == null) {
            loadWorldData();
        }

        // Effacer les données actuelles pour ce monde
        worldsConfig.set("worlds." + worldName, null);

        // Sauvegarder les nouvelles données
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            worldsConfig.set("worlds." + worldName + "." + entry.getKey(), entry.getValue());
        }

        try {
            worldsConfig.save(worldsFile);
            logger.fine("Saved data for world " + worldName);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save world data for " + worldName, e);
        }
    }

    /**
     * Supprime les données pour un monde spécifique.
     *
     * @param worldName Le nom du monde.
     */
    public void removeWorldData(String worldName) {
        worldsData.remove(worldName);

        // Supprimer immédiatement du fichier
        if (worldsConfig == null) {
            loadWorldData();
        }

        worldsConfig.set("worlds." + worldName, null);

        try {
            worldsConfig.save(worldsFile);
            logger.info("Removed data for world " + worldName);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not remove world data for " + worldName, e);
        }
    }

    /**
     * Obtient une valeur spécifique pour un monde.
     *
     * @param worldName Le nom du monde.
     * @param key La clé de la valeur à obtenir.
     * @return La valeur associée à la clé, ou null si non trouvée.
     */
    public Object getWorldValue(String worldName, String key) {
        Map<String, Object> worldData = getWorldData(worldName);
        if (worldData != null) {
            return worldData.get(key);
        }
        return null;
    }

    /**
     * Définit une valeur spécifique pour un monde.
     *
     * @param worldName Le nom du monde.
     * @param key La clé de la valeur à définir.
     * @param value La valeur à définir.
     */
    public void setWorldValue(String worldName, String key, Object value) {
        Map<String, Object> worldData = getWorldData(worldName);
        if (worldData == null) {
            worldData = new HashMap<>();
            worldsData.put(worldName, worldData);
        }

        worldData.put(key, value);

        // Sauvegarder immédiatement
        if (worldsConfig == null) {
            loadWorldData();
        }

        worldsConfig.set("worlds." + worldName + "." + key, value);

        try {
            worldsConfig.save(worldsFile);
            logger.fine("Set " + key + " = " + value + " for world " + worldName);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save world value for " + worldName, e);
        }
    }
}