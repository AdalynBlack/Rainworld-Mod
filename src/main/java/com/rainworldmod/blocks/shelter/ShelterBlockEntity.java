package com.rainworldmod.blocks.shelter;

import com.rainworldmod.AllBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class ShelterBlockEntity extends BlockEntity {
    public ShelterBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.SHELTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {}

    @Override
    public void writeNbt(NbtCompound nbt) {}
}
