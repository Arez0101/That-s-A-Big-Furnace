package net.arez0101.tabf.tileentity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.cyclopscore.block.multi.AllowedBlock;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.multi.CubeDetector.IDetectionListener;
import org.cyclops.cyclopscore.block.multi.CubeSizeValidator;
import org.cyclops.cyclopscore.block.multi.ExactBlockCountValidator;
import org.cyclops.cyclopscore.block.multi.HollowCubeDetector;
import org.cyclops.cyclopscore.block.multi.MaximumSizeValidator;
import org.cyclops.cyclopscore.block.multi.MinimumSizeValidator;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.LocationHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.cyclopscore.inventory.INBTInventory;
import org.cyclops.cyclopscore.inventory.IndexedInventory;
import org.cyclops.cyclopscore.inventory.IndexedSlotlessItemHandlerWrapper;
import org.cyclops.cyclopscore.inventory.IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference;
import org.cyclops.cyclopscore.inventory.LargeInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.InventoryTileEntityBase;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import gnu.trove.map.TIntObjectMap;
import lombok.experimental.Delegate;
import net.arez0101.tabf.TABF;
import net.arez0101.tabf.config.TABFConfigHandler;
import net.arez0101.tabf.init.BlockInit;
import net.arez0101.tabf.inventory.ContainerBigFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import scala.actors.threadpool.Arrays;

public class TileEntityBigFurnace extends InventoryTileEntityBase implements CyclopsTileEntity.ITickingTile {
	
	/*********************************************************/
	
	// General variables
	
	private static AllowedBlock[] STRUCTURE_BLOCKS = new AllowedBlock[] {new AllowedBlock(BlockInit.WALL), new AllowedBlock(BlockInit.FUEL_INPUT),
			new AllowedBlock(BlockInit.SMELTING_INPUT), new AllowedBlock(BlockInit.SMELTING_OUTPUT),
			new AllowedBlock(BlockInit.BIG_FURNACE).addCountValidator(new ExactBlockCountValidator(1))};
	
	@SuppressWarnings("unchecked")
	public static CubeDetector detector = new HollowCubeDetector(STRUCTURE_BLOCKS, (List<? extends IDetectionListener>) Lists.newArrayList(BlockInit.BIG_FURNACE, BlockInit.FUEL_INPUT, BlockInit.SMELTING_INPUT, BlockInit.SMELTING_OUTPUT, BlockInit.WALL))
	.addSizeValidator(new MinimumSizeValidator(new Vec3i(1, 1, 1)))
	.addSizeValidator(new CubeSizeValidator())
	.addSizeValidator(new MaximumSizeValidator(getMaxSize()) {
		
		@Override
		public Vec3i getMaximumSize() {
			return getMaxSize();
		}
	});
	
	@Delegate
	private final ITickingTile tickingTile = new TickingTileComponent(this);
	
	@NBTPersist
	private Vec3i size = LocationHelpers.copyLocation(Vec3i.NULL_VECTOR);
	
	@NBTPersist
	private String name = null;
	
	@NBTPersist(useDefaultValue = false)
	private static List<Vec3i> fuelInputLocations = Lists.newArrayList();
	
	@NBTPersist(useDefaultValue = false)
	private static List<Vec3i> smeltInputLocations = Lists.newArrayList();
	
	@NBTPersist(useDefaultValue = false)
	private static List<Vec3i> smeltOutputLocations = Lists.newArrayList();
	
	@NBTPersist
	private Vec3d renderOffset = new Vec3d(0, 0, 0);
	
	@NBTPersist
	private SimpleInventory lastValidInventory = null;
	
	private SimpleInventory inventory = null;
	private int playersUsing;
	private boolean setNullInventory = true;
	private Block block = BlockInit.BIG_FURNACE;
	private EnumFacingMap<int[]> facingSlots = EnumFacingMap.newMap();
	private int maxSize = TABFConfigHandler.maxFurnaceSize;

	/*********************************************************/
	
