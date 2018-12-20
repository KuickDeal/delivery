package org.yubing.delivery.handler;

import java.io.Serializable;

/**
 *	过滤链链条
 */
class FilterChain implements Serializable {

	public static class FilterIterator {
		def i = 0;
		def filters = [];
		def scope;
		def finish;

		FilterIterator(filters, scope, finish) {
			this.filters.addAll(filters);
			this.scope = scope;
			this.finish = finish;
		}

		def handleFilter(filter, it) {
			filter.resolveStrategy = Closure.DELEGATE_FIRST;
			filter.delegate = this.scope;
			filter(it);
		}

		def handleEnd() {
			finish.resolveStrategy = Closure.DELEGATE_FIRST;
			finish.delegate = this.scope;
			finish();
		}

		def next() {
			if (i < filters.size()) {
				this.handleFilter(filters[i++], this);
			} else {
				this.handleEnd();
			}
		}
	}

	protected def filters = [];

	def doFilter(Closure filter) {
		this.filters.add(filter)
	}

	def doBefore(Closure step) {
		this.filters.add { it ->
            this.handleBody(step);
            it.next();
        }
	}

	def doAfter(Closure step) {
		this.filters.add { it ->
            it.next();
            this.handleBody(step);
        }
	}

	protected def handleBody(body) {
		body.resolveStrategy = Closure.DELEGATE_FIRST;
		body.delegate = this;
		body();
	}

	protected def handleFilters(Closure body) {
		def it = new FilterIterator(this.filters, this, body);
		it.next();
	}
}