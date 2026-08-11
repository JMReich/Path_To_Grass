package jacobreich.path_reversal.mixin;

import jacobreich.path_reversal.util.PathBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class DirtPathBlockMixin {

    @Inject(method = "onRemove", at = @At("HEAD"))
    private void onBlockRemoved(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        if (state.getBlock() == Blocks.DIRT_PATH && !level.isClientSide()) {
            PathBlockData data = PathBlockData.get(level);
            data.removeBlockState(PathBlockData.getStorageKey(level, pos));
        }
    }
}