	// Furnace variables
	private int FUEL_SLOTS = fuelInputLocations.size() + 1 <= 9 ? fuelInputLocations.size() + 1 : 9;
	private int INPUT_SLOTS = smeltInputLocations.size();
	private int OUTPUT_SLOTS = smeltOutputLocations.size();
	private int TOTAL_SLOTS = FUEL_SLOTS + INPUT_SLOTS + OUTPUT_SLOTS;
	private int FIRST_FUEL = 0;
	private int FIRST_INPUT = FIRST_FUEL + FUEL_SLOTS;
	private int FIRST_OUTPUT = FIRST_INPUT + INPUT_SLOTS;
	private int[] burnRemaining = new int[FUEL_SLOTS];
	private int[] burnInitial = new int[FUEL_SLOTS];
	private short cookTime = 0;
	private short cookTimeRequired = 200;
	private int cachedBurningSlots = -1;
	private int controlTicks = 0;

	/*********************************************************/
	
	// Field variables
	private byte cookField = 0;
	private byte firstBurnRemainField = 1;
	private byte firstBurnInitialField = (byte) (firstBurnRemainField + (byte) FUEL_SLOTS);
	private byte totalFields = (byte) (firstBurnInitialField + (byte) FUEL_SLOTS);

	/*********************************************************/
	
	public TileEntityBigFurnace() {		
		if (Capabilities.SLOTLESS_ITEM_HANDLER != null) {
			this.addSlotlessItemHandler();
		}
	}

