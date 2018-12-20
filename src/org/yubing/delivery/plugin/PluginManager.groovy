package org.yubing.delivery.plugin;

import java.io.Serializable;

import org.yubing.delivery.plugin.flow.FlowPlugin;

/**
 *	插件管理
 */
class PluginManager implements Serializable {

	def plugins = [:];
	
	def init() {
		register("flow", new FlowPlugin());
	}

	def register(pluginId, plugin) {
		plugins[pluginId] = plugin;
	}

	def findPlugin(pluginId) {
		return plugins[pluginId];
	}
}