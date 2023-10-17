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
import io.blt.util.Obj;
import java.util.List;
import java.util.ServiceLoader;

import static io.blt.util.stream.SingletonCollectors.toOptional;

public class PluginLoader<T extends Plugin> {

    private final List<T> plugins;

    public PluginLoader(Class<T> type) {
        this.plugins = ServiceLoader.load(type).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }

    public T load(Properties.Plugin plugin) throws Exception {
        return Obj.poke(findPluginOrThrow(plugin), p -> p.load(plugin.properties()));
    }

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
