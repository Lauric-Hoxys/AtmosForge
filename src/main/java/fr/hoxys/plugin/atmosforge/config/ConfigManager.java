package fr.hoxys.plugin.atmosforge.config;

import fr.hoxys.plugin.atmosforge.Main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de configuration pour AtmosForge.
 */
public class ConfigManager {

    private final Main plugin;
    private final Logger logger;

    private FileConfiguration config;
    private File configFile;

    /**
     * Constructeur du gestionnaire de configuration.
     *
     * @param plugin L'instance du plugin principal.
     */
    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        // S'assurer que le dossier du plugin existe
        if (!plugin.getDataFolder().exists()) {
            boolean created = plugin.getDataFolder().mkdirs();
            if (!created) {
                logger.warning("Failed to create plugin data folder!");
            }
        }

        // Initialiser le fichier de configuration
        configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    /**
     * Charge la configuration du plugin.
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            // Si le fichier n'existe pas, le créer à partir des ressources par défaut
            plugin.saveResource("config.yml", false);
            logger.info("Created default configuration file.");
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Comparer avec les valeurs par défaut et ajouter les clés manquantes
        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));

            for (String key : defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defaultConfig.get(key));
                    logger.info("Added missing config key: " + key);
                }
            }

            // Sauvegarder les modifications
            try {
                config.save(configFile);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not save updated config to " + configFile, e);
            }
        }

        logger.info("Configuration loaded successfully.");
    }

    /**
     * Recharge la configuration du plugin.
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        // Recharger les valeurs par défaut
        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            config.setDefaults(defaultConfig);
        }

        logger.info("Configuration reloaded successfully.");
    }

    /**
     * Sauvegarde la configuration du plugin.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
            logger.info("Configuration saved successfully.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    /**
     * Obtient la configuration du plugin.
     *
     * @return La configuration du plugin.
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    /**
     * Vérifie si un monde est activé pour AtmosForge.
     *
     * @param worldName Le nom du monde à vérifier.
     * @return true si le monde est activé, false sinon.
     */
    public boolean isWorldEnabled(String worldName) {
        List<String> enabledWorlds = getConfig().getStringList("enabled_worlds");

        // Si la liste est vide ou contient "all", tous les mondes sont activés
        if (enabledWorlds.isEmpty() || enabledWorlds.contains("all")) {
            return true;
        }

        // Vérifier si le monde est dans la liste des mondes activés
        return enabledWorlds.contains(worldName);
    }

    /**
     * Obtient la liste des mondes activés pour AtmosForge.
     *
     * @return La liste des noms de mondes activés.
     */
    public List<String> getEnabledWorlds() {
        List<String> enabledWorlds = getConfig().getStringList("enabled_worlds");

        // Si la liste est vide ou contient "all", renvoyer une liste vide (tous les mondes sont activés)
        if (enabledWorlds.isEmpty() || enabledWorlds.contains("all")) {
            return new ArrayList<>();
        }

        return enabledWorlds;
    }

    /**
     * Définit les mondes activés pour AtmosForge.
     *
     * @param enabledWorlds La liste des noms de mondes à activer.
     */
    public void setEnabledWorlds(List<String> enabledWorlds) {
        getConfig().set("enabled_worlds", enabledWorlds);
        saveConfig();
    }

    /**
     * Active ou désactive un monde pour AtmosForge.
     *
     * @param worldName Le nom du monde à activer/désactiver.
     * @param enabled true pour activer, false pour désactiver.
     */
    public void setWorldEnabled(String worldName, boolean enabled) {
        List<String> enabledWorlds = getEnabledWorlds();

        // Si tous les mondes sont activés (liste vide)
        if (enabledWorlds.isEmpty()) {
            if (!enabled) {
                // Si on veut désactiver ce monde, créer une liste de tous les mondes sauf celui-ci
                List<String> allWorlds = new ArrayList<>();
                for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                    if (!world.getName().equals(worldName)) {
                        allWorlds.add(world.getName());
                    }
                }
                setEnabledWorlds(allWorlds);
            }
            // Si on veut l'activer, ne rien faire car tous les mondes sont déjà activés
        } else {
            // Liste spécifique de mondes activés
            if (enabled && !enabledWorlds.contains(worldName)) {
                enabledWorlds.add(worldName);
                setEnabledWorlds(enabledWorlds);
            } else if (!enabled && enabledWorlds.contains(worldName)) {
                enabledWorlds.remove(worldName);
                setEnabledWorlds(enabledWorlds);
            }
        }
    }
}