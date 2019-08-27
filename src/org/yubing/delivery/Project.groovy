package org.yubing.delivery;

import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;
import java.lang.reflect.Constructor;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.yubing.delivery.plugin.Plugin
import org.yubing.delivery.plugin.PluginManager;
import org.yubing.delivery.handler.FilterChain;
import org.yubing.delivery.handler.ScriptHandler;
import org.yubing.delivery.handler.PrepareEnvHandler;
import org.yubing.delivery.handler.ConfigPostHandler;
import org.yubing.delivery.handler.StagesHandler;

/**
 *	Project
 */
class Project extends FilterChain {
    def env = [:];
    def config = [:];
    def extensions = [:];
    def pipelines = [:];
    def stages = [:];

	private CpsScript script;
	private PluginManager pluginManager;
    private ScriptHandler scriptHandler;
    private PrepareEnvHandler prepareEnvHandler;
    private ConfigPostHandler configPostHandler;
    def StagesHandler stage;

	Project(CpsScript script) {
		this.script = script;
        this.env = script.env;
        
		this.pluginManager = new PluginManager(this);

        this.scriptHandler = new ScriptHandler(this);
        this.prepareEnvHandler = new PrepareEnvHandler(this);
        this.configPostHandler = new ConfigPostHandler(this);

        this.stage = new StagesHandler(this);

        this.script.echo "project new"
    }

    def log(String msg) {
        this.script.echo msg;
    }

    def init() {
        this.log "project init"

        // prepare
        this.prepareEnvHandler.handle();

        // init plugin
        this.pluginManager.init();
    }

    def buildScript(Closure body) {
        this.log "project buildScript"

        body.resolveStrategy = Closure.DELEGATE_FIRST;
        body.delegate = this.scriptHandler;
        body();
    }

    def config(Closure body) {
        this.log "project config"

        this.config.env = this.script.env;
        body.resolveStrategy = Closure.DELEGATE_FIRST;
        body.delegate = this.config;
        body();

        // post handle config
        this.configPostHandler.handle();

        // print config
        this.log "project config:" + this.config;
    }

    def propertyMissing(String name) {
        if (this.config[name] != null) {
            return this.config[name];
        }

        if (this.extensions[name] != null) {
            return this.extensions[name];
        }

        if (this.pipelines[name] != null) {
            return this.pipelines[name];
        }

        if (this.stages[name] != null) {
            return this.stages[name];
        }

        throw new DeliveryException("not_found_property", "not found property in project with name:" + name);
    }

    def methodMissing(String name, Object args) {
        Object[] arr = (Object[]) args;

        log("methodMissing: name:" + name + ", args:" + args);

        if (arr.length >= 1 && (arr[0] instanceof Closure)) {
            def obj = this.extensions[name];
            if (obj == null) {
                throw new DeliveryException("not_found_extension", "not found extensions property in project with name:" + name);
            }

            obj.env = this.script.env;
            def body = (Closure) arr[0];

            if (obj != null && body != null) {
                body.resolveStrategy = Closure.DELEGATE_FIRST;
                body.delegate = obj;
                return body();
            }
        } 

        throw new DeliveryException("not_found_method", "not found method in project with name:" + name + ", and args:" + args);
    }

    def pipeline(String name) {
        def p = this.pipelines[name];
        
        if (p == null) {
            p = new Pipeline(this, name);
            this.pipelines[name] = p;
        }
        
        return p;
    }

    def pipeline(String name, Pipeline pipeline) {
        this.pipelines[name] = pipeline;
        return pipeline;
    }

    def pipeline(String name, List stages) {
        def p = this.pipeline(name);
        p.stages = stages;
    }

    def pipeline(String name, Closure body) {
        def p = this.pipeline(name);

        body.resolveStrategy = Closure.DELEGATE_FIRST;
        body.delegate = p;
        body();
    }

    def stage(String name) {
        def s = this.stages[name];

        if (s == null) {
            s =  new Stage(this, name);
            this.stages[name] = s;
        }

        return s;
    }

    def stage(String name, Closure body) {
        def p = this.stage(name);

        body.resolveStrategy = Closure.DELEGATE_FIRST;
        body.delegate = p;
        body();
    }

    def stage(String name, Stage stage) {
        return this.stages[name] = stage;
    }

    def stage(String name, Class type) {
        Constructor create = type.getConstructor(Project.class, String.class)
        Stage stage = (Stage)(create.newInstance(this, name));
        return this.stages[name] = stage;
    }

    def stage(String name, Class type, Closure body) {
        this.stage(name, type);
        this.stage(name, body);
    }

	def apply(Plugin plugin) {
		plugin.apply(this);
	}
	
    def apply(String pluginId) {
        this.log "project apply plugin:" + pluginId

        // inner plugin
        def plugin = this.pluginManager.findPlugin(pluginId);
        if (plugin != null) {
            return plugin.apply(this);
        } 

        throw new DeliveryException("plugin_not_found", "not found plugin with id:" + pluginId);
    }

    def apply(Map<String, ?> options) {
        def pluginId = options.get("plugin");
        this.apply(pluginId);
    }

    def evaluate() {
    	this.log "project evaluate!"

        try {
            // filters
            this.handleFilters {
                def changeType = this.script.env.CHANGE_TYPE;
                def pipeline = this.pipelines[changeType];

                if (pipeline != null) {
                    pipeline.evaluate();

                    this.log "project evaluate ok!"
                } else {
                    throw new DeliveryException("pipeline_not_found", "not found pipeline with changeType:" + changeType);
                }
            }
        } catch(e) {
            this.log "project evaluate fail:" + e;
            throw e;
        }
    }

}