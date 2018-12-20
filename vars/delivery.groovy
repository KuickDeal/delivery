// See https://github.com/jenkinsci/workflow-cps-global-lib-plugin
import org.yubing.delivery.Project

// jenkinsfile 默认调用
def call(body) {
    def project = new Project(this);

    // 初始化
    project.init();

    // 配置
    body.resolveStrategy = Closure.DELEGATE_FIRST;
    body.delegate = project;
    body();

    // 执行
	project.evaluate();
}