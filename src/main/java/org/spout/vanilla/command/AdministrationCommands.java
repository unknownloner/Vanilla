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
package org.spout.vanilla.command;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.linked.TLongLinkedList;

import org.spout.api.Client;
import org.spout.api.Engine;
import org.spout.api.Server;
import org.spout.api.command.CommandArguments;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.CommandFilter;
import org.spout.api.command.annotated.CommandPermissions;
import org.spout.api.command.filter.PlayerFilter;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.api.generator.biome.Biome;
import org.spout.api.generator.biome.BiomeGenerator;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;
import org.spout.api.inventory.Inventory;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.Material;
import org.spout.api.scheduler.TaskPriority;
import org.spout.api.util.concurrent.AtomicFloat;

import org.spout.vanilla.ChatStyle;
import org.spout.vanilla.VanillaPlugin;
import org.spout.vanilla.component.entity.inventory.PlayerInventory;
import org.spout.vanilla.component.entity.living.Human;
import org.spout.vanilla.component.entity.misc.Health;
import org.spout.vanilla.component.entity.misc.Level;
import org.spout.vanilla.component.world.sky.Sky;
import org.spout.vanilla.data.GameMode;
import org.spout.vanilla.data.Time;
import org.spout.vanilla.data.Weather;
import org.spout.vanilla.data.configuration.OpConfiguration;
import org.spout.vanilla.data.configuration.VanillaConfiguration;
import org.spout.vanilla.event.cause.HealthChangeCause;
import org.spout.vanilla.material.VanillaMaterials;

public class AdministrationCommands {
	private final VanillaPlugin plugin;
	private final TicksPerSecondMonitor tpsMonitor;

	public AdministrationCommands(VanillaPlugin plugin) {
		this.plugin = plugin;
		tpsMonitor = new TicksPerSecondMonitor();
		plugin.setTPSMonitor(tpsMonitor);
		plugin.getEngine().getScheduler().scheduleSyncRepeatingTask(plugin, tpsMonitor, 0, 50, TaskPriority.CRITICAL);
	}

	private Engine getEngine() {
		return plugin.getEngine();
	}

	@Command(aliases = "clear", usage = "[player] [item] [data]", desc = "Clears the target's inventory", min = 0, max = 3)
	@CommandPermissions("vanilla.command.clear")
	public void clear(CommandSource source, CommandArguments args) throws CommandException {
		Player player;
		Material filter = null;
		Integer data = null;
		
		if (args.length() == 0) {
			if (!(source instanceof Player)) {
				throw new CommandException("You must be a player to clear your own inventory.");
			}
			player = (Player) source;
		} else if (args.length() == 3) { 
			if (getEngine() instanceof Client) {
				throw new CommandException("You cannot search for players unless you are in server mode.");
			}
			
			player = getEngine().getPlayer(args.getString(0), false);
			if (player == null) {
				throw new CommandException(args.getString(0) + " is not online.");
			}
			
			if (args.isInteger(1)) {
				filter = VanillaMaterials.getMaterial((short) args.getInteger(1));
			} else {
				filter = Material.get(args.getString(1));
			}
			
			data = args.getInteger(2);
		} else {
			if (args.isInteger(0)) {
				if (!(source instanceof Player)) {
					throw new CommandException("You must be a player to clear your own inventory.");
				}
				player = (Player) source;
				
				filter = VanillaMaterials.getMaterial((short) args.getInteger(0));
			} else {
				if (getEngine() instanceof Client) {
					throw new CommandException("You cannot search for players unless you are in server mode.");
				}
				
				player = getEngine().getPlayer(args.getString(0), false);
				if (player == null) {
					filter = Material.get(args.getString(0));
					if (filter == null) {
						throw new CommandException(args.getString(0) + " is not online.");
					}
					if (!(source instanceof Player)) {
						throw new CommandException("You must be a player to clear your own inventory.");
					}
					player = (Player) source;
				}
			}
			if (args.length() == 2) {
				if (args.isInteger(1)) {
					if (filter == null) {
						filter = VanillaMaterials.getMaterial((short) args.getInteger(1));
					} else {
						data = args.getInteger(1);
					}
				} else {
					if (filter == null) {
						filter = Material.get(args.getString(1));
					} else {
						throw new NumberFormatException();
					}
				}
			}
		}
		
		PlayerInventory inv = player.get(PlayerInventory.class);
		if (inv == null) {
			throw new CommandException(player.getName() + " doesn't have a inventory!");
		} else {
			// Count the items and clear the inventory
			Inventory[] inventories = new Inventory[] { inv.getMain(), inv.getQuickbar(), inv.getArmor() };
			int cleared = 0;
			for (int k = 0; k < inventories.length; k++) {
				for (int i = 0; i < inventories[k].size(); i++) {
					if (inventories[k].get(i) != null && (filter == null || inventories[k].get(i).isMaterial(filter)) && (data == null || inventories[k].get(i).getData() == data)) {
						cleared += inventories[k].get(i).getAmount();
						inventories[k].set(i, null);
					}
				}
			}
			
			if (cleared == 0) {
				throw new CommandException("Inventory is already empty");
			}
			
			source.sendMessage(plugin.getPrefix() + ChatStyle.GREEN + "Cleared the inventory of " + player.getName() + ", removing " + cleared + " items.");
		}	
	}

