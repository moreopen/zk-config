package com.moreopen.config.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZkConfig {
	public enum Type {String, Int, Long, Double};
	String key();
	Type type() default Type.String;
}
