package com.mengcraft.simpleorm;

import lombok.val;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.HashMap;
import java.util.Map;

public class EbeanManager {

    public static final EbeanManager DEFAULT = new EbeanManager();

    static String url = "jdbc:mysql://localhost/mc";
    static String user = "root";
    static String password = "root";

    final Map<String, EbeanHandler> map = new HashMap<>();

    private EbeanManager() {
    }

    public EbeanHandler getHandler(PluginContainer plugin) {
        EbeanHandler out = map.get(plugin.getName());
        if (out == null) {
            map.put(plugin.getName(), out = build(plugin));
        }
        return out;
    }

    /**
     * @param name the name
     * @return the named handler, or {@code null} if absent
     */
    public EbeanHandler getHandler(String name) {
        return map.get(name);
    }

    private EbeanHandler build(PluginContainer plugin) {
        val out = new EbeanHandler(plugin, true);
//
//        String url = plugin.getConfig().getString("dataSource.url");
//
//        String user = plugin.getConfig().getString("dataSource.user");
//        if (user == null) {
//            user = plugin.getConfig().getString("dataSource.userName");
//        }
//
//        String password = plugin.getConfig().getString("dataSource.password");
//
//        String driver = plugin.getConfig().getString("dataSource.driver");
//
//        boolean b = false;
//
//        if (url == null) {
//            plugin.getConfig().set("dataSource.url", url = EbeanManager.url);
//            b = true;
//        }
//        out.setUrl(url);
//
//        if (user == null) {
//            plugin.getConfig().set("dataSource.user", user = EbeanManager.user);
//            b = true;
//        }
//        out.setUser(user);
//
//        if (password == null) {
//            plugin.getConfig().set("dataSource.password", password = EbeanManager.password);
//            b = true;
//        }
//        out.setPassword(password);
//
//        if (b) {
//            plugin.saveConfig();
//        }
//
//        if (!(driver == null)) {
//            out.setDriver(driver);
//        }

        return out;
    }

    static void unHandle(EbeanHandler db) {
        DEFAULT.map.remove(db.getPlugin().getName(), db);
    }

    @Deprecated
    public static void shutdown(PluginContainer plugin) throws DatabaseException {
        val db = DEFAULT.map.get(plugin.getName());
        if (!(db == null)) {
            db.shutdown();
        }
    }

}
