package jacob_reich.path_to_grass;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<PathBlockEntity> PATH_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(
            PathBlockEntity::new, Blocks.DIRT_PATH).build(null);

    public static void register() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("path_to_grass", "path_block_entity"), PATH_BLOCK_ENTITY);
    }
}