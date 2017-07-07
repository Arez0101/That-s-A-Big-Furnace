package net.arez0101.tabf.block;

import javax.annotation.Nullable;

import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.multi.DetectionResult;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.block.property.BlockPropertyManagerComponent;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainerGui;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers.UnlocalizedString;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;

import net.arez0101.tabf.TABF;
import net.arez0101.tabf.gui.GuiBigFurnace;
import net.arez0101.tabf.gui.GuiHandlerBigFurnace;
import net.arez0101.tabf.init.BlockInit;
import net.arez0101.tabf.inventory.ContainerBigFurnace;
import net.arez0101.tabf.tileentity.TileEntityBigFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockController extends ConfigurableBlockContainerGui implements CubeDetector.IDetectionListener {
	
	@BlockProperty
	public final static PropertyBool ACTIVE = PropertyBool.create("active");
	
	@BlockProperty
	private PropertyDirection FACING = PropertyDirection.create("facing");
	
	@BlockProperty
	private PropertyInteger BURNING_SLOTS = PropertyInteger.create("burning", 0, TileEntityBigFurnace.getFuelInputLocation().size());

	public BlockController(ExtendedConfig eConfig) {
		super(eConfig, Material.ROCK, TileEntityBigFurnace.class);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		this.setSoundType(SoundType.STONE);
		this.setHardness(1.5F);
		this.setResistance(2500.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing getFacingFromEntity = EnumFacing.getFacingFromVector((float) (placer.posX - pos.getX()), (float) (placer.posY - pos.getY()), (float) (placer.posZ - pos.getZ()));
		worldIn.setBlockState(pos, state.withProperty(FACING, getFacingFromEntity), 2);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return true;
		
		if (!state.getValue(ACTIVE)) {
			this.playerChatError(worldIn, pos, playerIn, hand);
			return false;
		}
		
		playerIn.openGui(TABF.INSTANCE, GuiHandlerBigFurnace.getGuiID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (worldIn.getTileEntity(pos) instanceof IInventory) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) worldIn.getTileEntity(pos));
		}
		
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		if (worldIn.getTileEntity(pos) instanceof TileEntityBigFurnace) {
			TileEntityBigFurnace te = (TileEntityBigFurnace) worldIn.getTileEntity(pos);
			int burning = te.numberFuelSlotsBurning();
			return this.getDefaultState().withProperty(FACING, state.getValue(FACING)).withProperty(BURNING_SLOTS, burning);
		}
		return state;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta * 2, placer, hand);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		BlockPropertyManagerComponent manager = new BlockPropertyManagerComponent(this, new BlockPropertyManagerComponent.PropertyComparator(){
			
			@Override
			public int compare(IProperty o1, IProperty o2) {
				return o2.getName().compareTo(o1.getName());
			}
			
		}, new BlockPropertyManagerComponent.UnlistedPropertyComparator());
		
		return manager.createDelegatedBlockState();
	}

	@Override
	public Class<? extends Container> getContainer() {
		return ContainerBigFurnace.class;
	}

	@Override
	public Class<? extends GuiScreen> getGui() {
		return GuiBigFurnace.class;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBigFurnace();
	}

	@Override
	public void onDetect(World world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
		Block block = world.getBlockState(location).getBlock();
		
		if (block == this) {
			world.setBlockState(location, world.getBlockState(location).withProperty(ACTIVE, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
			TileEntityBigFurnace te = TileHelpers.getSafeTile(world, location, TileEntityBigFurnace.class);
			
			if (te != null) {
				te.setSize(valid ? size : Vec3i.NULL_VECTOR);
				te.setCenter(new Vec3d(originCorner.getX() + (size.getX() / 2), originCorner.getY() + (size.getY() / 2), originCorner.getZ() + (size.getZ() / 2)));
				te.addFuelInput(location);
				te.addSmeltInput(location);
				te.addSmeltOutput(location);
			}
		}
	}
	
	public static DetectionResult triggerDetector(World worldIn, BlockPos pos, boolean valid) {
		return TileEntityBigFurnace.detector.detect(worldIn, pos, valid ? null : pos, true);
	}
	
	public static void playerChatError(World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand) {
		if (!worldIn.isRemote && playerIn.getHeldItem(hand).isEmpty()) {
			DetectionResult result = TileEntityBigFurnace.detector.detect(worldIn, pos, null, false);
			
			if (result != null && result.getError() != null) {
				playerIn.sendMessage(new TextComponentString(result.getError().localize()));
			}
			else {
				playerIn.sendMessage(new TextComponentString(L10NHelpers.localize("multiblock.tabf.error.unexpected")));
			}
		}
	}
	
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	@Override
	public boolean isKeepNBTOnDrop() {
		return false;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos) && canPlace(worldIn, pos);
	}
	
	public @Nullable static BlockPos getCoreLocation(World worldIn, BlockPos pos) {
		final Wrapper<BlockPos> tilePosWrapper = new Wrapper<BlockPos>();
		
		TileEntityBigFurnace.detector.detect(worldIn, pos, null, new CubeDetector.IValidationAction() {

			@Override
			public UnlocalizedString onValidate(BlockPos location, IBlockState blockState) {
				if (blockState.getBlock() == BlockInit.BIG_FURNACE) {
					tilePosWrapper.set(location);
				}
				return null;
			}
		}, false);
		
		return tilePosWrapper.get();
	}

	public static boolean canPlace(World worldIn, BlockPos pos) {
		for (EnumFacing side : EnumFacing.VALUES) {
			IBlockState state = worldIn.getBlockState(pos.offset(side));
			
			if (state.getProperties().containsKey(ACTIVE) && state.getValue(ACTIVE)) {
				return false;
			}
		}
		return true;
	}
}
