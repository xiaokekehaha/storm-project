package com.kuxun.kxtopology.objectassist;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
/**
 * 提供了实例化对象的工具方法
 * @author dengzh
 */
public class BeanUtils {

	private static final char PACKAGE_SEPARATOR = '.';
	private static final char INNER_CLASS_SEPARATOR = '$';

	public static Class<?> forName(String name, ClassLoader classLoader)
			throws ClassNotFoundException, LinkageError {
		Class<?> clazz = null;
		ClassLoader clToUse = classLoader;
		if (clToUse == null) {
			clToUse = getDefaultClassLoader();
		}
		try {
			return (clToUse != null ? clToUse.loadClass(name) : Class
					.forName(name));
		} catch (ClassNotFoundException ex) {
			int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
			if (lastDotIndex != -1) {
				String innerClassName = name.substring(0, lastDotIndex)
						+ INNER_CLASS_SEPARATOR
						+ name.substring(lastDotIndex + 1);
				try {
					return (clToUse != null ? clToUse.loadClass(innerClassName)
							: Class.forName(innerClassName));
				} catch (ClassNotFoundException ex2) {
				}
			}
			throw ex;
		}
	}

	public static Class<?> resolveClassName(String className,
			ClassLoader classLoader) throws IllegalArgumentException {
		try {
			return forName(className, classLoader);
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Cannot find class ["
					+ className + "]", ex);
		} catch (LinkageError ex) {
			throw new IllegalArgumentException("Error loading class ["
					+ className
					+ "]: problem with class file or dependent class.", ex);
		}
	}

	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
		}
		if (cl == null) {
			cl = BeanUtils.class.getClassLoader();
			if (cl == null) {
				try {
					cl = ClassLoader.getSystemClassLoader();
				} catch (Throwable ex) {
					
				}
			}
		}
		return cl;
	}

	public static <T> T instantiate(Class<T> clazz) throws RuntimeException {
		if (clazz.isInterface()) {
			throw new RuntimeException("Specified class is an interface:" + clazz);
		}
		try {
			return clazz.newInstance();
		} catch (InstantiationException ex) {
			throw new RuntimeException("Is it an abstract class?" + clazz);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Is the constructor accessible?" + clazz);
		}
	}

	public static <T> T instantiateClass(Class<T> clazz) throws RuntimeException {
		if (clazz.isInterface()) {
			throw new RuntimeException("Specified class is an interface" + clazz);
		}
		try {
			return instantiateClass(clazz.getDeclaredConstructor());
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("No default constructor found" + clazz);
		}
	}

	public static <T> T instantiateClass(Constructor<T> ctor, Object... args)
			throws RuntimeException {
		try {
			return ctor.newInstance(args);
		} catch (InstantiationException ex) {
			throw new RuntimeException("Is it an abstract class?"
					+ ctor.getDeclaringClass());
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Is the constructor accessible?"
					+ ctor.getDeclaringClass());
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException("Illegal arguments for constructor"
					+ ctor.getDeclaringClass());
		} catch (InvocationTargetException ex) {
			throw new RuntimeException("Constructor threw exception"
					+ ctor.getDeclaringClass());
		}
	}

	
	
}
