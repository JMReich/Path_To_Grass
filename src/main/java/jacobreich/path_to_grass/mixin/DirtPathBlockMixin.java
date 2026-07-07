package jacobreich.path_to_grass.mixin;

import jacobreich.path_to_grass.util.PathBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class DirtPathBlockMixin {

    @Inject(method = "affectNeighborsAfterRemoval", at = @At("HEAD"))
    private void onBlockRemoved(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston, CallbackInfo ci) {
        if (state.getBlock() == Blocks.DIRT_PATH) {
            PathBlockData data = PathBlockData.get(level);
            data.removeBlockState(PathBlockData.getStorageKey(level, pos));
        }
    }
}
