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

import org.spout.vanilla.data.effect.store.SoundEffects;
import org.spout.vanilla.material.block.Solid;
import org.spout.vanilla.material.block.solid.Wool;

public class StainedClay extends Solid {
	public static final StainedClay WHITE_STAINED_CLAY = new StainedClay("White Stained Clay", null);
	public static final StainedClay ORANGE_STAINED_CLAY = new StainedClay("Orange Stained Clay", Wool.WoolColor.ORANGE, WHITE_STAINED_CLAY, null);
	public static final StainedClay MAGENTA_STAINED_CLAY = new StainedClay("Magenta Stained Clay", Wool.WoolColor.MAGENTA, WHITE_STAINED_CLAY, null);
	public static final StainedClay LIGHTBLUE_STAINED_CLAY = new StainedClay("Light Blue Stained Clay", Wool.WoolColor.LIGHTBLUE, WHITE_STAINED_CLAY, null);
	public static final StainedClay YELLOW_STAINED_CLAY = new StainedClay("Yellow Stained Clay", Wool.WoolColor.YELLOW, WHITE_STAINED_CLAY, null);
	public static final StainedClay LIME_STAINED_CLAY = new StainedClay("Lime Stained Clay", Wool.WoolColor.LIME, WHITE_STAINED_CLAY, null);
	public static final StainedClay PINK_STAINED_CLAY = new StainedClay("Pink Stained Clay", Wool.WoolColor.PINK, WHITE_STAINED_CLAY, null);
	public static final StainedClay GRAY_STAINED_CLAY = new StainedClay("Gray Stained Clay", Wool.WoolColor.GRAY, WHITE_STAINED_CLAY, null);
	public static final StainedClay SILVER_STAINED_CLAY = new StainedClay("Light Gray Stained Clay", Wool.WoolColor.SILVER, WHITE_STAINED_CLAY, null);
	public static final StainedClay CYAN_STAINED_CLAY = new StainedClay("Cyan Stained Clay", Wool.WoolColor.CYAN, WHITE_STAINED_CLAY, null);
	public static final StainedClay PURPLE_STAINED_CLAY = new StainedClay("Purple Stained Clay", Wool.WoolColor.PURPLE, WHITE_STAINED_CLAY, null);
	public static final StainedClay BLUE_STAINED_CLAY = new StainedClay("Blue Stained Clay", Wool.WoolColor.BLUE, WHITE_STAINED_CLAY, null);
	public static final StainedClay BROWN_STAINED_CLAY = new StainedClay("Brown Stained Clay", Wool.WoolColor.BROWN, WHITE_STAINED_CLAY, null);
	public static final StainedClay GREEN_STAINED_CLAY = new StainedClay("Green Stained Clay", Wool.WoolColor.GREEN, WHITE_STAINED_CLAY, null);
	public static final StainedClay RED_STAINED_CLAY = new StainedClay("Red Stained Clay", Wool.WoolColor.RED, WHITE_STAINED_CLAY, null);
	public static final StainedClay BLACK_STAINED_CLAY = new StainedClay("Black Stained Clay", Wool.WoolColor.BLACK, WHITE_STAINED_CLAY, null);

	private final Wool.WoolColor color;

	private StainedClay(String name, String model) {
		super((short) 0x000F, name, 171, model);
		this.color = Wool.WoolColor.WHITE;
		this.setHardness(0.8F).setResistance(1.3F).setStepSound(SoundEffects.STEP_CLOTH);
	}

	private StainedClay(String name, Wool.WoolColor color, StainedClay parent, String model) {
		super(name, 171, color.getData(), parent, model);
		this.color = color;
		this.setHardness(0.8F).setResistance(1.3F).setStepSound(SoundEffects.STEP_CLOTH);
	}

	public Wool.WoolColor getColor() {
		return color;
	}
}
