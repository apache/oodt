package org.apache.oodt.cas.cl.store.spring;

import org.springframework.context.ApplicationContext;

public interface SpringSetContextInjectionType {

	public void setContext(ApplicationContext appContext);

	public ApplicationContext getContext();
}
