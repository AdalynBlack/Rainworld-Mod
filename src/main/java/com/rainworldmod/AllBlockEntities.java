package com.rainworldmod;

import com.rainworldmod.blocks.shelter.ShelterBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class AllBlockEntities {
    public static final BlockEntityType<ShelterBlockEntity> SHELTER_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(RainworldMod.MOD_ID, "shelter_block_entity"),
            BlockEntityType.Builder.create(ShelterBlockEntity::new, AllBlocks.SHELTER_BLOCK).build(null)
    );
}
