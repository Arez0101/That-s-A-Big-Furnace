package net.arez0101.tabf.inventory;

import net.arez0101.tabf.tileentity.TileEntityBigFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerBigFurnace extends Container {
	
	private int[] cached;
	private TileEntityBigFurnace tileentity;
	
	private int FUEL_SLOTS = 0;
	private int INPUT_SLOTS = 0;
	private int OUTPUT_SLOTS = 0;
	private int TOTAL_SLOTS = FUEL_SLOTS + INPUT_SLOTS + OUTPUT_SLOTS;
	
	private final int HOTBAR_SLOTS = 9;
	private final int PLAYER_INV_ROWS = 3;
	private final int PLAYER_INV_COLS = 9;
	private final int PLAYER_INV_SLOTS = PLAYER_INV_ROWS * PLAYER_INV_COLS;
	private final int PLAYER_TOTAL_SLOTS = PLAYER_INV_SLOTS + HOTBAR_SLOTS;
	
	private final int ROW_START = 7;
	private final int SPACING = 18;
	private final int HOTBAR_Y_POS = 219;
	private final int PLAYER_INV_Y_POS = 161;
	
	private final int FIRST_SLOT = 0;
	private final int FIRST_FUEL_SLOT = FIRST_SLOT + PLAYER_TOTAL_SLOTS;
	private final int FIRST_INPUT_SLOT = FIRST_FUEL_SLOT + FUEL_SLOTS;
	private final int FIRST_OUTPUT_SLOT = FIRST_INPUT_SLOT + INPUT_SLOTS;
	private final int COMP_FIRST_FUEL_SLOT = 0;
	private final int COMP_FIRST_INPUT_SLOT = COMP_FIRST_FUEL_SLOT + FUEL_SLOTS;
	private final int COMP_FIRST_OUTPUT_SLOT = COMP_FIRST_INPUT_SLOT + INPUT_SLOTS;
	
	private final int FUEL_Y_POS = 60;
	private final int INPUT_Y_POS = 5;
	private final int OUTPUT_Y_POS = 119;
	
	public ContainerBigFurnace(InventoryPlayer playerInv, TileEntityBigFurnace tileEntityIn) {
		this.tileentity = tileEntityIn;
		FUEL_SLOTS = this.tileentity.getFuelInputLocation().size();
		INPUT_SLOTS = this.tileentity.getSmeltInputLocations().size();
		OUTPUT_SLOTS = this.tileentity.getSmeltOutputLocations().size();
		
		this.createPlayerInv(playerInv);
		this.createControllerInventory();
	}
	
	private void createPlayerInv(InventoryPlayer playerInv) {
		// Add hotbar slots
		for (int i = 0; i < HOTBAR_SLOTS; i++) {
			this.addSlotToContainer(new Slot(playerInv, i, ROW_START + (SPACING * i), HOTBAR_Y_POS));
		}
		
		// Add player inventory slots
		for (int y = 0; y < PLAYER_INV_ROWS; y++) {
			for (int x = 0; x < PLAYER_INV_COLS; x++) {
				int slot = (HOTBAR_SLOTS + y) * (PLAYER_INV_COLS + x);
				int xPos = ROW_START + (x * SPACING);
				int yPos = PLAYER_INV_Y_POS + (y * SPACING);
				this.addSlotToContainer(new Slot(playerInv, slot, xPos, yPos));
			}
		}
	}
	
	private void createControllerInventory() {		
		// Add fuel slots
		for (int fuel = 0; fuel < FUEL_SLOTS; fuel++) {
			int slot = fuel + COMP_FIRST_FUEL_SLOT;
			this.addSlotToContainer(new SlotFuel(this.tileentity, slot, ROW_START + (fuel * SPACING), FUEL_Y_POS));
		}
		
		// Add input slots
		for (int in = 0; in < INPUT_SLOTS; in++) {
			int slot = in + COMP_FIRST_INPUT_SLOT;
			
			if (in <= 8) {
				this.addSlotToContainer(new SlotInput(this.tileentity, slot, ROW_START + (in * SPACING), INPUT_Y_POS));
			}
			else if (in > 8) {
				this.addSlotToContainer(new SlotInput(this.tileentity, slot, ROW_START + ((in - 8) * SPACING), INPUT_Y_POS + SPACING));
			}
		}
		
		// Add output slots
		for (int out = 0; out < OUTPUT_SLOTS; out++) {
			int slot = out + COMP_FIRST_OUTPUT_SLOT;
			
			if (out <= 8) {
				this.addSlotToContainer(new SlotOutput(this.tileentity, slot, ROW_START + (out * SPACING), OUTPUT_Y_POS));
			}
			else if (out > 8) {
				this.addSlotToContainer(new SlotOutput(this.tileentity, slot, ROW_START + ((out - 8) * SPACING), OUTPUT_Y_POS + SPACING));
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return this.tileentity.isUsableByPlayer(playerIn);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		Slot slot = (Slot) this.inventorySlots.get(index);
		
		if (slot == null || !slot.getHasStack()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack source = slot.getStack();
		ItemStack copy = source.copy();
		
		if (index >= FIRST_SLOT && index < FIRST_FUEL_SLOT) {
			if (!this.tileentity.getSmeltingResult(source).isEmpty()) {
				if (!this.mergeItemStack(source, FIRST_INPUT_SLOT, FIRST_INPUT_SLOT + INPUT_SLOTS, false)) {
					return ItemStack.EMPTY;
				}
			}
			else if (this.tileentity.getBurnTime(source) > 0) {
				if (!this.mergeItemStack(source, FIRST_FUEL_SLOT, FIRST_FUEL_SLOT + FUEL_SLOTS, true)) {
					return ItemStack.EMPTY;
				}
			}
			else {
				return ItemStack.EMPTY;
			}
		}
		else if (index >= FIRST_FUEL_SLOT && index < FIRST_FUEL_SLOT + TOTAL_SLOTS) {
			if (!this.mergeItemStack(source, FIRST_SLOT, FIRST_SLOT + PLAYER_TOTAL_SLOTS, false)) {
				return ItemStack.EMPTY;
			}
		}
		else {
			System.err.println("Invalid slot index: " + index);
			return ItemStack.EMPTY;
		}
		
		if (source.getCount() == 0) {
			slot.putStack(ItemStack.EMPTY);
		}
		else {
			slot.onSlotChanged();
		}
		
		slot.onTake(playerIn, source);
		return copy;
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		boolean allChanged = false;
		boolean[] fieldsChanged = new boolean[this.tileentity.getFieldCount()];
		
		if (this.cached == null) {
			this.cached = new int[this.tileentity.getFieldCount()];
			allChanged = true;
		}
		
		for (int i = 0; i < this.cached.length; ++i) {
			if (allChanged || this.cached[i] != this.tileentity.getField(i)) {
				this.cached[i] = this.tileentity.getField(i);
				fieldsChanged[i] = true;
			}
		}
		
		for (IContainerListener listener : this.listeners) {
			for (int field = 0; field < this.tileentity.getFieldCount(); ++field) {
				if (fieldsChanged[field]) {
					listener.sendProgressBarUpdate(this, field, this.cached[field]);
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int id, int data) {
		this.tileentity.setField(id, data);
	}
	
	/************************************************************/
	
	public class SlotFuel extends Slot {

		public SlotFuel(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack) {
			return TileEntityBigFurnace.IsItemValidFuel(stack);
		}
	}
	
	public class SlotInput extends Slot {

		public SlotInput(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack) {
			return true;
		}
	}
	
	public class SlotOutput extends Slot {

		public SlotOutput(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack) {
			return false;
		}
	}
}
