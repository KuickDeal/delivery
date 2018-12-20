package org.yubing.delivery.handler;

import java.io.Serializable;

import org.yubing.delivery.Project;
import org.yubing.delivery.DeliveryException;

/**
 *	脚本处理器
 */
class StagesHandler implements Serializable {
	def project;

	StagesHandler(Project project) {
		this.project = project;
	}

    def methodMissing(String name, Object args) {
        Object[] arr = (Object[]) args;

        if (arr.length >= 1 && (arr[0] instanceof Map)) {
            Map opts = (Map)arr[0];

            Class type = opts.get("type");
            if (type == null) {
                throw new DeliveryException("type_miss", "method:" + name + ", args:" + args + " miss type!");
            }

            if (arr.length == 1) {
                return this.project.stage(name, type)
            } else if (arr.length == 2 && arr[1] instanceof Closure) {
                def body = (Closure)arr[1];
                return this.project.stage(name, type, body);
            }
        } else {
            throw new DeliveryException("method_not_found", "method:" + name + ",args:" + args + "not support!");
        }
    }

}