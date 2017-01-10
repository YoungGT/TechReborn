package techreborn.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import reborncore.api.power.EnumPowerTier;
import reborncore.api.tile.IInventoryProvider;
import reborncore.common.IWrenchable;
import reborncore.common.powerSystem.TilePowerAcceptor;
import reborncore.common.util.Inventory;
import reborncore.common.util.ItemUtils;

import techreborn.api.RollingMachineRecipe;
import techreborn.client.container.IContainerProvider;
import techreborn.client.container.builder.BuiltContainer;
import techreborn.client.container.builder.ContainerBuilder;
import techreborn.init.ModBlocks;

//TODO add tick and power bars.
public class TileRollingMachine extends TilePowerAcceptor
		implements IWrenchable, IInventoryProvider, IContainerProvider {

	public final InventoryCrafting craftMatrix = new InventoryCrafting(new RollingTileContainer(), 3, 3);
	public Inventory inventory = new Inventory(3, "TileRollingMachine", 64, this);
	public boolean isRunning;
	public int tickTime;
	public int runTime = 250;
	public ItemStack currentRecipe;

	public int euTick = 5;

	public TileRollingMachine() {
		super(1);
	}

	@Override
	public double getMaxPower() {
		return 100000;
	}

	@Override
	public boolean canAcceptEnergy(final EnumFacing direction) {
		return true;
	}

	@Override
	public boolean canProvideEnergy(final EnumFacing direction) {
		return false;
	}

	@Override
	public double getMaxOutput() {
		return 0;
	}

	@Override
	public double getMaxInput() {
		return 64;
	}

	@Override
	public EnumPowerTier getTier() {
		return EnumPowerTier.LOW;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		this.charge(2);
		if (!this.world.isRemote) {
			this.currentRecipe = RollingMachineRecipe.instance.findMatchingRecipe(this.craftMatrix, this.world);
			if (this.currentRecipe != null && this.canMake()) {
				if (this.tickTime >= this.runTime) {
					this.currentRecipe = RollingMachineRecipe.instance.findMatchingRecipe(this.craftMatrix, this.world);
					if (this.currentRecipe != null) {
						boolean hasCrafted = false;
						if (this.inventory.getStackInSlot(0) == null) {
							this.inventory.setInventorySlotContents(0, this.currentRecipe);
							this.tickTime = -1;
							hasCrafted = true;
						} else {
							if (this.inventory.getStackInSlot(0).stackSize + this.currentRecipe.stackSize <= this.currentRecipe
									.getMaxStackSize()) {
								final ItemStack stack = this.inventory.getStackInSlot(0);
								stack.stackSize = (stack.stackSize + this.currentRecipe.stackSize);
								this.inventory.setInventorySlotContents(0, stack);
								this.tickTime = -1;
								hasCrafted = true;
							}
						}
						if (hasCrafted) {
							for (int i = 0; i < this.craftMatrix.getSizeInventory(); i++) {
								this.craftMatrix.decrStackSize(i, 1);
							}
							this.currentRecipe = null;
						}
					}
				}
			}
			if (this.currentRecipe != null) {
				if (this.canUseEnergy(this.euTick) && this.tickTime < this.runTime) {
					this.useEnergy(this.euTick);
					this.tickTime++;
				}
			}
			if (this.currentRecipe == null) {
				this.tickTime = -1;
			}
		} else {
			this.currentRecipe = RollingMachineRecipe.instance.findMatchingRecipe(this.craftMatrix, this.world);
			if (this.currentRecipe != null) {
				this.inventory.setInventorySlotContents(1, this.currentRecipe);
			} else {
				this.inventory.setInventorySlotContents(1, null);
			}
		}
	}

	public boolean canMake() {
		return RollingMachineRecipe.instance.findMatchingRecipe(this.craftMatrix, this.world) != null;
	}

	@Override
	public boolean wrenchCanSetFacing(final EntityPlayer entityPlayer, final EnumFacing side) {
		return false;
	}

	@Override
	public EnumFacing getFacing() {
		return this.getFacingEnum();
	}

	@Override
	public boolean wrenchCanRemove(final EntityPlayer entityPlayer) {
		return entityPlayer.isSneaking();
	}

	@Override
	public float getWrenchDropRate() {
		return 1.0F;
	}

	@Override
	public ItemStack getWrenchDrop(final EntityPlayer entityPlayer) {
		return new ItemStack(ModBlocks.ROLLING_MACHINE, 1);
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		ItemUtils.readInvFromNBT(this.craftMatrix, "Crafting", tagCompound);
		this.isRunning = tagCompound.getBoolean("isRunning");
		this.tickTime = tagCompound.getInteger("tickTime");
	}

	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		ItemUtils.writeInvToNBT(this.craftMatrix, "Crafting", tagCompound);
		this.writeUpdateToNBT(tagCompound);
		return tagCompound;
	}

	public void writeUpdateToNBT(final NBTTagCompound tagCompound) {
		tagCompound.setBoolean("isRunning", this.isRunning);
		tagCompound.setInteger("tickTime", this.tickTime);
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	private static class RollingTileContainer extends Container {

		@Override
		public boolean canInteractWith(final EntityPlayer entityplayer) {
			return true;
		}

	}

	public int getBurnTime() {
		return this.tickTime;
	}

	public void setBurnTime(final int burnTime) {
		this.tickTime = burnTime;
	}

	public int getBurnTimeRemainingScaled(final int scale) {
		if (this.tickTime == 0 || this.runTime == 0) {
			return 0;
		}
		return this.tickTime * scale / this.runTime;
	}

	@Override
	public BuiltContainer createContainer(final EntityPlayer player) {
		return new ContainerBuilder("rollingmachine").player(player.inventory).inventory(8, 84).hotbar(8, 142)
				.addInventory().tile(this.craftMatrix).slot(0, 30, 17).slot(1, 48, 17).slot(2, 66, 17).slot(3, 30, 35)
				.slot(4, 48, 35).slot(5, 66, 35).slot(6, 30, 53).slot(7, 48, 53).slot(8, 66, 53)
				.onCraft(inv -> this.inventory.setInventorySlotContents(1,
						RollingMachineRecipe.instance.findMatchingRecipe(inv, this.world)))
				.addInventory().tile(this).outputSlot(0, 124, 35).energySlot(2, 8, 51).syncEnergyValue()
				.syncIntegerValue(this::getBurnTime, this::setBurnTime).addInventory().create();
	}
}
