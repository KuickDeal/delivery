
package org.yubing.delivery.handler;

import java.io.Serializable;

import org.yubing.delivery.Project;

class ConfigPostHandler implements Serializable {
    def project;

	ConfigPostHandler(Project project) {
		this.project = project;
	}

    def handle() {
        def config = this.project.config;

        def body = {
            // 更新版本
            if (env.IMAGE_TAG != null && env.IMAGE_TAG != "") {
                config.version = env.IMAGE_TAG
            }
        }

        body.resolveStrategy = Closure.DELEGATE_FIRST;
        body.delegate = this.project.script;
        body();
    }
}