	@Command(aliases = {"give"}, usage = "[player] <block> [amount] [data]", desc = "Lets a player spawn items", min = 1, max = 4)
	@CommandPermissions("vanilla.command.give")
	public void give(CommandSource source, CommandArguments args) throws CommandException {
		int index = 0;
		Player player = null;
		
		if (args.length() != 1) {
			if (getEngine() instanceof Client) {
				throw new CommandException("You cannot search for players unless you are in server mode.");
			}
			player = getEngine().getPlayer(args.getString(index++), true);
		}
		
		if (player == null) {
			switch (args.length()) {
				case 4:
					throw new CommandException(args.getString(0) + " is not online.");
				case 3:
				case 2:
					index--;
				case 1:
					if (!(source instanceof Player)) {
						throw new CommandException("You must be a player to give yourself materials!");
					}

					player = (Player) source;
					break;
			}
		}
		
		Material material;
		if (args.isInteger(index)) {
			material = VanillaMaterials.getMaterial((short) args.getInteger(index++));
		} else {
			material = Material.get(args.getString(index++));
		}
		
		if (material == null) {
			throw new CommandException(args.getString(index) + " is not a block!");
		}
		
		int quantity = 1;
		int data = 0;
		
		if (args.length() > 2) {
			quantity = args.getInteger(index++);
		}
		
		if (args.length() > 3) {
			data = args.getInteger(index++);
		}
		
		PlayerInventory inventory = player.get(PlayerInventory.class);
		if (inventory != null) {
			inventory.add(new ItemStack(material, data, quantity));
			source.sendMessage(plugin.getPrefix() + ChatStyle.GREEN + "Gave "
					+ ChatStyle.WHITE + player.getName() + " " + quantity + ChatStyle.GREEN + " of " + ChatStyle.WHITE
					+ material.getDisplayName());
		} else {
			throw new CommandException(player.getName() + " doesn't have a inventory!");
		}
	}

	@Command(aliases = {"deop"}, usage = "<player>", desc = "Revoke a players operator status", min = 1, max = 1)
	@CommandPermissions("vanilla.command.deop")
	public void deop(CommandSource source, CommandArguments args) throws CommandException {
		String playerName = args.getString(0);
		OpConfiguration ops = VanillaConfiguration.OPS;
		if (!ops.isOp(playerName)) {
			source.sendMessage(plugin.getPrefix() + playerName + ChatStyle.RED + " is not an operator!");
			return;
		}

		ops.setOp(playerName, false);
		source.sendMessage(plugin.getPrefix() + playerName + ChatStyle.RED + " had their operator status revoked!");
		Player player = getEngine().getPlayer(playerName, true);
		if (player != null && !source.equals(player)) {
			player.sendMessage(plugin.getPrefix() + ChatStyle.RED + "You had your operator status revoked!");
		}
	}

