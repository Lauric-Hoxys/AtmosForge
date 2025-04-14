package fr.hoxys.plugin.atmosforge.data;

import fr.hoxys.plugin.atmosforge.Main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de fichiers pour AtmosForge.
 * Cette classe gère la création, le chargement et la sauvegarde des fichiers de données.
 */
public class FileManager {

    private final Main plugin;
    private final Logger logger;
    private final File dataFolder;

    // Cache des configurations
    private final Map<String, FileConfiguration> configCache;
    private final Map<String, File> fileCache;

    /**
     * Constructeur du gestionnaire de fichiers.
     *
     * @param plugin L'instance du plugin principal.
     */
    public FileManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
        this.configCache = new HashMap<>();
        this.fileCache = new HashMap<>();

        // S'assurer que le dossier du plugin existe
        if (!dataFolder.exists()) {
            boolean created = dataFolder.mkdirs();
            if (!created) {
                logger.warning("Failed to create plugin data folder!");
            }
        }
    }

    /**
     * Vérifie si un fichier existe dans le dossier du plugin.
     *
     * @param fileName Le nom du fichier.
     * @return true si le fichier existe, false sinon.
     */
    public boolean fileExists(String fileName) {
        File file = new File(dataFolder, fileName);
        return file.exists();
    }

    /**
     * Crée un fichier dans le dossier du plugin.
     *
     * @param fileName Le nom du fichier.
     * @return true si le fichier a été créé avec succès, false sinon.
     */
    public boolean createFile(String fileName) {
        File file = new File(dataFolder, fileName);
        if (file.exists()) {
            return true; // Le fichier existe déjà
        }

        try {
            boolean created = file.createNewFile();
            if (created) {
                logger.info("Created file: " + fileName);
            } else {
                logger.warning("Failed to create file: " + fileName);
            }
            return created;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not create file: " + fileName, e);
            return false;
        }
    }

    /**
     * Crée un dossier dans le dossier du plugin.
     *
     * @param dirName Le nom du dossier.
     * @return true si le dossier a été créé avec succès, false sinon.
     */
    public boolean createDirectory(String dirName) {
        File dir = new File(dataFolder, dirName);
        if (dir.exists() && dir.isDirectory()) {
            return true; // Le dossier existe déjà
        }

        boolean created = dir.mkdirs();
        if (created) {
            logger.info("Created directory: " + dirName);
        } else {
            logger.warning("Failed to create directory: " + dirName);
        }
        return created;
    }

    /**
     * Obtient un fichier de configuration YAML.
     * Si le fichier n'existe pas, il sera créé.
     *
     * @param fileName Le nom du fichier.
     * @return La configuration YAML, ou null si une erreur s'est produite.
     */
    public FileConfiguration getConfig(String fileName) {
        // Vérifier si la configuration est déjà en cache
        if (configCache.containsKey(fileName)) {
            return configCache.get(fileName);
        }

        // Créer le fichier s'il n'existe pas
        if (!fileExists(fileName)) {
            createFile(fileName);
        }

        // Obtenir le fichier
        File file = new File(dataFolder, fileName);
        fileCache.put(fileName, file);

        // Charger la configuration
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configCache.put(fileName, config);

        return config;
    }

    /**
     * Sauvegarde une configuration YAML dans un fichier.
     *
     * @param fileName Le nom du fichier.
     * @return true si la sauvegarde a réussi, false sinon.
     */
    public boolean saveConfig(String fileName) {
        // Vérifier si la configuration est en cache
        if (!configCache.containsKey(fileName) || !fileCache.containsKey(fileName)) {
            logger.warning("Cannot save " + fileName + " because it's not loaded in cache.");
            return false;
        }

        FileConfiguration config = configCache.get(fileName);
        File file = fileCache.get(fileName);

        try {
            config.save(file);
            logger.fine("Saved configuration to " + fileName);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save config to " + fileName, e);
            return false;
        }
    }

    /**
     * Recharge une configuration YAML depuis un fichier.
     *
     * @param fileName Le nom du fichier.
     * @return La configuration YAML rechargée, ou null si une erreur s'est produite.
     */
    public FileConfiguration reloadConfig(String fileName) {
        // Supprimer la configuration du cache
        configCache.remove(fileName);
        fileCache.remove(fileName);

        // Charger à nouveau la configuration
        return getConfig(fileName);
    }

    /**
     * Copie un fichier de ressource interne vers le dossier du plugin.
     * Si le fichier de destination existe déjà, il ne sera pas écrasé, sauf si force est true.
     *
     * @param resourcePath Le chemin de la ressource interne.
     * @param outFileName Le nom du fichier de sortie.
     * @param force true pour écraser le fichier existant, false sinon.
     * @return true si la copie a réussi, false sinon.
     */
    public boolean copyResource(String resourcePath, String outFileName, boolean force) {
        File outFile = new File(dataFolder, outFileName);

        // Vérifier si le fichier existe déjà
        if (outFile.exists() && !force) {
            return true; // Le fichier existe déjà et on ne force pas l'écrasement
        }

        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                logger.warning("Resource not found: " + resourcePath);
                return false;
            }

            // Créer les dossiers parents si nécessaire
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) {
                boolean created = parent.mkdirs();
                if (!created) {
                    logger.warning("Failed to create parent directories for: " + outFileName);
                }
            }

            // Copier le fichier
            Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied resource " + resourcePath + " to " + outFileName);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not copy resource " + resourcePath + " to " + outFileName, e);
            return false;
        }
    }

    /**
     * Lit un fichier de ressource interne sous forme de chaîne.
     *
     * @param resourcePath Le chemin de la ressource interne.
     * @return Le contenu du fichier, ou null si une erreur s'est produite.
     */
    public String readResource(String resourcePath) {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                logger.warning("Resource not found: " + resourcePath);
                return null;
            }

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read resource: " + resourcePath, e);
            return null;
        }
    }

    /**
     * Lit un fichier sous forme de chaîne.
     *
     * @param fileName Le nom du fichier.
     * @return Le contenu du fichier, ou null si une erreur s'est produite.
     */
    public String readFile(String fileName) {
        File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            logger.warning("File not found: " + fileName);
            return null;
        }

        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read file: " + fileName, e);
            return null;
        }
    }

    /**
     * Écrit une chaîne dans un fichier.
     *
     * @param fileName Le nom du fichier.
     * @param content Le contenu à écrire.
     * @return true si l'écriture a réussi, false sinon.
     */
    public boolean writeFile(String fileName, String content) {
        File file = new File(dataFolder, fileName);

        // Créer les dossiers parents si nécessaire
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean created = parent.mkdirs();
            if (!created) {
                logger.warning("Failed to create parent directories for: " + fileName);
            }
        }

        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            logger.fine("Wrote to file: " + fileName);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not write to file: " + fileName, e);
            return false;
        }
    }

    /**
     * Supprime un fichier ou un dossier.
     *
     * @param path Le chemin du fichier ou du dossier.
     * @return true si la suppression a réussi, false sinon.
     */
    public boolean delete(String path) {
        File file = new File(dataFolder, path);
        if (!file.exists()) {
            return true; // Le fichier n'existe pas, donc on considère que la suppression est réussie
        }

        boolean deleted;
        if (file.isDirectory()) {
            // Supprimer tous les fichiers dans le dossier d'abord
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    delete(path + "/" + f.getName());
                }
            }
            deleted = file.delete();
        } else {
            deleted = file.delete();
        }

        if (deleted) {
            logger.fine("Deleted: " + path);
        } else {
            logger.warning("Failed to delete: " + path);
        }

        // Supprimer du cache si nécessaire
        if (fileCache.containsKey(path)) {
            fileCache.remove(path);
            configCache.remove(path);
        }

        return deleted;
    }

    /**
     * Liste tous les fichiers dans un répertoire.
     *
     * @param directory Le répertoire à lister.
     * @return Un tableau de noms de fichiers, ou un tableau vide si une erreur s'est produite.
     */
    public String[] listFiles(String directory) {
        File dir = new File(dataFolder, directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return new String[0];
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return new String[0];
        }

        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }

        return fileNames;
    }

    /**
     * Copie une configuration par défaut depuis les ressources si elle n'existe pas.
     *
     * @param resourcePath Le chemin de la ressource interne.
     * @param outFileName Le nom du fichier de sortie.
     * @return La configuration chargée, ou null si une erreur s'est produite.
     */
    public FileConfiguration getDefaultConfig(String resourcePath, String outFileName) {
        // Copier la configuration par défaut si nécessaire
        if (!fileExists(outFileName)) {
            copyResource(resourcePath, outFileName, false);
        }

        // Charger la configuration
        FileConfiguration config = getConfig(outFileName);

        // Vérifier les clés manquantes par rapport à la configuration par défaut
        try (InputStream defaultConfigStream = plugin.getResource(resourcePath)) {
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));

                for (String key : defaultConfig.getKeys(true)) {
                    if (!config.contains(key)) {
                        config.set(key, defaultConfig.get(key));
                        logger.info("Added missing config key: " + key);
                    }
                }

                // Sauvegarder les modifications
                saveConfig(outFileName);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read default config: " + resourcePath, e);
        }

        return config;
    }

    /**
     * Obtient le dossier de données du plugin.
     *
     * @return Le dossier de données du plugin.
     */
    public File getDataFolder() {
        return dataFolder;
    }
}