	/*****************************************************************/

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.playersUsing = type;
		}
		return true;
	}

	private void triggerPlayerChange(int change) {
		if (this.world != null) {
			this.playersUsing += change;
			this.world.addBlockEvent(this.getPos(), this.block, 1, this.playersUsing);
		}
	}

	@Override
	protected boolean canAccess(int slot, EnumFacing side) {
		return this.getSingleSize() > 1 && super.canAccess(slot, side);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return this.getSingleSize() > 1 && super.canInteractWith(entityPlayer);
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if (this.world.getTileEntity(this.getPos()) != this) return false;
		return player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
	}

	/*****************************************************************/
	
	public void setCenter(Vec3d center) {
		EnumFacing rotation;
		double x = Math.abs(center.xCoord - this.getPos().getX());
		double z = Math.abs(center.zCoord - this.getPos().getZ());
		boolean equalLength = x == z;
		
		if (x > z || (!equalLength && this.getSingleSize() == 2)) {
			rotation = DirectionHelpers.getEnumFacingFromXSign((int) Math.round(x));
		}
		else {
			rotation = DirectionHelpers.getEnumFacingFromZSing((int) Math.round(z));
		}
		
		this.setRotation(rotation);
		this.renderOffset = new Vec3d(this.getPos().getX() - center.xCoord, this.getPos().getY() - center.yCoord, this.getPos().getZ() - center.zCoord);
	}
	
	private static Vec3i getMaxSize() {
		int maxSize = TABFConfigHandler.maxFurnaceSize;
		return new Vec3i(maxSize, maxSize, maxSize);
	}
	
	public Vec3i getSize() {
		return this.size;
	}
	
	public int getSingleSize() {
		return this.getSize().getX() + 1;
	}
	
	public void setSize(Vec3i sizeIn) {
		this.size = sizeIn;
		this.facingSlots.clear();
		
		if (this.isFurnaceBuilt()) {
			this.inventory = this.constructInventory();
			
			if (this.lastValidInventory != null) {
				int slot = 0;
				
				while (slot < Math.min(this.lastValidInventory.getSizeInventory(), this.inventory.getSizeInventory())) {
					ItemStack inventory = this.lastValidInventory.getStackInSlot(slot);
					
					if (!inventory.isEmpty()) {
						this.inventory.setInventorySlotContents(slot, inventory);
						this.lastValidInventory.setInventorySlotContents(slot, ItemStack.EMPTY);
					}
					slot++;
				}
				
				if (slot < this.lastValidInventory.getSizeInventory()) {
					MinecraftHelpers.dropItems(this.getWorld(), this.lastValidInventory, this.getPos());
				}
				this.lastValidInventory = null;
			}
		}
		else {
			fuelInputLocations.clear();
			smeltInputLocations.clear();
			smeltOutputLocations.clear();
			
			if (this.inventory != null) {
				if (TABFConfigHandler.ejectItems) {
					MinecraftHelpers.dropItems(this.getWorld(), this.inventory, this.getPos());
					this.lastValidInventory = null;
				}
				else {
					this.lastValidInventory = this.inventory;
				}
			}
			this.inventory = new LargeInventory(0, "invalid", 0);
		}
		this.sendUpdate();
	}
	
	private SimpleInventory constructInventory() {
		return new IndexedInventory(calculateInventorySize(), null, 64);
	}

	private int calculateInventorySize() {
		return (int) (this.getSingleSize() * 9);
	}

	protected void addSlotlessItemHandler() {
		IItemHandler handler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		this.addCapabilityInternal(Capabilities.SLOTLESS_ITEM_HANDLER,
				new IndexedSlotlessItemHandlerWrapper(handler, new IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference() {
					
					@Override
					public int getLastNonEmptySlot() {
						return ((IInventoryIndexReference) getInventory()).getLastNonEmptySlot();
					}
					
					@Override
					public int getLastEmptySlot() {
						return ((IInventoryIndexReference) getInventory()).getLastEmptySlot();
					}
					
					@Override
					public int getInventoryStackLimit() {
						return getInventory().getInventoryStackLimit();
					}
					
					@Override
					public Map<Item, TIntObjectMap<ItemStack>> getIndex() {
						return ((IInventoryIndexReference) getInventory()).getIndex();
					}
					
					@Override
					public int getFirstNonEmptySlot() {
						return ((IInventoryIndexReference) getInventory()).getFirstNonEmptySlot();
					}
					
					@Override
					public int getFirstEmptySlot() {
						return ((IInventoryIndexReference) getInventory()).getFirstEmptySlot();
					}
				}));
	}
	
	/*****************************************************************/
	
	public boolean isFurnaceBuilt() {
		return !this.getSize().equals(Vec3i.NULL_VECTOR);
	}
	
	public static void detectStructure(World worldIn, BlockPos pos, Vec3i size, boolean valid, BlockPos origin) {}

	/*****************************************************************/

	public static void addFuelInput(BlockPos pos) {
		fuelInputLocations.add(pos);
	}

	public static void addSmeltInput(BlockPos pos) {
		smeltInputLocations.add(pos);
	}

	public static void addSmeltOutput(BlockPos pos) {
		smeltOutputLocations.add(pos);
	}

	public static List<Vec3i> getFuelInputLocation() {
		return Collections.unmodifiableList(fuelInputLocations);
	}

	public static List<Vec3i> getSmeltInputLocations() {
		return Collections.unmodifiableList(smeltInputLocations);
	}

	public static List<Vec3i> getSmeltOutputLocations() {
		return Collections.unmodifiableList(smeltOutputLocations);
	}

	/*****************************************************************/
	
	public double fractionFuelRemaining(int index) {
		if (this.burnInitial[index] <= 0) return 0;
		
		double fraction = this.burnRemaining[index] / (double) this.burnInitial[index];
		return MathHelper.clamp(fraction, 0.0, 1.0);
	}
	
	public int secondsFuelRemaining(int index) {
		return this.burnRemaining[index] / 20;
	}
	
	public double fractionCookComplete() {
		double fraction = this.cookTime / (double) this.cookTimeRequired;
		return MathHelper.clamp(fraction, 0.0, 1.0);
	}
	
	public int numberFuelSlotsBurning() {
		int burning = 0;
		
		for (int burnTime : this.burnRemaining) {
			if (burnTime > 0) ++burning;
		}
		
		return burning;
	}
	
	private int burnFuel() {
		int burning = 0;
		boolean invChanged = false;
		
		for (int i = 0; i < FUEL_SLOTS; i++) {
			int fuelSlot = i + FIRST_FUEL;
			
			if (this.burnRemaining[i] > 0) {
				--this.burnRemaining[i];
				++burning;
			}
			
			if (this.burnRemaining[i] == 0) {
				if (!this.inventory.getStackInSlot(fuelSlot).isEmpty() && this.getBurnTime(this.inventory.getStackInSlot(fuelSlot)) > 0) {
					this.burnRemaining[i] = this.burnInitial[i] = this.getBurnTime(this.inventory.getStackInSlot(fuelSlot));
					this.inventory.getStackInSlot(fuelSlot).splitStack(1);
					++burning;
					invChanged = true;
					
					if (this.inventory.getStackInSlot(fuelSlot).getCount() == 0) {
						this.inventory.setInventorySlotContents(fuelSlot, this.inventory.getStackInSlot(fuelSlot).getItem().getContainerItem(this.inventory.getStackInSlot(fuelSlot)));
					}
				}
			}
		}
		
		if (invChanged) this.markDirty();
		return burning;
	}
	
	public short getBurnTime(ItemStack stackIn) {
		int burntime = TileEntityFurnace.getItemBurnTime(stackIn) / this.numberFuelSlotsBurning();
		return (short) MathHelper.clamp(burntime, 0, Short.MAX_VALUE);
	}
	
	public ItemStack getSmeltingResult(ItemStack stackIn) {
		return FurnaceRecipes.instance().getSmeltingResult(stackIn);
	}
	
	private boolean canSmelt() {
		return this.smelt(false);
	}
	
	private void smelt() {
		this.smelt(true);
	}
	
	private boolean smelt(boolean run) {
		Integer firstAvailableInput = null;
		Integer firstAvailableOutput = null;
		ItemStack result = ItemStack.EMPTY;
		
		for (int input = FIRST_INPUT; input < FIRST_OUTPUT; input++) {
			if (!this.inventory.getStackInSlot(input).isEmpty()) {
				result = this.getSmeltingResult(this.inventory.getStackInSlot(input));
			
				if (!result.isEmpty()) {
					for (int output = FIRST_OUTPUT; output < TOTAL_SLOTS; output++) {
						ItemStack outputStack = this.inventory.getStackInSlot(output);

						if (outputStack.isEmpty()) {
							firstAvailableInput = input;
							firstAvailableOutput = output;
							break;
						}

						if (outputStack.getItem() == result.getItem()
								&& (!outputStack.getHasSubtypes() || outputStack.getMetadata() == outputStack.getMetadata())
								&& ItemStack.areItemStackTagsEqual(outputStack, result)) {

							int combined = this.inventory.getStackInSlot(output).getCount() + result.getCount();

							if (combined <= this.getInventoryStackLimit() && combined <= this.inventory.getStackInSlot(output).getMaxStackSize()) {
								firstAvailableInput = input;
								firstAvailableOutput = output;
								break;
							}
						}
					}
					if (firstAvailableInput != null) break;
				}
			}
		}
		
		if (firstAvailableInput == null) return false;
		
		if (!run) return true;
		
		this.inventory.getStackInSlot(firstAvailableInput).splitStack(1);
		
		if (this.inventory.getStackInSlot(firstAvailableInput).getCount() <= 0) {
			this.inventory.setInventorySlotContents(firstAvailableInput, ItemStack.EMPTY);
		}
		
		if (this.inventory.getStackInSlot(firstAvailableOutput).isEmpty()) {
			this.inventory.setInventorySlotContents(firstAvailableOutput, result.copy());
		}
		else {
			int newCount = this.inventory.getStackInSlot(firstAvailableOutput).getCount() + result.getCount();
			this.inventory.getStackInSlot(firstAvailableOutput).setCount(newCount);
		}
		
		this.markDirty();
		return true;
	}

	/*********************************************************/
	
	@Override
	public String getName() {
		return "container.furnace_controller.name";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
	}

	/*********************************************************/
	
	@Override
	public int getSizeInventory() {
		return this.inventory.getSizeInventory();
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return this.inventory.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = this.getStackInSlot(index);
		
		if (stack.isEmpty()) return ItemStack.EMPTY;
		
		ItemStack removed;
		
		if (stack.getCount() <= count) {
			removed = stack;
			this.setInventorySlotContents(index, ItemStack.EMPTY);
		}
		else {
			removed = stack.splitStack(count);
			
			if (stack.getCount() == 0) {
				this.setInventorySlotContents(index, ItemStack.EMPTY);
			}
		}
		
		this.markDirty();
		return removed;
	}
	
	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack stack = this.getStackInSlot(index);
		
		if (!stack.isEmpty()) {
			this.setInventorySlotContents(index, (ItemStack) null);
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.inventory.setInventorySlotContents(index, stack);
		
		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
		
		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		this.triggerPlayerChange(1);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		this.triggerPlayerChange(-1);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index < FIRST_INPUT && TileEntityFurnace.isItemFuel(stack)) {
			return true;
		}
		else if (index >= FIRST_INPUT && index < FIRST_OUTPUT) {
			return true;
		}
		return false;
	}
	
	public static boolean IsItemValidFuel(ItemStack stack) {
		return TileEntityFurnace.isItemFuel(stack);
	}
	
	@Override
	public void clear() {
		for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
			this.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return canAccess(index, direction) && this.isItemValidForSlot(index, itemStackIn);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return canAccess(index, direction);
	}
	
	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		if (side == null) {
			side = EnumFacing.NORTH;
		}
		
		int[] slots = this.facingSlots.get(side);
		
		if (slots == null) {
			ContiguousSet<Integer> ints = ContiguousSet.create(Range.closed(0, getSizeInventory()), DiscreteDomain.integers());
			slots = ArrayUtils.toPrimitive(ints.toArray(new Integer[ints.size()]));
			this.facingSlots.put(side, slots);
		}
		return slots;
	}
	
	@Override
	public INBTInventory getInventory() {
		if (this.world != null && this.world.isRemote && (this.inventory == null || this.inventory.getSizeInventory() != this.calculateInventorySize())) {
			return this.inventory = this.constructInventory();
		}
		
		if (this.lastValidInventory != null) {
			return this.lastValidInventory;
		}
		
		if (this.inventory == null && this.setNullInventory) {
			this.inventory = this.constructInventory();
		}
		
		return this.inventory;
	}

	/*********************************************************/
	
	@Override
	public int getField(int id) {
		if (id == this.cookField) {
			return this.cookTime;
		}
		else if (id >= this.firstBurnRemainField && id < this.firstBurnInitialField) {
			return this.burnRemaining[id - this.firstBurnRemainField];
		}
		else if (id >= this.firstBurnInitialField && id < this.totalFields) {
			return this.burnInitial[id - this.firstBurnInitialField];
		}
		else {
			System.err.println("Invalid field ID in " + TABF.MODID + ":TileEntityController.getField(int id): " + id);
			return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		if (id == this.cookField) {
			this.cookTime = (short) value;
		}
		else if (id >= this.firstBurnRemainField && id < this.firstBurnInitialField) {
			this.burnRemaining[id - this.firstBurnRemainField] = value;
		}
		else if (id >= this.firstBurnInitialField && id < this.totalFields) {
			this.burnInitial[id - this.firstBurnInitialField] = value;
		}
		else {
			System.err.println("Invalid field ID in " + TABF.MODID + ":TileEntityController.setField(int id, int value): " + id);
		}
	}

	@Override
	public int getFieldCount() {
		return this.totalFields;
	}
	
	/*********************************************************/
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		
		NBTTagList slotData = new NBTTagList();
		
		for (int i = 0; i < this.inventory.getSizeInventory(); ++i) {
			if (!this.inventory.getStackInSlot(i).isEmpty()) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				this.inventory.getStackInSlot(i).writeToNBT(tag);
				slotData.appendTag(tag);
			}
		}
		
		compound.setTag("Items", slotData);
		compound.setShort("CookTime", this.cookTime);
		compound.setTag("BurnRemaining", new NBTTagIntArray(this.burnRemaining));
		compound.setTag("BurnInitial", new NBTTagIntArray(this.burnInitial));
		return compound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		NBTTagList slotData = compound.getTagList("Items", 10);
		this.inventory.clear();
		
		for (int i = 0; i < slotData.tagCount(); ++i) {
			NBTTagCompound currentSlot = slotData.getCompoundTagAt(i);
			byte slotNum = currentSlot.getByte("Slot");
			
			if (slotNum >= 0 && slotNum < this.inventory.getSizeInventory()) {
				this.inventory.setInventorySlotContents(i, new ItemStack(currentSlot));
			}
		}
		
		this.cookTime = compound.getShort("CookTime");
		this.burnRemaining = Arrays.copyOf(compound.getIntArray("BurnRemaining"), FUEL_SLOTS);
		this.burnInitial = Arrays.copyOf(compound.getIntArray("BurnInitial"), FUEL_SLOTS);
		this.cachedBurningSlots = -1;
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return tag;
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.getPos(), 0, this.getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.handleUpdateTag(pkt.getNbtCompound());
	}

	/*********************************************************/
	
	@Override
	public void updateTileEntity() {
		super.updateTileEntity();
		
		if (this.getWorld() != null && !this.getWorld().isRemote && this.playersUsing != 0 && WorldHelpers.efficientTick(this.getWorld(), 200, this.getPos().hashCode())) {
			this.playersUsing = 0;
			float range = 5.0F;
			
			@SuppressWarnings("unchecked")
			List<EntityPlayer> players = this.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(
					this.getPos().add(new Vec3i(-range, -range, -range)),
					this.getPos().add(new Vec3i(1 + range, 1 + range, 1 + range))
					)
			);
			
			for (EntityPlayer player : players) {
				if (player.openContainer instanceof ContainerBigFurnace) {
					++this.playersUsing;
				}
			}
			
			this.getWorld().addBlockEvent(this.getPos(), this.block, 1, this.playersUsing);
		}
		
		++this.controlTicks;
		
		if (this.controlTicks >= 1000000) {
			this.controlTicks = 0;
		}
		
		if (this.isFurnaceBuilt()) {
			if (this.canSmelt()) {
				int fuelBurning = this.burnFuel();
				
				if (fuelBurning > 0) {
					this.cookTime += fuelBurning;
				}
				else {
					this.cookTime -= 2;
				}
				
				if (this.cookTime < 0) this.cookTime = 0;
				
				if (cookTime >= this.cookTimeRequired) {
					this.smelt();
					this.cookTime = 0;
				}
			}
			else {
				this.cookTime = 0;
			}
		}
		
		if (this.cachedBurningSlots != this.numberFuelSlotsBurning()) {
			this.cachedBurningSlots = this.numberFuelSlotsBurning();
			
			if (this.world.isRemote) {
				IBlockState state = this.world.getBlockState(this.getPos());
				this.world.notifyBlockUpdate(this.getPos(), state, state, 3);
			}
			this.world.checkLightFor(EnumSkyBlock.BLOCK, this.getPos());
		}
	}

	/*********************************************************/
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		// TODO
		return null;
	}
	
	public void setRenderOffset(Vec3d renderOffset) {
		this.renderOffset = renderOffset;
	}
	
	/*********************************************************/
	
	public static class Capabilities {

		@CapabilityInject(IInventory.class)
		public static Capability<IInventory> INVENTORY_STATE = null;

		@CapabilityInject(ISlotlessItemHandler.class)
		public static Capability<ISlotlessItemHandler> SLOTLESS_ITEM_HANDLER = null;
	}
}