	@Command(aliases = {"op"}, usage = "<player>", desc = "Make a player an operator", min = 1, max = 1)
	@CommandPermissions("vanilla.command.op")
	public void op(CommandSource source, CommandArguments args) throws CommandException {
		String playerName = args.getString(0);
		OpConfiguration ops = VanillaConfiguration.OPS;
		if (ops.isOp(playerName)) {
			source.sendMessage(plugin.getPrefix() + ChatStyle.RED + playerName + " is already an operator!");
			return;
		}

		ops.setOp(playerName, true);
		source.sendMessage(plugin.getPrefix() + ChatStyle.RED + playerName + " is now an operator!");
		Player player = getEngine().getPlayer(playerName, true);
		if (player != null && !source.equals(player)) {
			player.sendMessage(plugin.getPrefix() + ChatStyle.YELLOW + "You are now an operator!");
		}
	}

	@Command(aliases = {"time"}, usage = "<add|set> <0-24000|day|night|dawn|dusk> [world]", desc = "Set the time of the server", min = 2, max = 3)
	@CommandPermissions("vanilla.command.time")
	public void time(CommandSource source, CommandArguments args) throws CommandException {
		long time;
		boolean relative = args.getString(0).equalsIgnoreCase("add");

		if (args.isInteger(1)) {
			time = args.getInteger(1);
		} else {
			if (relative) {
				throw new CommandException("Argument to 'add' must be an integer.");
			}
			try {
				time = Time.get(args.getString(1)).getTime();
			} catch (Exception e) {
				throw new CommandException("'" + args.getString(1) + "' is not a valid time.");
			}
		}

		World world;
		if (args.length() == 3) {
			world = plugin.getEngine().getWorld(args.getString(2));
			if (world == null) {
				throw new CommandException("'" + args.getString(2) + "' is not a valid world.");
			}
		} else if (source instanceof Player) {
			Player player = (Player) source;
			world = player.getWorld();
		} else {
			throw new CommandException("You must specify a world.");
		}

		Sky sky = world.get(Sky.class);
		if (sky == null) {
			throw new CommandException("The sky for " + world.getName() + " is not available.");
		}

		sky.setTime(relative ? (sky.getTime() + time) : time);
		if (getEngine() instanceof Client) {
			source.sendMessage(plugin.getPrefix() + ChatStyle.GREEN + "You set "
					+ ChatStyle.WHITE + world.getName() + ChatStyle.GREEN
					+ " to time: " + ChatStyle.WHITE + sky.getTime());
		} else {
			((Server) getEngine()).broadcastMessage(plugin.getPrefix() + ChatStyle.WHITE
					+ world.getName() + ChatStyle.GREEN + " set to: " + ChatStyle.WHITE + sky.getTime());
		}
	}

	@Command(aliases = {"gamemode", "gm"}, usage = "<0|1|2|survival|creative|adventure|s|c|a> [player] (0 = SURVIVAL, 1 = CREATIVE, 2 = ADVENTURE)", desc = "Change a player's game mode", min = 1, max = 2)
	@CommandPermissions("vanilla.command.gamemode")
	public void gamemode(CommandSource source, CommandArguments args) throws CommandException {
		Player player;
		if (args.length() == 2) {
			if (getEngine() instanceof Client) {
				throw new CommandException("You cannot search for players unless you are in server mode.");
			}
			player = getEngine().getPlayer(args.getString(1), true);
			if (player == null) {
				throw new CommandException(args.getString(1) + " is not online.");
			}
		} else {
			if (!(source instanceof Player)) {
				throw new CommandException("You must be a player to toggle your game mode.");
			}

			player = (Player) source;
		}

		Human human = player.get(Human.class);
		if (human == null) {
			throw new CommandException("Invalid player!");
		}

		GameMode mode;

		try {
			if (args.isInteger(0)) {
				mode = GameMode.get(args.getInteger(0));
			} else if (args.getString(0).length() == 1) {
				String str = args.getString(0);
				if (str.equalsIgnoreCase("s")) {
					mode = GameMode.SURVIVAL;
				} else if (str.equalsIgnoreCase("c")) {
					mode = GameMode.CREATIVE;
				} else if (str.equalsIgnoreCase("a")) {
					mode = GameMode.ADVENTURE;
				} else {
					throw new Exception();
				}
			} else {
				mode = GameMode.get(args.getString(0));
			}
		} catch (Exception e) {
			throw new CommandException("A game mode must be either a number between 0 and 2, 'CREATIVE', 'SURVIVAL' or 'ADVENTURE'");
		}

		human.setGamemode(mode);

		if (!player.equals(source)) {
			source.sendMessage(plugin.getPrefix() + ChatStyle.WHITE + player.getName()
					+ "'s " + ChatStyle.GREEN + "gamemode has been changed to "
					+ ChatStyle.WHITE + mode.name() + ChatStyle.GREEN + ".");
		}
	}

