package jacobreich.path_reversal.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PathBlockData {
    private static final String FILE_NAME_TEMPLATE = "path_reversal%d.nbt";
    private static final String DEFAULT_BLOCK_STATE = "minecraft:grass_block";
    private static PathBlockData instance;
    private Map<Long, CompoundTag> chunkData;
    private Map<Long, Boolean> dirtyChunks;
    private Path worldDataPath;
    private final Object dataLock = new Object();

    private PathBlockData(Path worldPath) {
        this.worldDataPath = worldPath;
        this.chunkData = new HashMap<>();
        this.dirtyChunks = new HashMap<>();
    }

    public static String getStorageKey(Level level, BlockPos pos) {
        return level.dimension().toString() + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    public void storeBlockState(String key, String blockId) {
        synchronized (dataLock) {
            long chunkHash = getChunkHashFromKey(key);
            CompoundTag chunk = getOrLoadChunk(chunkHash);
            chunk.putString(key, blockId);
            dirtyChunks.put(chunkHash, true);
        }
    }

    public String getBlockState(String key) {
        synchronized (dataLock) {
            long chunkHash = getChunkHashFromKey(key);
            CompoundTag chunk = getOrLoadChunk(chunkHash);
            return chunk.contains(key) ? chunk.getString(key) : DEFAULT_BLOCK_STATE;
        }
    }

    public void removeBlockState(String key) {
        synchronized (dataLock) {
            long chunkHash = getChunkHashFromKey(key);
            CompoundTag chunk = getOrLoadChunk(chunkHash);
            chunk.remove(key);
            dirtyChunks.put(chunkHash, true);
        }
    }

    public boolean hasBlockState(String key) {
        synchronized (dataLock) {
            long chunkHash = getChunkHashFromKey(key);
            CompoundTag chunk = getOrLoadChunk(chunkHash);
            return chunk.contains(key);
        }
    }

    private long getChunkHashFromKey(String key) {
        // Key format: "dimension_X_Y_Z" where dimension may itself contain underscores
        // Parse X, Y, Z from the end to avoid being tripped up by dimension names like "the_nether"
        String[] parts = key.split("_");
        if (parts.length >= 4) {
            try {
                int z = Integer.parseInt(parts[parts.length - 1]);
                // parts[parts.length - 2] is Y (ignored for chunk lookup)
                int x = Integer.parseInt(parts[parts.length - 3]);
                int chunkX = x >> 4;
                int chunkZ = z >> 4;

                // Include a dimension component so overworld and nether chunks don't share files
                String dimension = String.join("_", java.util.Arrays.copyOfRange(parts, 0, parts.length - 3));
                long dimComponent = (long) dimension.hashCode() << 48;

                long coordHash = ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
                return coordHash ^ dimComponent;
            } catch (NumberFormatException e) {
                return (long) key.hashCode();
            }
        }
        return (long) key.hashCode();
    }

    private CompoundTag getOrLoadChunk(long chunkHash) {
        CompoundTag chunk = chunkData.get(chunkHash);
        if (chunk == null) {
            chunk = loadChunk(chunkHash);
            if (chunk == null) {
                chunk = new CompoundTag();
            }
            chunkData.put(chunkHash, chunk);
        }
        return chunk;
    }

    public void savePeriodically() {
        synchronized (dataLock) {
            for (Map.Entry<Long, Boolean> entry : dirtyChunks.entrySet()) {
                if (entry.getValue()) {
                    saveChunk(entry.getKey());
                    entry.setValue(false);
                }
            }
        }
    }

    private void saveChunk(long chunkHash) {
        try {
            if (worldDataPath != null) {
                CompoundTag chunk = chunkData.get(chunkHash);
                if (chunk != null) {
                    Path chunkDir = worldDataPath.resolve("path_reversal");
                    File file = chunkDir.resolve(
                        String.format(FILE_NAME_TEMPLATE, chunkHash)
                    ).toFile();
                    
                    if (chunk.size() > 0) {
                        java.nio.file.Files.createDirectories(chunkDir);
                        NbtIo.writeCompressed(chunk, file);
                    } else {
                        // Delete empty chunk file
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CompoundTag loadChunk(long chunkHash) {
        try {
            if (worldDataPath != null) {
                Path chunkDir = worldDataPath.resolve("path_reversal");
                File file = chunkDir.resolve(
                    String.format(FILE_NAME_TEMPLATE, chunkHash)
                ).toFile();
                if (file.exists()) {
                    return NbtIo.readCompressed(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PathBlockData get(Level level) {
        if (level.getServer() != null) {
            Path worldPath = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            if (instance == null) {
                instance = new PathBlockData(worldPath);
            }
        }
        return instance != null ? instance : new PathBlockData(null);
    }
}
