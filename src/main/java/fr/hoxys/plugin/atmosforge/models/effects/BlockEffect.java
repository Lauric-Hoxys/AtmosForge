package fr.hoxys.plugin.atmosforge.models.effects;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Implémentation d'un effet météorologique qui affecte les blocs.
 */
public class BlockEffect implements WeatherEffect {

    private final Main plugin;
    private final WeatherType weatherType;
    private float intensity;
    private int duration;
    private final int radius;
    private final int interval;
    private final Material affectedMaterial;
    private final Material resultMaterial;

    // Stocke les blocs affectés pour pouvoir les restaurer plus tard
    private final Map<Location, Material> affectedBlocks;
    private final Map<UUID, BukkitTask> worldTasks;
    private final Random random;

    /**
     * Constructeur de BlockEffect.
     *
     * @param plugin L'instance du plugin.
     * @param weatherType Le type de météo associé.
     * @param intensity L'intensité de l'effet (0.0 à 1.0).
     * @param duration La durée de l'effet en ticks.
     * @param radius Le rayon d'effet autour des joueurs.
     * @param interval L'intervalle entre les applications en ticks.
     * @param affectedMaterial Le matériau à affecter.
     * @param resultMaterial Le matériau résultant.
     */
    public BlockEffect(Main plugin, WeatherType weatherType, float intensity, int duration,
                       int radius, int interval, Material affectedMaterial, Material resultMaterial) {
        this.plugin = plugin;
        this.weatherType = weatherType;
        this.intensity = intensity;
        this.duration = duration;
        this.radius = radius;
        this.interval = interval;
        this.affectedMaterial = affectedMaterial;
        this.resultMaterial = resultMaterial;

        this.affectedBlocks = new HashMap<>();
        this.worldTasks = new HashMap<>();
        this.random = new Random();
    }

    @Override
    public WeatherType getWeatherType() {
        return weatherType;
    }

    @Override
    public void applyToPlayer(Player player, World world) {
        // Les effets de bloc sont appliqués au monde, pas directement aux joueurs
        // Cette méthode est donc vide
    }

    @Override
    public void applyToWorld(World world) {
        if (isActiveForWorld(world)) {
            return; // Déjà actif
        }

        // Créer une tâche pour ce monde
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Pour chaque joueur dans ce monde
            for (Player player : world.getPlayers()) {
                // Trouver des blocs à affecter autour du joueur
                applyEffectAroundPlayer(player, world);
            }
        }, 0L, interval); // Intervalle entre les applications

        worldTasks.put(world.getUID(), task);
    }

    /**
     * Applique l'effet aux blocs autour d'un joueur.
     *
     * @param player Le joueur autour duquel appliquer l'effet.
     * @param world Le monde dans lequel le joueur se trouve.
     */
    private void applyEffectAroundPlayer(Player player, World world) {
        Location playerLoc = player.getLocation();

        // Obtenir le nombre de blocs à affecter en fonction de l'intensité
        int blocksToAffect = (int) (10 * intensity);

        // Affecter des blocs aléatoires dans le rayon
        for (int i = 0; i < blocksToAffect; i++) {
            // Position aléatoire dans le rayon
            int x = playerLoc.getBlockX() + random.nextInt(radius * 2) - radius;
            int z = playerLoc.getBlockZ() + random.nextInt(radius * 2) - radius;

            // Trouver le bloc le plus haut à cette position
            int y = world.getHighestBlockYAt(x, z);
            Block block = world.getBlockAt(x, y, z);

            // Si le bloc est du type affecté, le changer
            if (block.getType() == affectedMaterial) {
                // Sauvegarder l'état du bloc pour pouvoir le restaurer plus tard
                affectedBlocks.put(block.getLocation(), block.getType());

                // Changer le bloc
                block.setType(resultMaterial);
            }
        }
    }

    @Override
    public void removeFromPlayer(Player player) {
        // Les effets de bloc sont appliqués au monde, pas directement aux joueurs
        // Cette méthode est donc vide
    }

    @Override
    public void removeFromWorld(World world) {
        BukkitTask task = worldTasks.remove(world.getUID());
        if (task != null) {
            task.cancel();
        }

        // Restaurer tous les blocs affectés dans ce monde
        Set<Location> locationsToRestore = new HashSet<>();

        for (Location loc : affectedBlocks.keySet()) {
            if (loc.getWorld().equals(world)) {
                // Restaurer le bloc à son état original
                Block block = world.getBlockAt(loc);
                block.setType(affectedBlocks.get(loc));

                // Ajouter la location à la liste des blocs à supprimer
                locationsToRestore.add(loc);
            }
        }

        // Supprimer les locations restaurées
        for (Location loc : locationsToRestore) {
            affectedBlocks.remove(loc);
        }
    }

    @Override
    public boolean isActiveForPlayer(Player player) {
        // Les effets de bloc n'affectent pas directement les joueurs
        return false;
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
     * Annule toutes les tâches et restaure tous les blocs affectés.
     */
    public void cancelAllTasksAndRestoreBlocks() {
        // Annuler toutes les tâches
        for (BukkitTask task : worldTasks.values()) {
            task.cancel();
        }
        worldTasks.clear();

        // Restaurer tous les blocs affectés
        for (Map.Entry<Location, Material> entry : affectedBlocks.entrySet()) {
            Location loc = entry.getKey();
            Material originalMaterial = entry.getValue();

            // Restaurer le bloc à son état original
            Block block = loc.getBlock();
            block.setType(originalMaterial);
        }

        // Vider la liste des blocs affectés
        affectedBlocks.clear();
    }
}