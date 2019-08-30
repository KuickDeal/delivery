package org.yubing.delivery.plugin.flow;

import java.io.Serializable;

import org.yubing.delivery.Project;
import org.yubing.delivery.plugin.Plugin;

/**
 *	Flow Plugin
 */
class FlowPlugin implements Plugin<Project> {

	def wholeFlow(project) {
		 project.pipeline("WHOLE_FLOW", [
			// Build
			"PreDeploy", 
			"Skip",
			"UnitTest",
			"SonarQube",
			"BuildImage",
			"UploadImage",

			//DeployToDev
			"DeployDev",

			//DeployToQA
			"ConfirmDeployQA",
			"DeployQA",
			"ConfirmQATestOk",

			//DeployToPreProd
			"ConfirmDeployPreProd",
			"DeployPreProd",
			"PreProdApiTesting",
			"ConfirmRegressionTestOk",
			"ConfirmAcceptationTestOk",

			//DeployToProd
			"ConfirmDeployProd",
			"AccessControl",
			"DeployProd",
			"ConfirmSmokeTestOk",
			
			//Follow
			"StableTag",
			"PostDeployAutoChangeLog",
			"ConfirmAutoMergeBranch",
			"PostDeployAutoMerge"
		])
	}

	def fixFlow(project) {
		 project.pipeline("FIX_FLOW", [
			// Build
			"PreDeploy", 
			"Skip",
			"UnitTest",
			"SonarQube",
			"BuildImage",
			"UploadImage",

			//DeployToDev
			"Skip",

			//DeployToQA
			"Skip",
			"Skip",
			"Skip",

			//DeployToPreProd
			"ConfirmDeployPreProd",
			"DeployPreProd",
			"PreProdApiTesting",
			"ConfirmRegressionTestOk",
			"ConfirmAcceptationTestOk",

			//DeployToProd
			"ConfirmDeployProd",
			"AccessControl",
			"DeployProd",
			"ConfirmSmokeTestOk",
			
			//Follow
			"StableTag",
			"PostDeployAutoChangeLog",
			"ConfirmAutoMergeBranch",
			"PostDeployAutoMerge"
		])
	}

	def deployTestFlow(project) {
		project.pipeline("DEPLOY_TEST", [
			// Build
			"PreDeploy", 
			"Skip",
			"UnitTest",
			"SonarQube",
			"BuildImage",
			"UploadImage",

			//DeployToDev
			"DeployTest"
		])
	}

	def deployQAFlow(project) {
		project.pipeline("DEPLOY_QA", [
			// Build
			"PreDeploy", 
			"Skip",
			"UnitTest",
			"SonarQube",
			"BuildImage",
			"UploadImage",

			//DeployToDev
			"Skip",

			//DeployToQA
			"ConfirmDeployQA",
			"DeployQA",
			"ConfirmQATestOk",
		])
	}

	def deployPreProdFlow(project) {
		project.pipeline("DEPLOY_PreProd", [
			// Build
			"PreDeploy", 
			"Skip",
			"UnitTest",
			"SonarQube",
			"BuildImage",
			"UploadImage",

			//DeployToDev
			"Skip",

			//DeployToQA
			"Skip",
			"Skip",
			"Skip",

			//DeployToPreProd
			"ConfirmDeployPreProd",
			"DeployPreProd",
			"PreProdApiTesting",
			"ConfirmRegressionTestOk",
			"ConfirmAcceptationTestOk"
		])
	}

	def deployProdFlow(project) {
		project.pipeline("DEPLOY_PROD", [
			// Build
			"PreDeploy", 
			"Skip",
			"UnitTest",
			"SonarQube",
			"BuildImage",
			"UploadImage",

			//DeployToDev
			"Skip",

			//DeployToQA
			"Skip",
			"Skip",
			"Skip",

			//DeployToPreProd
			"Skip",
			"Skip",
			"Skip",
			"Skip",
			"Skip",

			//DeployToProd
			"ConfirmDeployProd",
			"AccessControl",
			"DeployProd",
			"ConfirmSmokeTestOk",
			
			//Follow
			"StableTag",
			"PostDeployAutoChangeLog",
			"ConfirmAutoMergeBranch",
			"PostDeployAutoMerge"
		])
	}

	def rebaseImageFlow(project) {
		project.pipeline("REBASE", [
			// Build
			"PreDeploy", 
			"BuildBaseImage"
		])
	}

	def mergeFLow(project) {
		project.pipeline("MERGE", [
			// Build
			"PreDeploy", 
			"Skip",
			"UnitTest",
			"SonarQube",
			"BuildImage"
		])

		project.pipeline("MERGE") {
			branchs = "develop|master"

			doFilter {
				gitlabCommitStatus(name: "Merge Test") {
					it.next();
					addGitLabMRComment comment: '测试完成！'
				}
			}
		}
	}

	def pushFlow(project) {
		def fixFlow = project.pipeline("FIX_FLOW");
		def wholeFlow = project.pipeline("WHOLE_FLOW");

		if (project.env.CHANGE_TARGET == "master") {
			project.pipeline("PUSH", fixFlow);
		} else if (project.env.CHANGE_TARGET == "develop") {
			project.pipeline("PUSH", wholeFlow);
		}
	}

	def registerFlows(project) {
		this.rebaseImageFlow(project);

		this.wholeFlow(project);
		this.fixFlow(project);

		this.deployTestFlow(project);
		this.deployQAFlow(project);
		this.deployPreProdFlow(project);
		this.deployProdFlow(project);
		this.deployQAFlow(project);

		this.mergeFLow(project);
		this.pushFlow(project);
	}

    def apply(Project project) {
	    project.log "apply flow plugin"

		this.registerFlows(project);

		project.log "apply flow plugin ok!"
    }

}