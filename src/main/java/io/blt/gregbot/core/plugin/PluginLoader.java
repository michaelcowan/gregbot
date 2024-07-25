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

    private final ServiceLoader<T> loader;

    public PluginLoader(Class<T> type) {
        this.loader = ServiceLoader.load(type);
    }

    /**
     * Creates and loads an implementation of {@code Plugin} matching the provided plugin properties.
     *
     * @param plugin the plugin properties
     * @return a new loaded plugin instance
     * @throws PluginException        if there is an error loading the plugin
     * @throws NoSuchElementException if no plugin matches the properties
     * @throws NullPointerException   if {@code plugin} is {@code null}
     */
    public T load(Properties.Plugin plugin) throws PluginException {
        return Obj.poke(findPluginOrThrow(plugin), p -> p.load(plugin.properties()));
    }

    /**
     * Returns a list of all plugin types supported by this instance.
     *
     * @return list of plugin types
     */
    public List<String> plugins() {
        return loader.stream()
                .map(this::providerType)
                .toList();
    }

    private T findPluginOrThrow(Properties.Plugin plugin) {
        return loader.stream()
                .filter(p -> providerType(p).equals(plugin.type()))
                .map(ServiceLoader.Provider::get)
                .collect(toOptional())
                .orElseThrow(() -> new NoSuchElementException(
                        "Cannot find plugin '%s'".formatted(plugin.type())));
    }

    private String providerType(ServiceLoader.Provider<T> provider) {
        return provider.type().getName();
    }

}
