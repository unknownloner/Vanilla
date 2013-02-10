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
package org.spout.vanilla.plugin.resources.loader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.spout.api.Spout;
import org.spout.api.exception.ConfigurationException;
import org.spout.api.inventory.recipe.Recipe;
import org.spout.api.inventory.recipe.RecipeBuilder;
import org.spout.api.material.Material;
import org.spout.api.material.MaterialRegistry;
import org.spout.api.resource.BasicResourceLoader;
import org.spout.api.util.config.ConfigurationNode;
import org.spout.api.util.config.yaml.YamlConfiguration;

import org.spout.vanilla.plugin.resources.RecipeYaml;

public class RecipeLoader extends BasicResourceLoader<RecipeYaml> {
	@Override
	public String getFallbackResourceName() {
		return "recipe://Vanilla/recipes.yml";
	}

	@Override
	public RecipeYaml getResource(InputStream stream) {
		// TODO: Re-implement
		return null;
	}

	@Override
	public String getProtocol() {
		return "recipe";
	}

	@Override
	public String[] getExtensions() {
		return new String[]{"yml"};
	}
}
