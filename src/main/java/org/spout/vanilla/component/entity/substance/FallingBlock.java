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
package org.spout.vanilla.component.entity.substance;

import org.spout.api.collision.CollisionStrategy;
import org.spout.api.geo.World;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.geo.discrete.Point;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.BlockMaterial;
import org.spout.api.math.GenericMath;
import org.spout.api.math.Vector3;

import org.spout.vanilla.VanillaPlugin;
import org.spout.vanilla.material.VanillaBlockMaterial;
import org.spout.vanilla.protocol.entity.object.FallingBlockProtocol;
import org.spout.vanilla.protocol.entity.object.ObjectType;

public class FallingBlock extends Substance {
	private VanillaBlockMaterial material;
	private float fallSpeed = 0F;

	@Override
	public void onAttached() {
		getOwner().getNetwork().setEntityProtocol(VanillaPlugin.VANILLA_PROTOCOL_ID, new FallingBlockProtocol(ObjectType.FALLING_OBJECT));
	}

	public void setMaterial(VanillaBlockMaterial material) {
		this.material = material;
	}

	public VanillaBlockMaterial getMaterial() {
		return material;
	}

	@Override
	public void onTick(float dt) {
		Point pos = this.getOwner().getScene().getPosition();
		World world = pos.getWorld();
		int x = pos.getBlockX();
		int y = pos.getBlockY();
		int z = pos.getBlockZ();
		int fallAmt = Math.max(1, GenericMath.floor(Math.abs(fallSpeed)));
		for (int dy = 0; dy < fallAmt; dy++) {
			BlockMaterial below = world.getBlockMaterial(x, y - dy, z);
			if (isFallingObstacle(below)) {
				// Place block on top of this obstacle, if possible
				Block current = world.getBlock(x, y - dy + 1, z);
				BlockMaterial currentMat = current.getMaterial();
				if (!(currentMat instanceof VanillaBlockMaterial) || !((VanillaBlockMaterial) currentMat).isPlacementObstacle()) {
					// Place in the world
					current.setMaterial(getMaterial(), getMaterial().toCause(pos));
				} else {
					// Spawn drops
					Item.dropNaturally(pos, new ItemStack(getMaterial(), 1));
				}
				this.getOwner().remove();
			}
		}
		if (!this.getOwner().isRemoved()) {
			float FALL_INCREMENT = -0.04F;
			fallSpeed += (FALL_INCREMENT * dt * 20);
			this.getOwner().getScene().translate(new Vector3(0, fallSpeed, 0F));
			float FALL_MULTIPLIER = 0.98F;
			fallSpeed *= FALL_MULTIPLIER;
		}
	}

	public float getFallingSpeed() {
		return fallSpeed;
	}

	/**
	 * Whether the material can stop a falling block from falling, acting as the new ground block
	 * @param material
	 * @return True if it obstructs further falling, False if not
	 */
	public static boolean isFallingObstacle(BlockMaterial material) {
		if (material == BlockMaterial.AIR) {
			return false;
		}
		if (material.getVolume().getStrategy() != CollisionStrategy.SOLID) {
			return false;
		}
		if (material instanceof VanillaBlockMaterial) {
			VanillaBlockMaterial vbm = (VanillaBlockMaterial) material;
			if (!vbm.isPlacementObstacle()) {
				return false;
			}
		}
		return true;
	}
}
