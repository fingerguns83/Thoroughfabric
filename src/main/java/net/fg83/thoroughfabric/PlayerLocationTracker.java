package net.fg83.thoroughfabric;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerLocationTracker {
    private static final Map<UUID, BlockPos> playerLocations = new HashMap<>();

    /**
     * Updates the player's location in the tracker.
     *
     * @param playerId the UUID of the player
     * @param newPos   the new position of the player
     */
    public static void updatePlayerLocation(UUID playerId, BlockPos newPos) {
        playerLocations.put(playerId, newPos);
    }

    /**
     * Retrieves the stored location of a player.
     *
     * @param playerId the UUID of the player
     * @return the stored position of the player, or null if not found
     */
    public static BlockPos getPlayerLocation(UUID playerId) {
        return playerLocations.get(playerId);
    }

    /**
     * Checks if the player's current position matches the tracked position.
     *
     * @param entity the entity representing the player
     * @return true if the current position matches the tracked position, false otherwise
     */
    public static boolean isMatch(Entity entity) {
        return entity.getBlockPos().equals(playerLocations.get(entity.getUuid()));
    }
}
