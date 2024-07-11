package com.rainworldmod.blocks.shelter;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class ShelterBlock extends FacingBlock implements BlockEntityProvider {
    public ShelterBlock() {
        super(FabricBlockSettings.copyOf(Blocks.BEDROCK)
                .nonOpaque()
                .noCollision());
        this.setDefaultState(getDefaultState().with(FACING, Direction.DOWN));
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlayerLookDirection();
        return this.getDefaultState().with(FACING, direction.getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShelterBlockEntity(pos, state);
    }
}
