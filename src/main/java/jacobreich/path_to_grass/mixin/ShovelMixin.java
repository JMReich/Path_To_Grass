package jacobreich.path_to_grass.mixin;

import jacobreich.path_to_grass.util.PathBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
//
//    private static BlockPos lastProcessedPos = null;
//    private static long lastProcessedTime = 0;
//    private static final long COOLDOWN_MS = 200;

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void useOnPath(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        


        
//        long currentTime = System.currentTimeMillis();
//        if (lastProcessedPos != null && lastProcessedPos.equals(pos) && (currentTime - lastProcessedTime) < COOLDOWN_MS) {
//            return;
//        }
//        lastProcessedPos = pos;
//        lastProcessedTime = currentTime;
        
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        Player player = context.getPlayer();
//        if (player != null) {
//            player.sendSystemMessage(Component.literal("fired"));
//        }

        // Revert DIRT_PATH back to original block or grass block
        if (block == Blocks.DIRT_PATH) {
            if(!level.isClientSide()) {
                player.swing(context.getHand(), true);
                level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1, 1);


                PathBlockData data = PathBlockData.get(level);
                String key = getStorageKey(level, pos);
                String storedState = data.getBlockState(key);
                BlockState revertState = deserializeBlockState(storedState, level);
                data.removeBlockState(key);

                level.setBlock(pos, revertState, 3);
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
        // Convert valid blocks to DIRT_PATH and store original state
        else if (!level.isClientSide()) {
            // Tentatively store the original block state before vanilla potentially converts it.
            // The RETURN injection will clean this up if conversion didn't actually happen.
            PathBlockData data = PathBlockData.get(level);
            String key = getStorageKey(level, pos);
            data.storeBlockState(key, serializeBlockState(blockState));
        }
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void cleanupIfNotConverted(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = context.getClickedPos();
        // If the block at this position is not a dirt path after useOn completes,
        // the conversion didn't happen — remove the tentatively stored state.
        if (level.getBlockState(pos).getBlock() != Blocks.DIRT_PATH) {
            PathBlockData data = PathBlockData.get(level);
            data.removeBlockState(getStorageKey(level, pos));
        }
    }

    private String getStorageKey(Level level, BlockPos pos) {
        return PathBlockData.getStorageKey(level, pos);
    }

    private String serializeBlockState(BlockState state) {
        CompoundTag tag = NbtUtils.writeBlockState(state);
        return tag.toString();
    }

    private BlockState deserializeBlockState(String serialized, Level level) {
        try {
            HolderGetter<Block> holderGetter = level.registryAccess().lookupOrThrow(Registries.BLOCK);
            try {
                // New format: full SNBT block state (e.g. {Name:"mod:block",Properties:{...}})
                CompoundTag tag = TagParser.parseCompoundFully(serialized);
                return NbtUtils.readBlockState(holderGetter, tag);
            } catch (Exception e) {
                // Old format fallback: plain block ID string (e.g. "mod:block")
                CompoundTag tag = new CompoundTag();
                tag.putString("Name", serialized);
                return NbtUtils.readBlockState(holderGetter, tag);
            }
        } catch (Exception e) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
    }
}
