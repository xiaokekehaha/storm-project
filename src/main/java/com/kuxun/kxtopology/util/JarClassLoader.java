package com.kuxun.kxtopology.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class JarClassLoader extends ClassLoader {
	private Hashtable classes = new Hashtable();
	private Hashtable resources = new Hashtable();
	private ClassLoader pcl;

	public JarClassLoader(File jarFile, ClassLoader parent) throws IOException {
		this(new FileInputStream(jarFile), parent, isSecure(jarFile));
	}

	private static boolean isSecure(File jarFile) {
		if (jarFile.getName().toLowerCase().endsWith(".rc"))
			return false;
		return true;
	}

	public JarClassLoader(InputStream jar, ClassLoader parent, boolean secured) throws IOException {
		super(parent);
		if (secured)
			throw new IOException("secured core files are not supported");
		this.pcl = parent;
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(jar));
		try {
			byte[] buffer = new byte[0xffff];
			int bytes_read;
			ZipEntry ze;
			byte[] barr;
			while ((ze = zis.getNextEntry()) != null) {
				if (!ze.isDirectory()) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while ((bytes_read = zis.read(buffer)) != -1)
						baos.write(buffer, 0, bytes_read);
					String name = ze.getName().replace('\\', '/');
					barr = baos.toByteArray();
					if (name.endsWith(".class")) {
						String className = name.substring(0, name.length() - 6);
						className = className.replace('/', '.');
						classes.put(className, barr);
					}
					resources.put(name, barr);
					zis.closeEntry();
					baos.close();
				}
			}
		} finally {
			if (zis != null)
				zis.close();
		}
	}

	public Class findClass(String className) throws ClassNotFoundException {
		byte[] classBytes = (byte[]) classes.get(className);
		if (classBytes == null)
			throw new ClassNotFoundException("class [" + className + "] not found");
		return defineClass(className, classBytes, 0, classBytes.length);
	}

	private Class findClassEL(String className) {
		byte[] classBytes = (byte[]) classes.get(className);
		if (classBytes == null)
			return null;
		return defineClass(className, classBytes, 0, classBytes.length);
	}

	public Class loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class c = findLoadedClass(name);
		if (c == null) {
			c = findClassEL(name);
			if (c == null) {
				c = pcl.loadClass(name);
			}
		}
		if (resolve) {
			resolveClass(c);
		}
		return c;
	}

	public byte[] getResourceAsByte(String name) throws IOException {
		name = name.replace('\\', '/');
		byte[] bytes = (byte[]) resources.get(name);
		if (bytes == null) {
			InputStream is = super.getResourceAsStream(name);
			byte[] buffer = new byte[0xffff];
			int bytes_read;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				while ((bytes_read = is.read(buffer)) != -1)
					baos.write(buffer, 0, bytes_read);
				bytes = baos.toByteArray();
			} finally {
				if (baos != null)
					baos.close();
			}
		}
		return bytes;
	}

	public InputStream getResourceAsStream(String name) {
		name = name.replace('\\', '/');
		byte[] bytes = (byte[]) resources.get(name);
		if (bytes == null)
			return super.getResourceAsStream(name);
		return new ByteArrayInputStream(bytes);
	}
	
	 public void release(){
		 classes = null;
		 resources = null;
		 pcl = null;
	 }
}
