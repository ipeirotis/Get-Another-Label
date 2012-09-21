package com.ipeirotis.gal.decorator;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.text.StrLookup;

@SuppressWarnings("rawtypes")
public class Decorator<E> extends StrLookup {
	private BeanMap decoratorBeanMap;

	private BeanMap wrappedBeanMap;

	protected E object;

	public Decorator(E wrapped) {
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
		} else {
			throw new IllegalStateException("Unable to lookup value for key '"
					+ key + "'");
		}

		return v;
	}
}
