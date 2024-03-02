package jacob_reich.path_to_grass.mixin;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShovelItem.class)
public class ShovelMixin {


    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnPath(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getHand() == Hand.MAIN_HAND) {
            World world = context.getWorld();
            PlayerEntity player = context.getPlayer();

            BlockPos pos = context.getBlockPos();
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();

            player.sendMessage(Text.literal("The block is " + block));

            if (block == Blocks.DIRT_PATH) {
                world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.setBlockState(pos, Blocks.GRASS_BLOCK.getDefaultState());
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }


    }

}
