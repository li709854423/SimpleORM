package com.mengcraft.simpleorm;

import com.mengcraft.simpleorm.lib.RefHelper;
import org.spongepowered.api.plugin.PluginContainer;

public final class Reflect {


    public static ClassLoader getLoader(PluginContainer plugin) throws Exception {
        return RefHelper.getField(plugin, "classLoader");
    }

}
