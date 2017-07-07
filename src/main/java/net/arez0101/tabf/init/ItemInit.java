package net.arez0101.tabf.init;

import net.arez0101.tabf.TABF;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemInit {
	
	public static void initCommon() {
		initItems();
	}
	
	@SideOnly(Side.CLIENT)
	public static void initClient() {
		registerRender(CONTROLLER);
		registerRender(WALL);
		registerRender(FUEL_INPUT);
		registerRender(SMELTING_INPUT);
		registerRender(SMELTING_OUTPUT);
	}
	
	private static void initItems() {
		CONTROLLER = new ItemBlock(BlockInit.CONTROLLER).setUnlocalizedName("furnace_controller").setRegistryName(TABF.MODID, "furnace_controller");
		WALL = new ItemBlock(BlockInit.WALL).setUnlocalizedName("furnace_wall").setRegistryName(TABF.MODID, "furnace_wall");
		FUEL_INPUT = new ItemBlock(BlockInit.FUEL_INPUT).setUnlocalizedName("fuel_input").setRegistryName(TABF.MODID, "fuel_input");
		SMELTING_INPUT = new ItemBlock(BlockInit.SMELTING_INPUT).setUnlocalizedName("smelting_input").setRegistryName(TABF.MODID, "smelting_input");
		SMELTING_OUTPUT = new ItemBlock(BlockInit.SMELTING_OUTPUT).setUnlocalizedName("smelting_output").setRegistryName(TABF.MODID, "smelting_output");
	}
	
	private static void registerItem(Item item) {
		GameRegistry.register(item);
	}
	
	@SideOnly(Side.CLIENT)
	private static void registerRender(Item item) {
		ModelResourceLocation resource = new ModelResourceLocation(item.getRegistryName(), "inventory");
		ModelLoader.setCustomModelResourceLocation(item, 0, resource);
	}
	
	public static Item CONTROLLER;
	public static Item WALL;
	public static Item FUEL_INPUT;
	public static Item SMELTING_INPUT;
	public static Item SMELTING_OUTPUT;
}
