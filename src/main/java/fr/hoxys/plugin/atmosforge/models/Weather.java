package fr.hoxys.plugin.atmosforge.models;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.effects.WeatherEffect;
import fr.hoxys.plugin.atmosforge.models.effects.BlockEffect;
import fr.hoxys.plugin.atmosforge.models.effects.ParticleEffect;
import fr.hoxys.plugin.atmosforge.models.effects.PotionEffect;
import fr.hoxys.plugin.atmosforge.models.effects.SoundEffect;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Classe représentant une condition météorologique avec ses effets.
 * Cette classe sert de base pour tous les types de météo spécifiques.
 */
public class Weather {

    private final Main plugin;
    private final WeatherType weatherType;
    private final List<WeatherEffect> effects;
    private final Map<UUID, List<BukkitTask>> worldTasks;
    private final Logger logger;

    /**
     * Constructeur de la classe Weather.
     *
     * @param plugin L'instance du plugin principal.
     * @param weatherType Le type de météo associé à cette condition.
     */
    public Weather(Main plugin, WeatherType weatherType) {
        this.plugin = plugin;
        this.weatherType = weatherType;
        this.effects = new ArrayList<>();
        this.worldTasks = new HashMap<>();
        this.logger = plugin.getLogger();
    }

    /**
     * Obtient le type de météo associé à cette condition.
     *
     * @return Le type de météo.
     */
    public WeatherType getWeatherType() {
        return weatherType;
    }

    /**
     * Obtient la liste des effets associés à cette condition météorologique.
     *
     * @return La liste des effets.
     */
    public List<WeatherEffect> getEffects() {
        return new ArrayList<>(effects);
    }

    /**
     * Ajoute un effet à cette condition météorologique.
     *
     * @param effect L'effet à ajouter.
     */
    public void addEffect(WeatherEffect effect) {
        effects.add(effect);
    }

    /**
     * Supprime un effet de cette condition météorologique.
     *
     * @param effect L'effet à supprimer.
     * @return true si l'effet a été supprimé, false sinon.
     */
    public boolean removeEffect(WeatherEffect effect) {
        return effects.remove(effect);
    }

    /**
     * Supprime tous les effets de cette condition météorologique.
     */
    public void clearEffects() {
        effects.clear();
    }

    /**
     * Ajoute un effet de potion à cette condition météorologique.
     *
     * @param effectType Le type d'effet de potion.
     * @param amplifier L'amplificateur de l'effet (0 = niveau 1, 1 = niveau 2, etc.).
     * @param duration La durée de l'effet en ticks.
     * @param ambient Si l'effet est ambient (particules moins visibles).
     */
    public void addPotionEffect(PotionEffectType effectType, int amplifier, int duration, boolean ambient) {
        PotionEffect effect = new PotionEffect(plugin, weatherType, 1.0f, 24000,
                effectType, amplifier, duration, ambient, true, true);
        addEffect(effect);
    }

    /**
     * Ajoute un effet de particules à cette condition météorologique.
     *
     * @param particleType Le type de particule.
     * @param count Le nombre de particules.
     * @param spread L'étendue des particules.
     * @param speed La vitesse des particules.
     */
    public void addParticleEffect(Particle particleType, int count, double spread, float speed) {
        ParticleEffect effect = new ParticleEffect(plugin, weatherType, particleType,
                1.0f, 24000, count, spread, 15.0, speed);
        addEffect(effect);
    }

    /**
     * Ajoute un effet de dégâts périodiques à cette condition météorologique.
     *
     * @param damage La quantité de dégâts.
     * @param interval L'intervalle entre les dégâts en ticks.
     */
    public void addDamageEffect(double damage, int interval) {
        BukkitTask task = null;
        addEffect(new WeatherEffect() {
            private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();
            private final Map<UUID, BukkitTask> worldTasks = new HashMap<>();
            private float intensity = 1.0f;
            private int duration = 24000;

            @Override
            public WeatherType getWeatherType() {
                return weatherType;
            }

            @Override
            public void applyToPlayer(Player player, World world) {
                if (isActiveForPlayer(player)) return;

                BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                    if (!player.isOnline() || !player.getWorld().equals(world)) {
                        removeFromPlayer(player);
                        return;
                    }

                    // Vérifier si le joueur est exposé au ciel
                    if (isPlayerExposedToSky(player)) {
                        player.damage(damage);
                        sendDamageMessage(player);
                    }
                }, 0L, interval);

                playerTasks.put(player.getUniqueId(), task);
            }

            @Override
            public void applyToWorld(World world) {
                if (isActiveForWorld(world)) return;

                BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                    for (Player player : world.getPlayers()) {
                        // Vérifier si le joueur est exposé au ciel
                        if (isPlayerExposedToSky(player)) {
                            player.damage(damage);
                            sendDamageMessage(player);
                        }
                    }
                }, 0L, interval);

