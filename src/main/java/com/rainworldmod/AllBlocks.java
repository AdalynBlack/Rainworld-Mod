package com.rainworldmod;

import com.rainworldmod.blocks.shelter.ShelterBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.rainworldmod.RainworldMod.MOD_ID;

public final class AllBlocks {
	public static final Block SHELTER_BLOCK = register(new ShelterBlock(), "shelter");

	private static Block register(Block block, String path) {
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, path), new BlockItem(block, new Item.Settings()));
		return Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, path), block);
	}

	public static void initialize() {}
}
