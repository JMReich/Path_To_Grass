package jacobreich.path_to_grass.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.world.level.Level;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PathBlockData {
    private static final String FILE_NAME = "path_to_grass.nbt";
    private static final String DEFAULT_BLOCK_STATE = "minecraft:grass_block";
    private static PathBlockData instance;
    private CompoundTag blockStates;
    private Path worldDataPath;

    private PathBlockData(Path worldPath) {
        this.worldDataPath = worldPath;
        this.blockStates = new CompoundTag();
        load();
    }

    public void storeBlockState(String key, String blockId) {
        blockStates.putString(key, blockId);
        save();
    }

    public String getBlockState(String key) {
        return blockStates.getString(key).orElse(DEFAULT_BLOCK_STATE);
    }

    public void removeBlockState(String key) {
        blockStates.remove(key);
        save();
    }

    public boolean hasBlockState(String key) {
        return blockStates.contains(key);
    }

    private void save() {
        try {
            if (worldDataPath != null) {
                File file = worldDataPath.resolve(FILE_NAME).toFile();
                NbtIo.writeCompressed(blockStates, file.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        try {
            if (worldDataPath != null) {
                File file = worldDataPath.resolve(FILE_NAME).toFile();
                if (file.exists()) {
                    blockStates = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PathBlockData get(Level level) {
        if (level.getServer() != null) {
            Path worldPath = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            if (instance == null || !instance.worldDataPath.equals(worldPath)) {
                instance = new PathBlockData(worldPath);
            }
        }
        return instance != null ? instance : new PathBlockData(null);
    }
}
