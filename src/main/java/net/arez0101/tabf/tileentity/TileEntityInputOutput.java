package net.arez0101.tabf.tileentity;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;

import lombok.Getter;
import lombok.experimental.Delegate;
import net.arez0101.tabf.tileentity.TileEntityBigFurnace.Capabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class TileEntityInputOutput extends CyclopsTileEntity implements ISidedInventory {
	
	@Delegate
	private final ITickingTile tickingTile = new TickingTileComponent(this);
	
	@NBTPersist
	@Getter
	private Vec3i corePos = null;
	
	private WeakReference<TileEntityBigFurnace> coreRef = new WeakReference<TileEntityBigFurnace>(null);
	
	public TileEntityInputOutput() {
		this.addCapabilityInternal(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new InvWrapper(this));
		
		if (Capabilities.INVENTORY_STATE != null) {
			this.addInventoryState();
		}
		
		if (Capabilities.SLOTLESS_ITEM_HANDLER != null) {
			this.addSlotlessItemHandler();
		}
	}
	
	protected void addInventoryState() {
		this.addCapabilityInternal(Capabilities.INVENTORY_STATE, new TileInputOutputInventoryState(this));
	}

	protected void addSlotlessItemHandler() {
		this.addCapabilityInternal(Capabilities.SLOTLESS_ITEM_HANDLER, new TileInputOutputSlotlessItemHandler(this));
	}

	@Override
	public int getSizeInventory() {
		ISidedInventory core = this.getCore();
		return core != null ? core.getSizeInventory() : 0;
	}

	@Override
	public String getName() {
		ISidedInventory core = this.getCore();		
		return core != null ? core.getName() : null;
	}

	@Override
	public boolean hasCustomName() {
		ISidedInventory core = this.getCore();		
		return core != null ? core.hasCustomName() : false;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		ISidedInventory core = this.getCore();		
		return core != null ? core.getDisplayName() : null;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		ISidedInventory core = this.getCore();
		return core != null ? core.getSlotsForFace(side) : new int[0];
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		ISidedInventory core = this.getCore();		
		return core != null ? core.canInsertItem(index, itemStackIn, direction) : false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		ISidedInventory core = this.getCore();		
		return core != null ? core.canExtractItem(index, stack, direction) : false;
	}

	public void setCorePosition(BlockPos tilePos) {
		this.corePos = tilePos;
		this.coreRef = new WeakReference<TileEntityBigFurnace>(null);
	}
	
	protected TileEntityBigFurnace getCore() {
		if (this.corePos == null) {
			return null;
		}
		
		if (this.coreRef.get() == null) {
			this.coreRef = new WeakReference<TileEntityBigFurnace>(TileHelpers.getSafeTile(this.getWorld(), new BlockPos(this.corePos), TileEntityBigFurnace.class));
		}
		
		return this.coreRef.get();
	}
	
	@Override
	public boolean isEmpty() {
		// TODO
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		// TODO
		return null;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		// TODO
		return null;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		// TODO
		return null;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		// TODO
		
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO
		return 0;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		// TODO
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		// TODO
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// TODO
		
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		// TODO
		return false;
	}

	@Override
	public int getField(int id) {
		// TODO
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// TODO
		
	}

	@Override
	public int getFieldCount() {
		// TODO
		return 0;
	}

	@Override
	public void clear() {
		// TODO
		
	}
	
	public static class TileInputOutputInventoryState implements IInventoryState {
		
		private final TileEntityInputOutput tileentity;
		
		public TileInputOutputInventoryState(TileEntityInputOutput entityIn) {
			this.tileentity = entityIn;
		}

		@Override
		public int getHash() {
			TileEntityBigFurnace core = tileentity.getCore();
			
			if (core != null) {
				return core.getInventoryHash();
			}
			
			return -1;
		}
	}
	
	public static class TileInputOutputSlotlessItemHandler implements ISlotlessItemHandler {
		
		private final TileEntityInputOutput tileentity;
		
		public TileInputOutputSlotlessItemHandler(TileEntityInputOutput entityIn) {
			this.tileentity = entityIn;
		}
		
		protected @Nullable ISlotlessItemHandler getHandler() {
			TileEntityBigFurnace core = tileentity.getCore();
			
			if (core != null) {
				return core.hasCapability(Capabilities.SLOTLESS_ITEM_HANDLER, null) ? core.getCapability(Capabilities.SLOTLESS_ITEM_HANDLER, null) : null;
			}
			
			return null;
		}

		@Override
		public ItemStack extractItem(int arg0, boolean arg1) {
			ISlotlessItemHandler handler = this.getHandler();
			return handler != null ? handler.extractItem(arg0, arg1) : ItemStack.EMPTY;
		}

		@Override
		public ItemStack extractItem(ItemStack arg0, int arg1, boolean arg2) {
			ISlotlessItemHandler handler = this.getHandler();
			return handler != null ? handler.extractItem(arg0, arg1, arg2) : ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(ItemStack arg0, boolean arg1) {
			ISlotlessItemHandler handler = this.getHandler();
			return handler != null ? handler.insertItem(arg0, arg1) : ItemStack.EMPTY;
		}
	}

}
