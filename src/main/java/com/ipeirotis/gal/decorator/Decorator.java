package com.ipeirotis.gal.decorator;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.text.StrLookup;

public class Decorator<T> extends StrLookup<T> {
	private BeanMap decoratorBeanMap;

	private BeanMap wrappedBeanMap;

	protected T object;

	public Decorator(T wrapped) {
		this.decoratorBeanMap = new BeanMap(this);
		this.wrappedBeanMap = new BeanMap(wrapped);
		this.object = wrapped;
	}

	@Override
	public String lookup(String key) {
		Object v = lookupObject(key);

		if (v != null)
			return ObjectUtils.toString(v);

		return null;
	}

	public Object lookupObject(String key) {
		Object v = null;

		if (decoratorBeanMap.containsKey(key)) {
			v = decoratorBeanMap.get(key);
		} else if (wrappedBeanMap.containsKey(key)) {
			v = wrappedBeanMap.get(key);
		}
		
		return v;
	}
}
