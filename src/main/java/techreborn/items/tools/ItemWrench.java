/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.items.tools;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.BlockPosContext;
import reborncore.common.IWrenchable;
import reborncore.common.util.RebornPermissions;
import techreborn.client.TechRebornCreativeTabMisc;
import techreborn.compat.CompatManager;
import techreborn.init.ModSounds;
import techreborn.items.ItemTR;
import techreborn.utils.IC2WrenchHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by modmuss50 on 26/02/2016.
 */
public class ItemWrench extends ItemTR {

	public ItemWrench() {
		setCreativeTab(TechRebornCreativeTabMisc.instance);
		setUnlocalizedName("techreborn.wrench");
		setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
	                                  EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!PermissionAPI.hasPermission(player.getGameProfile(), RebornPermissions.WRENCH_BLOCK, new BlockPosContext(player, pos, world.getBlockState(pos), facing))) {
			return EnumActionResult.PASS;
		}
		if (CompatManager.isIC2Loaded) {
			EnumActionResult result = IC2WrenchHelper.onItemUse(player.getHeldItem(hand), player, world, pos, hand, facing, hitX, hitY, hitZ);
			if (result == EnumActionResult.SUCCESS) {
				return result;
			}
		}
		if (world.isAirBlock(pos)) {
			return EnumActionResult.PASS;
		}
		TileEntity tile = world.getTileEntity(pos);
		if (tile == null) {
			return EnumActionResult.PASS;
		}
		if (!world.isRemote) {
			if (player.isSneaking()) {
				List<ItemStack> items = new ArrayList<>();
				if (tile instanceof IWrenchable) {
					if (((IWrenchable) tile).wrenchCanRemove(player)) {
						ItemStack itemStack = ((IWrenchable) tile).getWrenchDrop(player);
						if (itemStack == null) {
							return EnumActionResult.FAIL;
						}
						items.add(itemStack);
					}
					if (!items.isEmpty()) {
						for (ItemStack itemStack : items) {

							Random rand = new Random();

							float dX = rand.nextFloat() * 0.8F + 0.1F;
							float dY = rand.nextFloat() * 0.8F + 0.1F;
							float dZ = rand.nextFloat() * 0.8F + 0.1F;

							EntityItem entityItem = new EntityItem(world, pos.getX() + dX, pos.getY() + dY,
								pos.getZ() + dZ, itemStack.copy());

							if (itemStack.hasTagCompound()) {
								entityItem.getItem()
									.setTagCompound(itemStack.getTagCompound().copy());
							}

							float factor = 0.05F;
							entityItem.motionX = rand.nextGaussian() * factor;
							entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
							entityItem.motionZ = rand.nextGaussian() * factor;
							if (!world.isRemote) {
								world.spawnEntity(entityItem);
							}
						}
					}
					world.playSound(null, player.posX, player.posY,
						player.posZ, ModSounds.BLOCK_DISMANTLE,
						SoundCategory.BLOCKS, 0.6F, 1F);
					if (!world.isRemote) {
						world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
					}
					return EnumActionResult.SUCCESS;
				}
			}
			return EnumActionResult.PASS;
		} else {
			return EnumActionResult.PASS;
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}
}