                worldTasks.put(world.getUID(), task);
            }

            @Override
            public void removeFromPlayer(Player player) {
                BukkitTask task = playerTasks.remove(player.getUniqueId());
                if (task != null) {
                    task.cancel();
                }
            }

            @Override
            public void removeFromWorld(World world) {
                BukkitTask task = worldTasks.remove(world.getUID());
                if (task != null) {
                    task.cancel();
                }
            }

            @Override
            public boolean isActiveForPlayer(Player player) {
                return playerTasks.containsKey(player.getUniqueId());
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
                this.intensity = intensity;
            }

            @Override
            public int getDuration() {
                return duration;
            }

            @Override
            public void setDuration(int duration) {
                this.duration = duration;
            }

            private boolean isPlayerExposedToSky(Player player) {
                return player.getLocation().getWorld().getHighestBlockYAt(player.getLocation()) <= player.getLocation().getY();
            }

            private void sendDamageMessage(Player player) {
                String message = "";
                if (weatherType == WeatherType.HEAT_WAVE || weatherType == WeatherType.HEATSTROKE) {
                    message = plugin.getLanguageManager().getMessage("effects.heat_damage");
                } else if (weatherType == WeatherType.COLD_WAVE || weatherType == WeatherType.BLIZZARD) {
                    message = plugin.getLanguageManager().getMessage("effects.cold_damage");
                } else {
                    message = plugin.getLanguageManager().getMessage("effects.wind_damage");
                }
                player.sendMessage(message);
            }
        });
    }

    /**
     * Ajoute un effet de son à cette condition météorologique.
     *
     * @param sound Le type de son.
     * @param volume Le volume du son.
     * @param pitch La hauteur du son.
     * @param interval L'intervalle entre les sons en ticks.
     */
    public void addSoundEffect(Sound sound, float volume, float pitch, int interval) {
        SoundEffect effect = new SoundEffect(plugin, weatherType, sound,
                1.0f, 24000, volume, pitch, interval);
        addEffect(effect);
    }

    /**
     * Ajoute un effet de bloc à cette condition météorologique.
     *
     * @param originalMaterial Le matériau original.
     * @param targetMaterial Le matériau cible.
     * @param radius Le rayon d'effet.
     * @param interval L'intervalle entre les transformations.
     */
    public void addBlockEffect(Material originalMaterial, Material targetMaterial, int radius, int interval) {
        BlockEffect effect = new BlockEffect(plugin, weatherType, 1.0f, 24000,
                radius, interval, originalMaterial, targetMaterial);
        addEffect(effect);
    }

    /**
     * Applique cette condition météorologique à un joueur.
     *
     * @param player Le joueur auquel appliquer les effets.
     * @param world Le monde dans lequel le joueur se trouve.
     */
    public void applyToPlayer(Player player, World world) {
        for (WeatherEffect effect : effects) {
            effect.applyToPlayer(player, world);
        }
    }

    /**
     * Applique cette condition météorologique à un monde.
     *
     * @param world Le monde auquel appliquer les effets.
     */
    public void applyToWorld(World world) {
        UUID worldId = world.getUID();

        // Annuler les tâches précédentes
        removeFromWorld(world);

        List<BukkitTask> tasks = new ArrayList<>();

        // Appliquer tous les effets au monde
        for (WeatherEffect effect : effects) {
            effect.applyToWorld(world);
        }

        // Stocker les tâches pour pouvoir les annuler plus tard
        worldTasks.put(worldId, tasks);

        logger.fine("Applied weather effects for " + weatherType.getId() + " in world " + world.getName());
    }

    /**
     * Supprime cette condition météorologique d'un joueur.
     *
     * @param player Le joueur duquel supprimer les effets.
     */
    public void removeFromPlayer(Player player) {
        for (WeatherEffect effect : effects) {
            effect.removeFromPlayer(player);
        }
    }

    /**
     * Supprime cette condition météorologique d'un monde.
     *
     * @param world Le monde duquel supprimer les effets.
     */
    public void removeFromWorld(World world) {
        UUID worldId = world.getUID();

        // Annuler toutes les tâches existantes
        if (worldTasks.containsKey(worldId)) {
            List<BukkitTask> tasks = worldTasks.get(worldId);
            for (BukkitTask task : tasks) {
                task.cancel();
            }
            worldTasks.remove(worldId);
        }

        // Supprimer tous les effets
        for (WeatherEffect effect : effects) {
            effect.removeFromWorld(world);
        }

        logger.fine("Removed weather effects for " + weatherType.getId() + " from world " + world.getName());
    }

    /**
     * Annule toutes les tâches associées à cette condition météorologique.
     */
    public void cancelAllTasks() {
        for (List<BukkitTask> tasks : worldTasks.values()) {
            for (BukkitTask task : tasks) {
                task.cancel();
            }
        }
        worldTasks.clear();

        // Annuler également les tâches dans les effets
        for (WeatherEffect effect : effects) {
            if (effect instanceof ParticleEffect) {
                ((ParticleEffect) effect).cancelAllTasks();
            } else if (effect instanceof SoundEffect) {
                ((SoundEffect) effect).cancelAllTasks();
            } else if (effect instanceof PotionEffect) {
                ((PotionEffect) effect).cancelAllTasks();
            } else if (effect instanceof BlockEffect) {
                ((BlockEffect) effect).cancelAllTasksAndRestoreBlocks();
            }
        }
    }

    /**
     * Obtient l'instance du plugin principal.
     *
     * @return L'instance du plugin.
     */
    protected Main getPlugin() {
        return plugin;
    }
}