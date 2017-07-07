package net.arez0101.tabf.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RecipeInit {
	
	public static void registerRecipes() {
		GameRegistry.addShapelessRecipe(new ItemStack(ItemInit.SMELTING_INPUT), new Object[] {ItemInit.SMELTING_OUTPUT});
		GameRegistry.addShapelessRecipe(new ItemStack(ItemInit.SMELTING_OUTPUT), new Object[] {ItemInit.SMELTING_INPUT});
		GameRegistry.addShapedRecipe(new ItemStack(ItemInit.FUEL_INPUT), new Object[] {"", "", ""});
	}
}
