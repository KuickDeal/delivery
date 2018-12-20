package org.yubing.delivery.handler;

import java.io.Serializable;

import org.yubing.delivery.Project;

/**
 *	脚本处理器
 */
class ScriptHandler implements Serializable {
	def project;

	ScriptHandler(Project project) {
		this.project = project;
	}

	def ext(body) {
		body.resolveStrategy = Closure.DELEGATE_FIRST;
		body.delegate = this.project.extensions;
		body();
	}

	def repo(url) {
		this.project.log "load repo: " + url
	}
}