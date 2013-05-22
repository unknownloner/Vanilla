/*
 * This file is part of Vanilla.
 *
 * Copyright (c) 2011-2012, Spout LLC <http://www.spout.org/>
 * Vanilla is licensed under the Spout License Version 1.
 *
 * Vanilla is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Vanilla is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.vanilla.material.block.misc;

import org.spout.api.entity.Entity;
import org.spout.api.entity.Player;
import org.spout.api.event.Cause;
import org.spout.api.event.player.PlayerInteractEvent.Action;
import org.spout.api.geo.World;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.material.BlockMaterial;
import org.spout.api.material.block.BlockFace;
import org.spout.api.material.block.BlockFaces;
import org.spout.api.math.Vector3;

import org.spout.vanilla.ChatStyle;
import org.spout.vanilla.component.entity.living.Hostile;
import org.spout.vanilla.component.entity.living.Living;
import org.spout.vanilla.component.entity.misc.Sleep;
import org.spout.vanilla.component.world.sky.Sky;
import org.spout.vanilla.data.Time;
import org.spout.vanilla.data.resources.VanillaMaterialModels;
import org.spout.vanilla.material.InitializableMaterial;
import org.spout.vanilla.material.VanillaBlockMaterial;
import org.spout.vanilla.material.VanillaMaterials;
import org.spout.vanilla.util.PlayerUtil;
import org.spout.vanilla.util.explosion.ExplosionModel;
import org.spout.vanilla.util.explosion.ExplosionModelSpherical;
import org.spout.vanilla.world.generator.nether.NetherGenerator;

public class BedBlock extends VanillaBlockMaterial implements InitializableMaterial {
	public static final int NEARBY_MONSTER_RANGE = 8, NETHER_EXPLOSION_SIZE = 4;
	public static final String NEARBY_MONSTER_MESSAGE = ChatStyle.RED + "You must not rest, there are monsters nearby!";
	public static final String NOT_NIGHT_MESSAGE = ChatStyle.RED + "You can only sleep at night.";
	public static final String OCCUPIED_MESSAGE = ChatStyle.RED + "This bed is occupied.";

	public BedBlock(String name, int id) {
		super(name, id, VanillaMaterialModels.BED_BLOCK);
		this.setHardness(0.2F).setResistance(0.3F).setTransparent();
	}

	@Override
	public void onInteractBy(Entity entity, Block block, Action type, BlockFace clickedFace) {
		if (!(entity instanceof Player) || type != Action.RIGHT_CLICK) {
			return;
		}

		final Player player = (Player) entity;
		Sleep sleep = player.get(Sleep.class);
		if (sleep == null) {
			return;
		}

		final Block head = getCorrectHalf(block, true);
		final World world = player.getWorld();
		final Sky sky = world.get(Sky.class);

		for (Entity e : world.getNearbyEntities(player, NEARBY_MONSTER_RANGE)) {
			if (e.get(Living.class) instanceof Hostile) {
				player.sendMessage(NEARBY_MONSTER_MESSAGE);
				return;
			}
		}

		if (sky != null && sky.getTime() < Time.DUSK.getTime()) {
			player.sendMessage(NOT_NIGHT_MESSAGE);
			return;
		}

		if (isOccupied(head)) {
			player.sendMessage(OCCUPIED_MESSAGE);
			return;
		}

		sleep.sleep(head);
	}

	@Override
	public void initialize() {
		this.getDrops().clear();
		this.getDrops().NOT_CREATIVE.add(VanillaMaterials.BED);
	}

	@Override
	public boolean onDestroy(Block block, Cause<?> cause) {
		Block head = getCorrectHalf(block, true);
		Block foot = getCorrectHalf(block, false);
		return head.setMaterial(VanillaMaterials.AIR, cause) && foot.setMaterial(VanillaMaterials.AIR, cause);
	}

	/**
	 * Sets whether or not a bed is occupied by a player
	 * @param bedBlock to get it of
	 */
	public void setOccupied(Block bedBlock, Entity sleeper, boolean occupied) {
		bedBlock = getCorrectHalf(bedBlock, false);
		bedBlock.setDataBits(0x4, occupied);
		// set to the same data for the head, but set the head flag
		getCorrectHalf(bedBlock, true).setData(bedBlock.getData() | 0x8);
	}

	/**
	 * Gets whether or not a bed block is occupied by a player
	 * @param bedBlock to get it of
	 * @return True if occupied
	 */
	public boolean isOccupied(Block bedBlock) {
		return bedBlock.isDataBitSet(0x4);
	}

	/**
	 * Gets the facing state of a single bed block
	 * @param bedBlock to get it of
	 * @return the face
	 */
	public BlockFace getFacing(Block bedBlock) {
		return BlockFaces.WNES.get(bedBlock.getData() & 0x3);
	}

	/**
	 * Sets the facing state of a single bed block<br>
	 * Note that this does not affect the misc half
	 * @param bedBlock to set it of
	 * @param facing to set to
	 */
	public void setFacing(Block bedBlock, BlockFace facing) {
		bedBlock.setDataField(0x3, BlockFaces.WNES.indexOf(facing, 0));
	}

	/**
	 * Creates a bed using the parameters specified
	 * @param footBlock of the bed
	 * @param facing of the bed
	 */
	public void create(Block footBlock, BlockFace facing) {
		Block headBlock = footBlock.translate(facing);
		if (headBlock.getWorld().getGenerator() instanceof NetherGenerator) {
			ExplosionModel explosion = new ExplosionModelSpherical();
			explosion.execute(headBlock.getPosition(), NETHER_EXPLOSION_SIZE, true, toCause(footBlock));
		} else {
			footBlock.setMaterial(this, 0x0, toCause(footBlock));
			headBlock.setMaterial(this, 0x8, toCause(footBlock));
			setFacing(footBlock, facing);
			setFacing(headBlock, facing);
		}
	}

	@Override
	public boolean canPlace(Block block, short data, BlockFace against, Vector3 clickedPos, boolean isClickedBlock, Cause<?> cause) {
		if (against == BlockFace.BOTTOM && super.canPlace(block, data, against, clickedPos, isClickedBlock, cause)) {
			Block below = block.translate(BlockFace.BOTTOM);
			BlockMaterial material = below.getMaterial();
			Block headBlock = block.translate(PlayerUtil.getFacing(cause));
			if (!VanillaMaterials.AIR.equals(headBlock)) {
				return false;
			}
			if (material instanceof VanillaBlockMaterial) {
				return ((VanillaBlockMaterial) material).canSupport(this, BlockFace.TOP);
			}
		}
		return false;
	}

	@Override
	public void onPlacement(Block block, short data, BlockFace face, Vector3 clickedPos, boolean isClicked, Cause<?> cause) {
		create(block, PlayerUtil.getFacing(cause));
	}

	/**
	 * Gets the top or face door block when either of the blocks is given
	 * @param bedBlock the top or bottom bed block
	 * @param head whether to get the top block, if false, gets the bottom block
	 * @return the requested bed half block
	 */
	private Block getCorrectHalf(Block bedBlock, boolean head) {
		BlockFace facing = getFacing(bedBlock);
		if (bedBlock.isDataBitSet(0x8)) {
			if (!head) {
				bedBlock = bedBlock.translate(facing.getOpposite());
			}
		} else {
			if (head) {
				bedBlock = bedBlock.translate(facing);
			}
		}
		if (!bedBlock.getMaterial().equals(this)) {
			// create default bed block to 'fix' things up
			bedBlock.setMaterial(this, head ? 0x8 : 0x0);
			// find out what facing makes most sense
			for (BlockFace face : BlockFaces.NESW) {
				if (bedBlock.translate(face).getMaterial().equals(this)) {
					if (head) {
						setFacing(bedBlock, face.getOpposite());
					} else {
						setFacing(bedBlock, face);
					}
					break;
				}
			}
		}
		return bedBlock;
	}
}