	@Command(aliases = "xp", usage = "[player] <amount>", desc = "Give/take experience from a player", min = 1, max = 2)
	@CommandPermissions("vanilla.command.xp")
	public void xp(CommandSource source, CommandArguments args) throws CommandException {
		int index = 0;
		Player player;
		if (args.length() == 1) {
			if (source instanceof Player) {
				player = (Player) source;
			} else {
				throw new CommandException("You must be a player to give yourself xp.");
			}
		} else {
			if (getEngine() instanceof Client) {
				throw new CommandException("You cannot search for players unless you are in server mode.");
			}
			player = getEngine().getPlayer(args.getString(index++), true);
		}
		if (player == null) {
			throw new CommandException("That player is not online.");
		}
		if (!args.isInteger(index)) {
			throw new CommandException("Argument 'amount' must be an integer.");
		}

		short amount = (short) args.getInteger(index);
		Level level = player.get(Level.class);
		if (level == null) {
			throw new CommandException(player.getDisplayName() + " does not have experience.");
		}

		level.addExperience(amount);
		player.sendMessage(plugin.getPrefix() + ChatStyle.GREEN + "Your experience has been set to " + ChatStyle.WHITE + amount + ChatStyle.GREEN + ".");
	}

	@Command(aliases = "weather", usage = "<0|1|2|clear|rain|thunder> (0 = CLEAR, 1 = RAIN/SNOW, 2 = THUNDERSTORM) [world]", desc = "Changes the weather", min = 1, max = 2)
	@CommandPermissions("vanilla.command.weather")
	public void weather(CommandSource source, CommandArguments args) throws CommandException {
		World world;
		if (source instanceof Player && args.length() == 1) {
			world = ((Player) source).getWorld();
		} else if (args.length() == 2) {
			world = plugin.getEngine().getWorld(args.getString(1));

			if (world == null) {
				throw new CommandException("Invalid world '" + args.getString(1) + "'.");
			}
		} else {
			throw new CommandException("You need to specify a world.");
		}

		Weather weather;
		try {
			if (args.isInteger(0)) {
				weather = Weather.get(args.getInteger(0));
			} else {
				weather = Weather.get(args.getString(0).replace("snow", "rain").replace("thunder", "thunderstorm"));
			}
		} catch (Exception e) {
			throw new CommandException("Weather must be a mode between 0 and 2, 'CLEAR', 'RAIN', 'SNOW', or 'THUNDERSTORM'");
		}

		Sky sky = world.get(Sky.class);
		if (sky == null) {
			throw new CommandException("The sky of world '" + world.getName() + "' is not available.");
		}

		sky.setWeather(weather);
		String message;
		switch (weather) {
			case RAIN:
				message = plugin.getPrefix() + ChatStyle.GREEN + "Weather set to " + ChatStyle.WHITE + "RAIN/SNOW" + ChatStyle.GREEN + ".";
				break;
			default:
				message = plugin.getPrefix() + ChatStyle.GREEN + "Weather set to " + ChatStyle.WHITE + weather.name() + ChatStyle.GREEN + ".";
				break;
		}
		if (getEngine() instanceof Client) {
			source.sendMessage(message);
		} else {
			for (Player player : ((Server) getEngine()).getOnlinePlayers()) {
				if (player.getWorld().equals(world)) {
					player.sendMessage(message);
				}
			}
		}
	}

