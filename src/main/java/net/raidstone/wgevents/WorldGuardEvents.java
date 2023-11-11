package net.raidstone.wgevents;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
/**
 * @author Weby &amp; Anrza (info@raidstone.net)
 * @since 2/24/19
 */
public class WorldGuardEvents {

    private final JavaPlugin plugin;

    static RegionContainer container;

    public WorldGuardEvents(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    //    Listeners listeners = null;
    public void init() {
        Logger log = plugin.getLogger();
        PluginManager pm = Bukkit.getPluginManager();
        Plugin p = Bukkit.getPluginManager().getPlugin("WorldGuard");
        
        if (p == null) {
            log.severe("WorldGuard wasn't found. Disabling...");
            pm.disablePlugin(plugin);
            return;
        }
        
        String version = WorldGuard.getVersion();

        if (!version.startsWith("7.")) {
            log.warning("Detected WorldGuard version \"" + version + "\".");
            log.warning("This plugin is meant to work with WorldGuard version \"7.0.0\" or higher,");
            log.warning("and may not work properly with any other major revision.");
            log.warning("Please update WorldGuard if your version is below \"7.0.0\"");
        }
        
        if (!WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(Entry.factory, null)) {
            log.severe("Could not register the entry handler !");
            log.severe("Please report this error. The plugin will now be disabled.");
    
            pm.disablePlugin(plugin);
            return;
        }
        
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    @Nonnull
    public static Set<ProtectedRegion> getRegions(Location location)
    {
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        return set.getRegions();
    }

    @Nonnull
    public static Set<String> getRegionsNames(Location location)
    {
        return getRegions(location).stream().map(ProtectedRegion::getId).collect(Collectors.toSet());
    }

    /**
     * Gets the regions a player is currently in.
     *
     * @param playerUUID UUID of the player in question.
     * @return Set of WorldGuard protected regions that the player is currently in.
     */
    @Nonnull
    public static Set<ProtectedRegion> getRegions(UUID playerUUID)
    {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline())
            return Collections.emptySet();
        
        return getRegions(player.getLocation());
    }
    
    /**
     * Gets the regions names a player is currently in.
     *
     * @param playerUUID UUID of the player in question.
     * @return Set of Strings with the names of the regions the player is currently in.
     */
    @Nonnull
    public static Set<String> getRegionsNames(UUID playerUUID)
    {
        return getRegions(playerUUID).stream().map(ProtectedRegion::getId).collect(Collectors.toSet());
    }
    
    /**
     * Checks whether a player is in one or several regions
     *
     * @param playerUUID UUID of the player in question.
     * @param regionNames Set of regions to check.
     * @return True if the player is in (all) the named region(s).
     */
    public static boolean isPlayerInAllRegions(UUID playerUUID, Set<String> regionNames)
    {
        Set<String> regions = getRegionsNames(playerUUID);
        if(regionNames.isEmpty()) throw new IllegalArgumentException("You need to check for at least one region !");
        
        return regions.containsAll(regionNames.stream().map(String::toLowerCase).collect(Collectors.toSet()));
    }
    
    /**
     * Checks whether a player is in one or several regions
     *
     * @param playerUUID UUID of the player in question.
     * @param regionNames Set of regions to check.
     * @return True if the player is in (any of) the named region(s).
     */
    public static boolean isPlayerInAnyRegion(UUID playerUUID, Set<String> regionNames)
    {
        Set<String> regions = getRegionsNames(playerUUID);
        if(regionNames.isEmpty()) throw new IllegalArgumentException("You need to check for at least one region !");
        for(String region : regionNames)
        {
            if(regions.contains(region.toLowerCase()))
                return true;
        }
        return false;
    }
    
    /**
     * Checks whether a player is in one or several regions
     *
     * @param playerUUID UUID of the player in question.
     * @param regionName List of regions to check.
     * @return True if the player is in (any of) the named region(s).
     */
    public static boolean isPlayerInAnyRegion(UUID playerUUID, String... regionName)
    {
        return isPlayerInAnyRegion(playerUUID, new HashSet<>(Arrays.asList(regionName)));
    }
    
    /**
     * Checks whether a player is in one or several regions
     *
     * @param playerUUID UUID of the player in question.
     * @param regionName List of regions to check.
     * @return True if the player is in (any of) the named region(s).
     */
    public static boolean isPlayerInAllRegions(UUID playerUUID, String... regionName)
    {
        return isPlayerInAllRegions(playerUUID, new HashSet<>(Arrays.asList(regionName)));
    }
    
}
