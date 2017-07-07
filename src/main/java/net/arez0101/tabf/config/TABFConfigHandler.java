package net.arez0101.tabf.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class TABFConfigHandler {

	private static Configuration config;
	
	public static int maxFurnaceSize = 24;
	public static boolean ejectItems = false;
	
	public static void initConfig(File file) {
		config = new Configuration(file);
		config.load();
		
		maxFurnaceSize = config.getInt("maxFurnaceSize", "FURNACE", maxFurnaceSize, 2, 64, "The maximum size of a multiblock furnace. ");
		ejectItems = config.getBoolean("ejectItems", "FURNACE", true, "Whether or not items are ejected from the furnace when destroyed. ");
	
		config.save();
	}
}
