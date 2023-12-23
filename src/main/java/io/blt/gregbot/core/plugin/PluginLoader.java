/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.plugin;

import io.blt.gregbot.core.properties.Properties;
import io.blt.gregbot.plugin.Plugin;
import io.blt.gregbot.plugin.PluginContext;
import io.blt.gregbot.plugin.PluginException;
import io.blt.util.Obj;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import static io.blt.util.stream.SingletonCollectors.toOptional;

/**
 * A facility to load implementations of {@code Plugin}.
 */
public class PluginLoader<T extends Plugin> {

    private final List<T> plugins;

    public PluginLoader(Class<T> type) {
        this.plugins = ServiceLoader.load(type).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }

    /**
     * Loads an implementation of {@code Plugin} matching the provided plugin properties.
     *
     * @param plugin the plugin properties
     * @return loaded plugin instance
     * @throws PluginException        if there is an error loading the plugin
     * @throws NoSuchElementException if no plugin matches the properties
     */
    public T load(Properties.Plugin plugin) throws PluginException {
        return load(new PluginContext(), plugin);
    }

    /**
     * Loads an implementation of {@code Plugin} matching the provided plugin properties.
     *
     * @param context the plugin context
     * @param plugin  the plugin properties
     * @return loaded plugin instance
     * @throws PluginException        if there is an error loading the plugin
     * @throws NoSuchElementException if no plugin matches the properties
     */
    public T load(PluginContext context, Properties.Plugin plugin) throws PluginException {
        return Obj.poke(findPluginOrThrow(plugin), p -> p.load(context, plugin.properties()));
    }

    /**
     * Returns a list of all plugin types loaded by {@code PluginLoader}.
     *
     * @return list of plugin types
     */
    public List<String> plugins() {
        return plugins.stream()
                .map(this::pluginType)
                .toList();
    }

    private T findPluginOrThrow(Properties.Plugin plugin) {
        return plugins.stream()
                .filter(p -> pluginType(p).equals(plugin.type()))
                .collect(toOptional())
                .orElseThrow();
    }

    private String pluginType(T plugin) {
        return plugin.getClass().getName();
    }

}
