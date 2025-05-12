package jacob_reich.path_to_grass.mixin;


import jacob_reich.path_to_grass.PathBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
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
            BlockPos pos = context.getBlockPos();
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();

            if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT) {
                // Save the original block type
                if (!world.isClient) {
                    world.setBlockState(pos, Blocks.DIRT_PATH.getDefaultState());
                    PathBlockEntity blockEntity = new PathBlockEntity(pos, Blocks.DIRT_PATH.getDefaultState());
                    blockEntity.setOriginalBlock(Registries.BLOCK.getId(block).toString());
                    world.addBlockEntity(blockEntity);
                }
                cir.setReturnValue(ActionResult.SUCCESS);
            } else if (block == Blocks.DIRT_PATH) {
                // Retrieve the original block type
                if (!world.isClient) {
                    PathBlockEntity blockEntity = (PathBlockEntity) world.getBlockEntity(pos);
                    if (blockEntity != null) {
                        Block originalBlock = Registries.BLOCK.get(Identifier.of(blockEntity.getOriginalBlock()));
                        world.setBlockState(pos, originalBlock.getDefaultState());
                    }
                } else {
                    PlayerEntity player = context.getPlayer();
                    PathBlockEntity blockEntity = (PathBlockEntity) world.getBlockEntity(pos);
                    if (player != null) {
                        player.sendMessage(Text.translatable("message.path_to_grass.original_block", blockEntity.getOriginalBlock()), true);
                    }
                }
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        } else if (context.getHand() == Hand.OFF_HAND) {
            World world = context.getWorld();
            PlayerEntity player = context.getPlayer();

            BlockPos pos = context.getBlockPos();
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();




            if (block == Blocks.DIRT_PATH) {
                world.playSound(null, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.setBlockState(pos, Blocks.GRASS_BLOCK.getDefaultState());
                cir.setReturnValue(ActionResult.SUCCESS);
                if (player != null) {
                    context.getStack().damage(1, player, EquipmentSlot.OFFHAND);
                }
            }
        }


    }

}
