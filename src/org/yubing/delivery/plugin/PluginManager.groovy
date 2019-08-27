package org.yubing.delivery.plugin;

import java.io.Serializable;

import org.yubing.delivery.Project;
import org.yubing.delivery.plugin.flow.FlowPlugin;

/**
 *	插件管理
 */
class PluginManager implements Serializable {

	def plugins = [:];
	def project;
	def script;

	PluginManager(Project project) {
		this.project = project;
		this.script = project.script;
	}

	def init() {
		register("flow", new FlowPlugin());
	}

	def register(pluginId, plugin) {
		plugins[pluginId] = plugin;
	}

	def findPlugin(pluginId) {
		// inner plugin
		def plugin = plugins[pluginId];
		if (plugin != null) {
			return plugin;
		}

		// load outside plugin
        def pluginFactory = this.script."${pluginId}_plugin"
        if (pluginFactory != null) {
            return pluginFactory.instance();
        }

        return null;
	}
}