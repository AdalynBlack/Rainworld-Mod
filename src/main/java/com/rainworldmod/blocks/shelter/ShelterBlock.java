package com.rainworldmod.blocks.shelter;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.Direction;

public class ShelterBlock extends FacingBlock {
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

    /*@Override
    protected MapCodec<? extends FacingBlock> getCodec() {
        return null;
    }*/
}
