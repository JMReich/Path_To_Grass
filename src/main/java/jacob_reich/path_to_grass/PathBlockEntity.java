package jacob_reich.path_to_grass;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class PathBlockEntity extends BlockEntity {
    private String originalBlock;

    public PathBlockEntity(BlockPos pos, net.minecraft.block.BlockState state) {
        super(ModBlockEntities.PATH_BLOCK_ENTITY, pos, state);
    }

    public void setOriginalBlock(String blockId) {
        this.originalBlock = blockId;
    }

    public String getOriginalBlock() {
        return originalBlock;
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup keepItems) {
        super.writeNbt(nbt, keepItems);
        if (originalBlock != null) {
            nbt.putString("OriginalBlock", originalBlock);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        if (nbt.contains("OriginalBlock")) {
            originalBlock = String.valueOf(nbt.getString("OriginalBlock"));
        }
    }
}