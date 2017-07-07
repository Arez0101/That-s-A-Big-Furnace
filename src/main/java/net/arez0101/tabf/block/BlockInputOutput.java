package net.arez0101.tabf.block;

import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.block.property.BlockPropertyManagerComponent;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;

import net.arez0101.tabf.tileentity.TileEntityBigFurnace;
import net.arez0101.tabf.tileentity.TileEntityInputOutput;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BlockInputOutput extends ConfigurableBlockContainer implements CubeDetector.IDetectionListener {
	
	@BlockProperty
	public static final PropertyBool ACTIVE = BlockController.ACTIVE;
	
	@BlockProperty
	public static final PropertyEnum<Variants> VARIANT = PropertyEnum.<Variants>create("variant", BlockInputOutput.Variants.class);
	
	public static Variants variant;

	public BlockInputOutput(ExtendedConfig eConfig, Variants variantIn) {
		super(eConfig, Material.ROCK, TileEntityInputOutput.class);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		this.setSoundType(SoundType.STONE);
		this.setHardness(5.0F);
		this.setHarvestLevel("pickaxe", 0);
		this.setResistance(2500.0F);
		variant = variantIn;
	}
	
	public Variants getVariant() {
		return variant;
	}

	@Override
	public void onDetect(World world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
		Block block = world.getBlockState(location).getBlock();
		
		if (block == this) {
			boolean change = !(Boolean) world.getBlockState(location).getValue(ACTIVE);
			world.setBlockState(location, world.getBlockState(location).withProperty(ACTIVE, valid).withProperty(VARIANT, this.getVariant()), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
			
			if (change) {
				BlockPos tilePos = BlockController.getCoreLocation(world, location);
				TileEntityInputOutput te = TileHelpers.getSafeTile(world, location, TileEntityInputOutput.class);
				
				if (te != null && tilePos != null) {
					te.setCorePosition(tilePos);
					TileEntityBigFurnace core = TileHelpers.getSafeTile(world, tilePos, TileEntityBigFurnace.class);
					
					if (core != null) {
						if (variant == Variants.FUEL_INPUT) {
							core.addFuelInput(location);
						}
						else if (variant == Variants.SMELT_INPUT) {
							core.addSmeltInput(location);
						}
						else if (variant == Variants.SMELT_OUTPUT) {
							core.addSmeltOutput(location);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onBlockPlacedBy(World arg0, BlockPos arg1, IBlockState arg2, EntityLivingBase arg3, ItemStack arg4) {
		super.onBlockPlacedBy(arg0, arg1, arg2, arg3, arg4);
		BlockController.triggerDetector(arg0, arg1, true);
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		
		if (worldIn.getBlockState(pos).getBlock() != state.getBlock()) {
			BlockController.triggerDetector(worldIn, pos, true);
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos blockPos, IBlockState blockState) {
		if ((Boolean) blockState.getValue(ACTIVE)) {
			BlockController.triggerDetector(world, blockPos, true);
		}
		
		super.breakBlock(world, blockPos, blockState);
	}
	
	@Override
	protected void onPreBlockDestroyed(World world, BlockPos blockPos) {}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (state.getValue(ACTIVE)) {
			BlockPos tilePos = BlockController.getCoreLocation(worldIn, pos);
			
			if (tilePos != null) {
				worldIn.getBlockState(tilePos).getBlock().onBlockActivated(worldIn, tilePos, worldIn.getBlockState(tilePos), playerIn, hand, facing, hitX, hitY, hitZ);
				return true;
			}
		}
		else {
			BlockController.playerChatError(worldIn, pos, playerIn, hand);
		}
		
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		BlockPropertyManagerComponent manager = new BlockPropertyManagerComponent(this, new BlockPropertyManagerComponent.PropertyComparator() {
			
			@Override
			public int compare(IProperty o1, IProperty o2) {
				return o2.getName().compareTo(o1.getName());
			}
			
		}, new BlockPropertyManagerComponent.UnlistedPropertyComparator());
		
		return manager.createDelegatedBlockState();
	}
	
	@Override
	public IBlockState getStateForPlacement(World arg0, BlockPos arg1, EnumFacing arg2, float arg3, float arg4, float arg5, int arg6, EntityLivingBase arg7, EnumHand arg8) {
		return super.getStateForPlacement(arg0, arg1, arg2, arg3, arg4, arg5, arg6 * 2, arg7, arg8);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos) && BlockController.canPlace(worldIn, pos);
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).ordinal();
	}
	
	@Override
	public boolean isKeepNBTOnDrop() {
		return false;
	}
	
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	public enum Variants implements IStringSerializable {
		
		FUEL_INPUT("fuel_in"),
		SMELT_INPUT("smelt_in"),
		SMELT_OUTPUT("smelt_out");
		
		private String name;
		
		private Variants(String nameIn) {
			this.name= nameIn;
		}

		@Override
		public String getName() {
			return this.name;
		}
	}
}
