package org.yubing.delivery;

import java.io.Serializable;

import org.yubing.delivery.Project;
import org.yubing.delivery.handler.FilterChain;

class Stage extends FilterChain {
	Project project;
	String name;

	def script;
	def config;

	Closure body;

	Stage(Project project, String name) {
		this.project = project;
		this.name = name;

		this.script = project.script;
		this.config = project.config;

		this.body = {
			this.project.log "skip stage ${name}"
		};
	}

	def run() {
		this.body.resolveStrategy = Closure.DELEGATE_FIRST;
		this.body.delegate = this;
		this.body();
	}

	def evaluate() {
		this.project.log "evaluate stage: ${name}"

		this.project.script.stage(this.name) {
			// filters
			this.handleFilters {
				this.run();
			}
		}

		this.project.log "evaluate stage: ${name} ok!"
	}
}