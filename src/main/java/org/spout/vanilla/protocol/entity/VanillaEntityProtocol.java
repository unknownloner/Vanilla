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
package org.spout.vanilla.protocol.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spout.api.entity.Entity;
import org.spout.api.geo.discrete.Transform;
import org.spout.api.math.Vector3;
import org.spout.api.protocol.EntityProtocol;
import org.spout.api.protocol.Message;
import org.spout.api.protocol.reposition.RepositionManager;
import org.spout.api.util.Parameter;

import org.spout.vanilla.component.entity.misc.Head;
import org.spout.vanilla.protocol.ChannelBufferUtils;
import org.spout.vanilla.protocol.msg.entity.EntityDestroyMessage;
import org.spout.vanilla.protocol.msg.entity.EntityMetadataMessage;
import org.spout.vanilla.protocol.msg.entity.pos.EntityHeadYawMessage;
import org.spout.vanilla.protocol.msg.entity.pos.EntityRelativePositionMessage;
import org.spout.vanilla.protocol.msg.entity.pos.EntityRelativePositionYawMessage;
import org.spout.vanilla.protocol.msg.entity.pos.EntityTeleportMessage;
import org.spout.vanilla.protocol.msg.entity.pos.EntityYawMessage;

import static org.spout.vanilla.protocol.ChannelBufferUtils.protocolifyPitch;
import static org.spout.vanilla.protocol.ChannelBufferUtils.protocolifyPosition;
import static org.spout.vanilla.protocol.ChannelBufferUtils.protocolifyYaw;

public abstract class VanillaEntityProtocol implements EntityProtocol {
	private List<Parameter<?>> lastMeta;

	public List<Parameter<?>> getUpdateParameters(Entity entity) {
		return Collections.emptyList();
	}

	@Override
	public final List<Message> getDestroyMessages(Entity entity) {
		return Arrays.<Message>asList(new EntityDestroyMessage(new int[]{entity.getId()}));
	}

	@Override
	public List<Message> getUpdateMessages(Entity entity, Transform liveTransform, RepositionManager rm, boolean force) {
		// Movement
		final Transform prevTransform = rm.convert(entity.getScene().getTransform());
		final Transform newTransform = rm.convert(liveTransform);

		final boolean looked = entity.getScene().isRotationDirty();

		final int lastX = protocolifyPosition(prevTransform.getPosition().getX());
		final int lastY = protocolifyPosition(prevTransform.getPosition().getY());
		final int lastZ = protocolifyPosition(prevTransform.getPosition().getZ());
		final int lastYaw = protocolifyYaw(prevTransform.getRotation().getYaw());
		final int lastPitch = protocolifyPitch(prevTransform.getRotation().getPitch());

		final int newX = protocolifyPosition(newTransform.getPosition().getX());
		final int newY = protocolifyPosition(newTransform.getPosition().getY());
		final int newZ = protocolifyPosition(newTransform.getPosition().getZ());
		final int newYaw = protocolifyYaw(newTransform.getRotation().getYaw());
		final int newPitch = protocolifyPitch(newTransform.getRotation().getPitch());

		final int deltaX = newX - lastX;
		final int deltaY = newY - lastY;
		final int deltaZ = newZ - lastZ;
		final int deltaYaw = newYaw - lastYaw;
		final int deltaPitch = newPitch - lastPitch;

		final List<Message> messages = new ArrayList<Message>();

		/*
		 * Two scenarios:
		 * - The entity moves more than 4 blocks and maybe changes rotation.
		 * - The entity moves less than 4 blocks and maybe changes rotation.
		 */
		if (force || deltaX > 128 || deltaX < -128 || deltaY > 128 || deltaY < -128 || deltaZ > 128 || deltaZ < -128) {
			messages.add(new EntityTeleportMessage(entity.getId(), newX, newY, newZ, newYaw, newPitch));
			if (force || looked) {
				messages.add(new EntityYawMessage(entity.getId(), newYaw, newPitch));
			}
		} else if (deltaX != 0 || deltaY != 0 || deltaZ != 0 || deltaYaw != 0 || deltaPitch != 0) {
			if (looked) {
				messages.add(new EntityRelativePositionYawMessage(entity.getId(), deltaX, deltaY, deltaZ, newYaw, newPitch));
			} else if (!prevTransform.getPosition().equals(newTransform.getPosition())) {
				messages.add(new EntityRelativePositionMessage(entity.getId(), deltaX, deltaY, deltaZ));
			}
		}

		// Head movement
		Head head = entity.get(Head.class);
		if (head != null && head.isDirty()) {
			final int headYawProt = ChannelBufferUtils.protocolifyYaw(head.getRotation().getYaw());
			messages.add(new EntityHeadYawMessage(entity.getId(), headYawProt));
		}

		// Physics
		//TODO: Actually not used?
		/*if (physics != null && physics.isLinearVelocityDirty()) {
			messages.add(new EntityVelocityMessage(entity.getId(), new Vector3(0, 0, 0)));
		}*/

		// Extra metadata
		List<Parameter<?>> params = getUpdateParameters(entity);
		if (lastMeta == null || !lastMeta.equals(params)) {
			messages.add(new EntityMetadataMessage(entity.getId(), params));
			lastMeta = params;
		}

		return messages;
	}

	public static Vector3 getProtocolVelocity(Vector3 velocity) {
		final float x = velocity.getX() * 32000;
		final float y = velocity.getY() * 32000;
		final float z = velocity.getZ() * 32000;
		return new Vector3(x, y, z);
	}
}
