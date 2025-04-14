package fr.hoxys.plugin.atmosforge.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe utilitaire pour manipuler les locations.
 */
public class LocationUtils {

    private static final Random random = new Random();

    /**
     * Obtient une location aléatoire dans un rayon autour d'une location centrale.
     *
     * @param center La location centrale.
     * @param radius Le rayon maximum.
     * @return Une location aléatoire dans le rayon.
     */
    public static Location getRandomLocation(Location center, double radius) {
        double x = center.getX() + (random.nextDouble() * 2 - 1) * radius;
        double z = center.getZ() + (random.nextDouble() * 2 - 1) * radius;
        double y = center.getY() + random.nextDouble() * radius;

        return new Location(center.getWorld(), x, y, z);
    }

    /**
     * Obtient une location aléatoire en surface dans un rayon autour d'une location centrale.
     *
     * @param center La location centrale.
     * @param radius Le rayon maximum.
     * @return Une location aléatoire en surface dans le rayon.
     */
    public static Location getRandomSurfaceLocation(Location center, double radius) {
        double x = center.getX() + (random.nextDouble() * 2 - 1) * radius;
        double z = center.getZ() + (random.nextDouble() * 2 - 1) * radius;

        // Trouver la surface à cette position
        World world = center.getWorld();
        if (world != null) {
            int y = world.getHighestBlockYAt((int) x, (int) z);
            return new Location(world, x, y + 1, z);
        }

        // Si le monde est null, retourner une location avec la même hauteur que le centre
        return new Location(center.getWorld(), x, center.getY(), z);
    }

    /**
     * Vérifie si une location a une vue dégagée vers le ciel.
     *
     * @param location La location à vérifier.
     * @return true si la location a une vue dégagée vers le ciel, false sinon.
     */
    public static boolean hasDirectSkyAccess(Location location) {
        World world = location.getWorld();
        if (world == null) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        // Vérifier s'il y a des blocs au-dessus
        for (int i = y + 1; i < world.getMaxHeight(); i++) {
            Block block = world.getBlockAt(x, i, z);
            if (!block.getType().isAir()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Obtient tous les blocs dans un rayon autour d'une location.
     *
     * @param center La location centrale.
     * @param radius Le rayon.
     * @return Une liste de tous les blocs dans le rayon.
     */
    public static List<Block> getBlocksInRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return blocks;

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    // Vérifier si le bloc est dans le rayon
                    double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) + Math.pow(z - centerZ, 2));
                    if (distance <= radius) {
                        blocks.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }

        return blocks;
    }

    /**
     * Obtient tous les blocs en surface dans un rayon autour d'une location.
     *
     * @param center La location centrale.
     * @param radius Le rayon.
     * @return Une liste de tous les blocs en surface dans le rayon.
     */
    public static List<Block> getSurfaceBlocksInRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return blocks;

        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                // Vérifier si le bloc est dans le rayon
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                if (distance <= radius) {
                    int y = world.getHighestBlockYAt(x, z);
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }

        return blocks;
    }
}