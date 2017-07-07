package net.arez0101.tabf.gui;

import java.awt.Color;

import net.arez0101.tabf.TABF;
import net.arez0101.tabf.inventory.ContainerBigFurnace;
import net.arez0101.tabf.tileentity.TileEntityBigFurnace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiBigFurnace extends GuiContainer {
	
	private final ResourceLocation texture = new ResourceLocation(TABF.MODID, "textures/gui/controller_gui.png");
	private TileEntityBigFurnace tileentity;
	private int inputSlots;
	private int outputSlots;
	private int fuelSlots;
	
	/** Variables for dynamically creating gui
	 *  Slot locations on texture
	 *  Flame locations on texture
	 *  Arrow locations on texture
	 *  Misc texture location variables */	
	private final int SLOT_U = 176;
	private final int SLOT_V = 36;
	private final int SLOT_WIDTH = 18;
	private final int SLOT_HEIGHT = 18;
	private final int SLOT_START_X = 7;
	private final int SLOT_INPUT_START_Y = 5;
	private final int SLOT_OUTPUT_START_Y = 119;
	private final int SLOT_FUEL_START_Y = 60;
	
	private final int FLAME_START_X_POS = 9;
	private final int FLAME_Y_POS = 45;
	private final int FLAME_ON_U = 176;
	private final int FLAME_OFF_U = 196;
	private final int FLAME_V = 0;
	private final int FLAME_WIDTH = 14;
	private final int FLAME_HEIGHT = 14;
	private final int FLAME_SPACING = 18;
	
	private final int ARROW_X_POS = 82;
	private final int ARROW_Y_POS = 78;
	private final int ARROW_ON_U = 176;
	private final int ARROW_OFF_U = 192;
	private final int ARROW_V = 14;
	private final int ARROW_WIDTH = 15;
	private final int ARROW_HEIGHT = 22;

	public GuiBigFurnace(InventoryPlayer playerInv, TileEntityBigFurnace tileEntityIn) {
		super(new ContainerBigFurnace(playerInv, tileEntityIn));
		this.xSize = 176;
		this.ySize = 244;
		this.tileentity = tileEntityIn;
		this.fuelSlots = this.tileentity.getFuelInputLocation().size();
		this.inputSlots = this.tileentity.getSmeltInputLocations().size();
		this.outputSlots = this.tileentity.getSmeltOutputLocations().size();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		
		// Draw background
		Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		
		// Draw input slots
		for (int slot = 0; slot < this.inputSlots; slot++) {
			int x;
			int y;
			if (slot <= 8) {
				x = this.guiLeft + SLOT_START_X + (18 * slot);
				y = this.guiTop + SLOT_INPUT_START_Y;
			}
			else {
				x = this.guiLeft + SLOT_START_X + (SLOT_WIDTH * (slot - 8));
				y = this.guiTop + SLOT_INPUT_START_Y + SLOT_HEIGHT;
			}
			this.drawTexturedModalRect(x, y, SLOT_U, SLOT_V, SLOT_WIDTH, SLOT_HEIGHT);
		}
		
		// Draw fuel slots
		for (int slot = 0; slot < this.fuelSlots; slot++) {
			int x = this.guiLeft + SLOT_START_X + (SLOT_WIDTH * slot);
			int y = this.guiTop + SLOT_FUEL_START_Y;
			this.drawTexturedModalRect(x, y, SLOT_U, SLOT_V, SLOT_WIDTH, SLOT_HEIGHT);
		}
		
		// Draw flames
		for (int flame = 0; flame < this.fuelSlots; flame++) {
			double burnRemaining = this.tileentity.fractionFuelRemaining(flame);
			int yOffset = (int) ((1.0 - burnRemaining) * FLAME_HEIGHT);
			int x = this.guiLeft + FLAME_START_X_POS + ((FLAME_SPACING) * flame);
			int y = this.guiTop + FLAME_Y_POS;
			
			// Draw regular flame icon
			this.drawTexturedModalRect(x, y, FLAME_OFF_U, FLAME_V, FLAME_WIDTH, FLAME_HEIGHT);
			
			// Draw animated flame
			this.drawTexturedModalRect(x, y + yOffset, FLAME_ON_U, FLAME_V + yOffset, FLAME_WIDTH, FLAME_HEIGHT - yOffset);
		}
		
		// Draw progress bar
		double progress = this.tileentity.fractionCookComplete();
		this.drawTexturedModalRect(this.guiLeft + ARROW_X_POS, this.guiTop + ARROW_Y_POS, ARROW_OFF_U, ARROW_V, ARROW_WIDTH, ARROW_HEIGHT);
		this.drawTexturedModalRect(this.guiLeft + ARROW_X_POS, this.guiTop + ARROW_Y_POS, ARROW_ON_U, ARROW_V, ARROW_WIDTH, (int) (progress * ARROW_HEIGHT));
		
		// Draw output slots
		for (int slot = 0; slot < this.outputSlots; slot++) {
			int x;
			int y;
			if (slot <= 8) {
				x = this.guiLeft + SLOT_START_X + (18 * slot);
				y = this.guiTop + SLOT_OUTPUT_START_Y;
			}
			else {
				x = this.guiLeft + SLOT_START_X + (SLOT_WIDTH * (slot - 8));
				y = this.guiTop + SLOT_OUTPUT_START_Y + SLOT_HEIGHT;
			}
			this.drawTexturedModalRect(x, y, SLOT_U, SLOT_V, SLOT_WIDTH, SLOT_HEIGHT);
		}
	}
}
