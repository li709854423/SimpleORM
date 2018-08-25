package com.mengcraft.simpleorm;

import com.google.inject.Inject;
import com.mengcraft.simpleorm.lib.LibraryLoader;
import com.mengcraft.simpleorm.lib.MavenLibrary;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.util.Optional;

@Plugin(
        id = "orm",
        name = "ORM",
        version = "1.0.0",
        authors = "mengcraft.com"
)
public class ORM {

    public static ConfigurationLoader<CommentedConfigurationNode> defaultConfig;

    @Listener
    public void gamePreInitializationEvent(GamePreInitializationEvent event) throws IOException {
        Optional<PluginContainer> pluginContainer = Sponge.getPluginManager().fromInstance(this);
        LibraryLoader.loadLibrary(pluginContainer.get(),"com.avaje.ebean.EbeanServer","org.avaje:ebean:2.8.1");
        @NonNull CommentedConfigurationNode node = defaultConfig.load();
        EbeanManager.url = node.getNode("dataSource","url").getString("jdbc:mysql://localhost/db");
        EbeanManager.user =  node.getNode("dataSource","user").getString( "root");
        EbeanManager.password = node.getNode("dataSource","password").getString( "root");

        Sponge.getServiceManager().setProvider(this,EbeanManager.class,
                EbeanManager.DEFAULT);
        EbeanHandler handler = EbeanManager.DEFAULT.getHandler(pluginContainer.get());

    }

    @Inject
    public void setDefaultConfig( @DefaultConfig(sharedRoot = false) ConfigurationLoader<CommentedConfigurationNode> defaultConfig) {
        this.defaultConfig = defaultConfig;
    }
}
