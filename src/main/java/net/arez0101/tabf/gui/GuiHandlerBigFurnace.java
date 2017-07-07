package net.arez0101.tabf.gui;

import net.arez0101.tabf.inventory.ContainerBigFurnace;
import net.arez0101.tabf.tileentity.TileEntityBigFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandlerBigFurnace implements IGuiHandler {
	
	private static final int GUI_ID = 1;
	
	public static int getGuiID() {
		return GUI_ID;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID != getGuiID()) {
			System.err.println("Invalid ID for GUI: expected " + getGuiID() + ", instead received " + ID);
		}
		
		BlockPos pos = new BlockPos(x, y, z);
		
		if (world.getTileEntity(pos) instanceof TileEntityBigFurnace) {
			TileEntityBigFurnace te = (TileEntityBigFurnace) world.getTileEntity(pos);
			return new ContainerBigFurnace(player.inventory, te);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID != getGuiID()) {
			System.err.println("Invalid ID for GUI: expected " + getGuiID() + ", instead received " + ID);
		}
		
		BlockPos pos = new BlockPos(x, y, z);
		
		if (world.getTileEntity(pos) instanceof TileEntityBigFurnace) {
			TileEntityBigFurnace te = (TileEntityBigFurnace) world.getTileEntity(pos);
			return new GuiBigFurnace(player.inventory, te);
		}
		return null;
	}

}
