package com.hencjo.summer.migration.api;

public interface Function<K,V> {
	public V apply(K k);
}