	@Command(aliases = {"kill"}, usage = "[player]", desc = "Kill yourself or another player", min = 0, max = 1)
	@CommandPermissions("vanilla.command.kill")
	public void kill(CommandSource source, CommandArguments args) throws CommandException {
		Player player;
		if (args.length() == 0) {
			if (!(source instanceof Player)) {
				throw new CommandException("Don't be silly...you cannot kill yourself as the console.");
			}
			player = (Player) source;
		} else {
			if (getEngine() instanceof Client) {
				throw new CommandException("You cannot search for players unless you are in server mode.");
			}
			player = getEngine().getPlayer(args.getString(0), true);
		}
		if (player == null) {
			throw new CommandException(args.getString(0) + " is not online.");
		}
		Health health = player.get(Health.class);
		if (health == null) {
			throw new CommandException(player.getDisplayName() + " can not be killed.");
		}
		health.kill(HealthChangeCause.COMMAND);
	}

	@Command(aliases = {"version", "vr"}, usage = "", desc = "Print out the version information for Vanilla", min = 0, max = 0)
	@CommandPermissions("vanilla.command.version")
	public void getVersion(CommandSource source, CommandArguments args) {
		source.sendMessage("Vanilla " + plugin.getDescription().getVersion()
				+ " (Implementing Minecraft protocol v" + plugin.getDescription().getData("protocol") + ")");

		source.sendMessage("Powered by Spout " + getEngine().getVersion() + " (Implementing SpoutAPI " + getEngine().getAPIVersion() + ")");
	}

	@Command(aliases = {"biome"}, usage = "", desc = "Print out the name of the biome at the current location", min = 0, max = 0)
	@CommandPermissions("vanilla.command.biome")
	@CommandFilter(PlayerFilter.class)
	public void getBiomeName(CommandSource source, CommandArguments args) throws CommandException {
		Player player = (Player) source;
		if (!(player.getScene().getPosition().getWorld().getGenerator() instanceof BiomeGenerator)) {
			throw new CommandException("This map does not appear to have any biome data.");
		}
		Point pos = player.getScene().getPosition();
		Biome biome = pos.getWorld().getBiome(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
		source.sendMessage(plugin.getPrefix() + ChatStyle.GREEN + "Current biome: " + ChatStyle.WHITE + (biome != null ? biome.getName() : "none"));
	}

	@Command(aliases = {"tps"}, usage = "", desc = "Print out the current engine ticks per second", min = 0, max = 0)
	@CommandPermissions("vanilla.command.tps")
	public void getTPS(CommandSource source, CommandArguments args) {
		source.sendMessage("TPS: " + tpsMonitor.getTPS());
		source.sendMessage("Average TPS: " + tpsMonitor.getAvgTPS());
	}

	private static class TicksPerSecondMonitor implements Runnable, TPSMonitor {
		private static final int MAX_MEASUREMENTS = 20 * 60;
		private final TLongLinkedList timings = new TLongLinkedList();
		private long lastTime = System.currentTimeMillis();
		private final AtomicFloat ticksPerSecond = new AtomicFloat(20);
		private final AtomicFloat avgTicksPerSecond = new AtomicFloat(20);

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			timings.add(time - lastTime);
			lastTime = time;
			if (timings.size() > MAX_MEASUREMENTS) {
				timings.removeAt(0);
			}
			final int size = timings.size();
			if (size > 20) {
				TLongIterator i = timings.iterator();
				int count = 0;
				long last20 = 0;
				long total = 0;
				while (i.hasNext()) {
					long next = i.next();
					if (count > size - 20) {
						last20 += next;
					}
					total += next;
					count++;
				}
				ticksPerSecond.set(1000F / (last20 / 20F));
				avgTicksPerSecond.set(1000F / (total / ((float) size)));
			}
		}

		public float getTPS() {
			return ticksPerSecond.get();
		}

		public float getAvgTPS() {
			return avgTicksPerSecond.get();
		}
	}

	public static interface TPSMonitor {
		public float getTPS();

		public float getAvgTPS();
	}
}
