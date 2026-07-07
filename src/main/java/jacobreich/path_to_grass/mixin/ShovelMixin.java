package jacobreich.path_to_grass.mixin;

import jacobreich.path_to_grass.util.PathBlockData;
import net.minecraft.core.BlockPos;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
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
            if(level.isClientSide()){
                player.swing(context.getHand());
            }
            if(!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1, 1);


                PathBlockData data = PathBlockData.get(level);
                String key = getStorageKey(level, pos);
                String storedState = data.getBlockState(key);
                BlockState revertState = deserializeBlockState(storedState);
                data.removeBlockState(key);

                level.setBlock(pos, revertState, 3);
                context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
        // Convert valid blocks to DIRT_PATH and store original state
        else if (canConvertToPath(blockState) && !level.isClientSide()) {
            PathBlockData data = PathBlockData.get(level);
            String key = getStorageKey(level, pos);
            data.storeBlockState(key, serializeBlockState(blockState));

//            BlockState pathState = Blocks.DIRT_PATH.defaultBlockState();
//            level.setBlock(pos, pathState, 3);

//            level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1, 1);
//            context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
//            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    private String getStorageKey(Level level, BlockPos pos) {
        return level.dimension().toString() + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    private boolean canConvertToPath(BlockState blockState) {
        // Check if block is in the dirt tag or is mycelium/podzol/rooted dirt
        Block block = blockState.getBlock();
        return blockState.is(BlockTags.DIRT) 
            || block == Blocks.MYCELIUM
            || block == Blocks.PODZOL
            || block == Blocks.ROOTED_DIRT;
    }

    private String serializeBlockState(BlockState state) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return blockId.toString();
    }

    private BlockState deserializeBlockState(String serialized) {
       try {
           Identifier blockId = Identifier.parse(serialized);
           Block block = BuiltInRegistries.BLOCK.get(blockId).get().value();
           return block.defaultBlockState();
       } catch (Exception e) {
           return Blocks.GRASS_BLOCK.defaultBlockState();
       }
    }
}
