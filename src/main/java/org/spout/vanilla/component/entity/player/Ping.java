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
package org.spout.vanilla.component.entity.player;

import org.spout.api.component.type.EntityComponent;
import org.spout.api.entity.Player;
import org.spout.api.math.GenericMath;

import org.spout.vanilla.ChatStyle;
import org.spout.vanilla.event.player.network.PlayerPingEvent;

/**
 * Component that pings periodictly the client to see if it's still connected.
 */
public class Ping extends EntityComponent {
	private Player player;
	private final float timeout = 30;
	private final int repeatRate = 8;
	private final long[] pingTime = new long[repeatRate];
	private final int[] pingHash = new int[repeatRate];
	private float pingTimer = timeout;
	private float kickTimer = 0;
	private float longKickTimer = 0;
	private float ping = 0;
	private int lastRequestCount = 0;

	@Override
	public void onAttached() {
		if (!(getOwner() instanceof Player)) {
			throw new IllegalStateException("Ping may only be attached to a player.");
		}
		player = (Player) getOwner();
	}

	@Override
	public void onTick(float dt) {
		pingTimer += dt;
		kickTimer += dt;
		longKickTimer += dt;
		float period = timeout / repeatRate;
		if (pingTimer > period) {
			pingTimer -= period;
			request();
		}
		if (kickTimer > timeout) {
			player.kick(ChatStyle.RED + "Connection timed out!");
		}
		float longTimeout = 120;
		if (longKickTimer > longTimeout) {
			player.kick(ChatStyle.RED + "Connection timed out due to no ping response!");
		}
	}

	/**
	 * Gets the Player Ping in seconds
	 * @return player ping in seconds
	 */
	public float getPing() {
		return ping;
	}

	/**
	 * Re-sets the long timeout timer by validating the received hash code
	 * @param hash of the message sent to validate against
	 */
	public void response(int hash) {
		refresh();
		for (int i = 0; i < pingTime.length; i++) {
			if (hash == pingHash[i]) {
				ping = (System.currentTimeMillis() - pingTime[i]) / 1000.0F;
				longKickTimer = 0;
				return;
			}
		}
	}

	/**
	 * Re-sets the short timeout timer when any data is received from the client
	 */
	public void refresh() {
		kickTimer = 0;
	}

	/**
	 * Sends a new request to the client to return a ping message.
	 */
	public void request() {
		int hash = GenericMath.getRandom().nextInt(Integer.MAX_VALUE);
		pingTime[lastRequestCount] = System.currentTimeMillis();
		pingHash[lastRequestCount] = hash;
		lastRequestCount++;
		if (lastRequestCount >= pingTime.length) {
			lastRequestCount = 0;
		}
		player.getNetworkSynchronizer().callProtocolEvent(new PlayerPingEvent(hash));
	}
}
