package org.yubing.delivery.plugin;

import java.io.Serializable;

/**
 *	插件接口
 */
public interface Plugin<T> {
	
	/**
     * Apply this plugin to the given target object.
     *
     * @param target The target object
     */
    def apply(T target);
}