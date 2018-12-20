package org.yubing.delivery;

import java.io.Serializable;

import org.yubing.delivery.Project;
import org.yubing.delivery.Stage;
import org.yubing.delivery.handler.FilterChain;

class Pipeline extends FilterChain {
	def project;
	def name = "Default";

	def branchs = "*";
	def stages = [];

	Pipeline(Project project, String name) {
		this.project = project;
		this.name = name;
	}

	def match(branch) {
		if (this.branchs == "*") {
			return true;
		}
		
		if (this.branchs.contains(branch)) {
			return true;
		}

		return false;
	}

	def run() {
		this.project.log "evaluate start stage!"

		this.stages.each {
			this.project.log "evaluate start stage:" + it

			this.project.stage(it).evaluate();
		}

		this.project.log "evaluate end stage!"
	}

	def evaluate() {
		this.project.log "evaluate pipeline: ${name}"

		def branch = this.project.env.CHANGE_TARGET;
		if (!this.match(branch)) {
			throw new DeliveryException("branch_not_match", "branch $branch not match this pipeline's branchs " + this.name);
		}

		// filters
		this.handleFilters {
			this.run();
		}

		this.project.log "evaluate pipeline ${name} ok!"
	}
}