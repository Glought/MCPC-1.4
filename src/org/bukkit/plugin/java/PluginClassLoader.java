package org.bukkit.plugin.java;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.relauncher.RelaunchClassLoader;

public class PluginClassLoader
{
	public final RelaunchClassLoader link;
	private final JavaPluginLoader loader;
	private final Map<String, Class<?>> classes = new HashMap();

	public PluginClassLoader(JavaPluginLoader loader, URL[] urls, RelaunchClassLoader parent) {

		this.link = parent;
		this.loader = loader;
	}

	public void addURL(URL url)
	{
		link.addURL(url);
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		return findClass(name, true);
	}

	protected Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
		if ((name.startsWith("org.bukkit.")) || (name.startsWith("net.minecraft."))) {
			throw new ClassNotFoundException(name);
		}
		Class result = (Class)this.classes.get(name);

		if (result == null) {
			if (checkGlobal) {
				result = this.loader.getClassByName(name);
			}

			if (result == null) {
				result = link.findClass(name);

				if (result != null) {
					this.loader.setClass(name, result);
				}
			}

			this.classes.put(name, result);
		}

		return result;
	}

	public Set<String> getClasses() {
		return this.classes.keySet();
	}
}