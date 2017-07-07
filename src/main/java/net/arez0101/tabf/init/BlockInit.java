package net.arez0101.tabf.init;

import net.arez0101.tabf.TABF;
import net.arez0101.tabf.block.BlockController;
import net.arez0101.tabf.block.BlockInputOutput;
import net.arez0101.tabf.block.BlockWall;
import net.arez0101.tabf.tileentity.TileEntityBigFurnace;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockInit {
	
	public static void initCommon() {
		initBlocks();
		
		registerBlock(CONTROLLER, TileEntityBigFurnace.class);
		registerBlock(WALL);
		registerBlock(FUEL_INPUT);
		registerBlock(SMELTING_INPUT);
		registerBlock(SMELTING_OUTPUT);
	}
	
	private static void initBlocks() {
		CONTROLLER = new BlockController().setUnlocalizedName("furnace_controller").setRegistryName(TABF.MODID, "furnace_controller");
		WALL = new BlockWall().setUnlocalizedName("furnace_wall").setRegistryName(TABF.MODID, "furnace_wall");
		FUEL_INPUT = new BlockInputOutput().setUnlocalizedName("fuel_input").setRegistryName(TABF.MODID, "fuel_input");
		SMELTING_INPUT = new BlockInputOutput().setUnlocalizedName("smelting_input").setRegistryName(TABF.MODID, "smelting_input");
		SMELTING_OUTPUT = new BlockInputOutput().setUnlocalizedName("smelting_output").setRegistryName(TABF.MODID, "smelting_output");
	}
	
	private static void registerBlock(Block block) {
		GameRegistry.register(block);
	}
	
	private static void registerBlock(Block block, Class<? extends TileEntity> tileentity) {
		registerBlock(block);
		GameRegistry.registerTileEntity(tileentity, TABF.MODID + "_te_" + block.getUnlocalizedName());
	}
	
	public static Block CONTROLLER;
	public static Block WALL;
	public static Block FUEL_INPUT;
	public static Block SMELTING_INPUT;
	public static Block SMELTING_OUTPUT;
	public static Block BIG_FURNACE;
}
