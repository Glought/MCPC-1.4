package org.bukkit.plugin.java;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.relauncher.FMLRelauncher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.Warning;
import org.bukkit.Warning.WarningState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.TimedRegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.yaml.snakeyaml.error.YAMLException;

public class JavaPluginLoader
implements PluginLoader
{
	private final Server server;
	protected final Pattern[] fileFilters = { Pattern.compile("\\.jar$") };
	protected final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	protected final Map<String, JavaPlugin> loaders = new LinkedHashMap<String, JavaPlugin>();

	public JavaPluginLoader(Server instance) {
		this.server = instance;
	}

	public Plugin loadPlugin(File file) throws InvalidPluginException {
		Validate.notNull(file, "File cannot be null");

		if (!file.exists()) {
			throw new InvalidPluginException(new FileNotFoundException(file.getPath() + " does not exist"));
		}
		PluginDescriptionFile description;
		try
		{
			description = getPluginDescription(file);
		} catch (InvalidDescriptionException ex) {
			throw new InvalidPluginException(ex);
		}

		File dataFolder = new File(file.getParentFile(), description.getName());
		File oldDataFolder = getDataFolder(file);

		if (!dataFolder.equals(oldDataFolder))
		{
			if ((dataFolder.isDirectory()) && (oldDataFolder.isDirectory())) {
				this.server.getLogger().log(Level.INFO, String.format("While loading %s (%s) found old-data folder: %s next to the new one: %s", new Object[] { description.getName(), file, oldDataFolder, dataFolder }));
			}
			else if ((oldDataFolder.isDirectory()) && (!dataFolder.exists())) {
				if (!oldDataFolder.renameTo(dataFolder)) {
					throw new InvalidPluginException("Unable to rename old data folder: '" + oldDataFolder + "' to: '" + dataFolder + "'");
				}
				this.server.getLogger().log(Level.INFO, String.format("While loading %s (%s) renamed data folder: '%s' to '%s'", new Object[] { description.getName(), file, oldDataFolder, dataFolder }));
			}

		}

		if ((dataFolder.exists()) && (!dataFolder.isDirectory())) {
			throw new InvalidPluginException(String.format("Projected datafolder: '%s' for %s (%s) exists and is not a directory", new Object[] { dataFolder, description.getName(), file }));
		}

		List<String> depend = description.getDepend();
		if (depend == null) {
			depend = ImmutableList.of();
		}
/*
		for (String pluginName : depend) {
			if (this.loaders == null) {
				throw new UnknownDependencyException(pluginName);
			}
			JavaPlugin current = this.loaders.get(pluginName);

			if (current == null) {
				throw new UnknownDependencyException(pluginName);
			}
		}*/

		ClassLoader loader = null;
		JavaPlugin result = null;
		try
		{
			// Bukkit already adds all the plugin jar's to the FML loader list on startup.
			loader = FMLRelauncher.instance().classLoader;
			/*
			URL[] urls = new URL[1];

			urls[0] = file.toURI().toURL();

			if (description.getClassLoaderOf() != null) {
				loader = (PluginClassLoader)this.loaders.get(description.getClassLoaderOf());
				loader.addURL(urls[0]);
			} else {
				loader = new PluginClassLoader(this, urls, FMLRelauncher.instance().classLoader);
			}
			*/
			Class jarClass = Class.forName(description.getMain(), true, loader);
			Class plugin = jarClass.asSubclass(JavaPlugin.class);

			Constructor constructor = plugin.getConstructor(new Class[0]);

			result = (JavaPlugin)constructor.newInstance(new Object[0]);

			result.initialize(this, this.server, description, dataFolder, file, loader);
		} catch (InvocationTargetException ex) {
			throw new InvalidPluginException(ex.getCause());
		} catch (Throwable ex) {
			throw new InvalidPluginException(ex);
		}

		//this.loaders.put(description.getName(), loader);

		return result;
	}

	public Plugin loadPlugin(File file, boolean ignoreSoftDependencies) throws InvalidPluginException {
		return loadPlugin(file);
	}

	protected File getDataFolder(File file) {
		File dataFolder = null;

		String filename = file.getName();
		int index = file.getName().lastIndexOf(".");

		if (index != -1) {
			String name = filename.substring(0, index);

			dataFolder = new File(file.getParentFile(), name);
		}
		else
		{
			dataFolder = new File(file.getParentFile(), filename + "_");
		}

		return dataFolder;
	}

	public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
		Validate.notNull(file, "File cannot be null");

		JarFile jar = null;
		InputStream stream = null;
		try
		{
			jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry("plugin.yml");

			if (entry == null) {
				throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain plugin.yml"));
			}

			stream = jar.getInputStream(entry);

			return new PluginDescriptionFile(stream);
		}
		catch (IOException ex) {
			throw new InvalidDescriptionException(ex);
		} catch (YAMLException ex) {
			throw new InvalidDescriptionException(ex);
		} finally {
			if (jar != null)
				try {
					jar.close();
				}
			catch (IOException e) {
			}
			if (stream != null)
				try {
					stream.close();
				}
			catch (IOException e) {
			}
		}
	}

	public Pattern[] getPluginFileFilters() {
		return this.fileFilters;
	}

	public Class<?> getClassByName(String name) {
		Class cachedClass = (Class)this.classes.get(name);

		if (cachedClass != null)
			return cachedClass;
		
		try {
			cachedClass = FMLRelauncher.instance().classLoader.findClass(name);
		} catch (ClassNotFoundException e) { } 
		
		return cachedClass;
		/*
		for (String current : this.loaders.keySet()) {
			JavaPlugin loader = this.loaders.get(current);
			try
			{
				cachedClass = FMLRelauncher.instance().classLoader.findClass(name); 
			} catch (ClassNotFoundException cnfe) { }
			if (cachedClass != null) {
				return cachedClass;
			}
		}

		return null;*/
	}

	public void setClass(String name, Class<?> clazz) {
		if (!this.classes.containsKey(name)) {
			this.classes.put(name, clazz);

			if (ConfigurationSerializable.class.isAssignableFrom(clazz)) {
				Class serializable = clazz.asSubclass(ConfigurationSerializable.class);
				ConfigurationSerialization.registerClass(serializable);
			}
		}
	}

	public void removeClass(String name) {
		Class clazz = (Class)this.classes.remove(name);
		try
		{
			if ((clazz != null) && (ConfigurationSerializable.class.isAssignableFrom(clazz))) {
				Class serializable = clazz.asSubclass(ConfigurationSerializable.class);
				ConfigurationSerialization.unregisterClass(serializable);
			}
		} catch (NullPointerException ex) {
		}
	}

	public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) { Validate.notNull(plugin, "Plugin can not be null");
	Validate.notNull(listener, "Listener can not be null");

	boolean useTimings = this.server.getPluginManager().useTimings();
	Map ret = new HashMap();
	Set<Method> methods;
	try { Method[] publicMethods = listener.getClass().getMethods();
	methods = new HashSet<Method>(publicMethods.length, 3.4028235E+38F);
	for (Method method : publicMethods) {
		methods.add(method);
	}
	for (Method method : listener.getClass().getDeclaredMethods())
		methods.add(method);
	} catch (NoClassDefFoundError e)
	{
		plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
		return ret;
	}

	for (final Method method : methods) {
		EventHandler eh = (EventHandler)method.getAnnotation(EventHandler.class);
		if (eh != null) {
			Class checkClass = method.getParameterTypes()[0];
			if ((!Event.class.isAssignableFrom(checkClass)) || (method.getParameterTypes().length != 1)) {
				plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
			}
			else {
				final Class eventClass = checkClass.asSubclass(Event.class);
				method.setAccessible(true);
				Set eventSet = (Set)ret.get(eventClass);
				if (eventSet == null) {
					eventSet = new HashSet();
					ret.put(eventClass, eventSet);
				}

				for (Class clazz = eventClass; Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass())
				{
					if (clazz.getAnnotation(Deprecated.class) != null) {
						Warning warning = (Warning)clazz.getAnnotation(Warning.class);
						Warning.WarningState warningState = this.server.getWarningState();
						if (!warningState.printFor(warning)) {
							break;
						}
						plugin.getLogger().log(Level.WARNING, String.format("\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated. \"%s\"; please notify the authors %s.", new Object[] { plugin.getDescription().getFullName(), clazz.getName(), method.toGenericString(), (warning != null) && (warning.reason().length() != 0) ? warning.reason() : "Server performance will be affected", Arrays.toString(plugin.getDescription().getAuthors().toArray()) }), warningState == Warning.WarningState.ON ? new AuthorNagException(null) : null);

						break;
					}
				}

				EventExecutor executor = new EventExecutor() {
					public void execute(Listener listener, Event event) throws EventException {
						try {
							if (!eventClass.isAssignableFrom(event.getClass())) {
								return;
							}
							method.invoke(listener, new Object[] { event });
						} catch (InvocationTargetException ex) {
							throw new EventException(ex.getCause());
						} catch (Throwable t) {
							throw new EventException(t);
						}
					}
				};
				if (useTimings)
					eventSet.add(new TimedRegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
				else
					eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled())); 
			}
		}
	}
	return ret; 
	}

	public void enablePlugin(Plugin plugin)
	{
		if (!(plugin instanceof JavaPlugin)) {
			throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
		}

		if (!plugin.isEnabled()) {
			String message = String.format("Enabling %s", new Object[] { plugin.getDescription().getFullName() });
			plugin.getLogger().info(message);

			JavaPlugin jPlugin = (JavaPlugin)plugin;

			String pluginName = jPlugin.getDescription().getName();
/*
			if (!this.loaders.containsKey(pluginName)) {
				this.loaders.put(pluginName, (PluginClassLoader)jPlugin.getClassLoader());*/

			try
			{
				jPlugin.setEnabled(true);
			} catch (Throwable ex) {
				this.server.getLogger().log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
			}

			this.server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
		}
	}

	public void disablePlugin(Plugin plugin) {
		if (!(plugin instanceof JavaPlugin)) {
			throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
		}

		if (plugin.isEnabled()) {
			String message = String.format("Disabling %s", new Object[] { plugin.getDescription().getFullName() });
			plugin.getLogger().info(message);

			this.server.getPluginManager().callEvent(new PluginDisableEvent(plugin));

			JavaPlugin jPlugin = (JavaPlugin)plugin;
			ClassLoader cloader = jPlugin.getClassLoader();
			try
			{
				jPlugin.setEnabled(false);
			} catch (Throwable ex) {
				this.server.getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
			}

			this.loaders.remove(jPlugin.getDescription().getName());
/*
			
			if ((cloader instanceof ClassLoader)) {
				ClassLoader loader = (ClassLoader)cloader;
				Set<String> names = loader.getClasses();

				for (String name : names)
					removeClass(name);
			}*/
		}
	}
}