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
package org.spout.vanilla.protocol.entity.creature;

import java.util.List;

import org.spout.api.entity.Entity;
import org.spout.api.util.Parameter;

import org.spout.vanilla.component.entity.living.hostile.Creeper;

public class CreeperEntityProtocol extends CreatureProtocol {
	public final static int STATE_INDEX = 16; // The MC metadata index to determine the fuse.
	public final static int CHARGE_INDEX = 17; // The MC metadata index to determine if the creeper is charged.

	public CreeperEntityProtocol() {
		super(CreatureType.CREEPER);
	}

	@Override
	public List<Parameter<?>> getSpawnParameters(Entity entity) {
		List<Parameter<?>> parameters = super.getSpawnParameters(entity);
		parameters.add(new Parameter<Byte>(Parameter.TYPE_BYTE, STATE_INDEX, entity.add(Creeper.class).getState()));
		parameters.add(new Parameter<Byte>(Parameter.TYPE_BYTE, CHARGE_INDEX, (byte) (entity.add(Creeper.class).isCharged() ? 1 : 0)));
		return parameters;
	}
}
