package net.arez0101.tabf.block;

import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;

import net.arez0101.tabf.tileentity.TileEntityBigFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWall extends ConfigurableBlock implements CubeDetector.IDetectionListener {
	
	@BlockProperty
	public static final PropertyBool ACTIVE = BlockController.ACTIVE;

	public BlockWall(ExtendedConfig<BlockConfig> eConfig) {
		super(eConfig, Material.ROCK);
		this.setHardness(5.0F);
		this.setHarvestLevel("pickaxe", 0);
		this.setSoundType(SoundType.STONE);
		this.setResistance(2500.0F);
	}
	
	@Override
	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
		return false;
	}

	@Override
	public void onDetect(World world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
		Block block = world.getBlockState(location).getBlock();
		
		if (block == this) {
			boolean change = !(Boolean) world.getBlockState(location).getValue(ACTIVE);
			world.setBlockState(location, world.getBlockState(location).withProperty(ACTIVE, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
			
			if (change) {
				TileEntityBigFurnace.detectStructure(world, location, size, valid, originCorner);
			}
		}
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		
		if (!worldIn.captureBlockSnapshots) {
			BlockController.triggerDetector(worldIn, pos, true);
		}
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		
		if (!worldIn.captureBlockSnapshots && state.getBlock() == this && !state.getValue(ACTIVE)) {
			BlockController.triggerDetector(worldIn, pos, true);
		}
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if ((Boolean) state.getValue(ACTIVE)) {
			BlockController.triggerDetector(worldIn, pos, false);
		}
		
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if (state.getValue(ACTIVE)) {
			BlockPos tileLoc = BlockController.getCoreLocation(worldIn, pos);
			
			if (tileLoc != null) {
				worldIn.getBlockState(tileLoc).getBlock().onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
				return true;
			}
		}
		
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta * 2, placer, hand);
	}
}
