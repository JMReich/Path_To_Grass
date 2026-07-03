package jacobreich.path_to_grass.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ShovelItem.class)
public class ShovelMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void useOnPath(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();

        if(block == Blocks.DIRT_PATH) {
            level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1, 1);
            level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 0);
            context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
            cir.setReturnValue(InteractionResult.SUCCESS);
        }

    }
}
