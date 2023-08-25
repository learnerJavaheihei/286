package l2s.gameserver.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import l2s.commons.compiler.Compiler;
import l2s.commons.compiler.MemoryClassLoader;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.listener.script.OnLoadScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.annotation.OnScriptInit;
import l2s.gameserver.scripts.annotation.OnScriptLoad;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scripts
{
	public class ScriptListenerImpl extends ListenerList<Scripts>
	{
		public void load()
		{
			for(Listener<Scripts> listener : getListeners())
				if(OnLoadScriptListener.class.isInstance(listener))
					((OnLoadScriptListener) listener).onLoad();
		}

		public void init()
		{
			for(Listener<Scripts> listener : getListeners())
				if(OnInitScriptListener.class.isInstance(listener))
					((OnInitScriptListener) listener).onInit();
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(Scripts.class);

	private static final Scripts _instance = new Scripts();

	public static Scripts getInstance()
	{
		return _instance;
	}

	private final Map<String, Class<?>> _classes = new TreeMap<String, Class<?>>();
	private final Map<Class<?>, Object> _instances = new ConcurrentHashMap<Class<?>, Object>();
	private final ScriptListenerImpl _listeners = new ScriptListenerImpl();

	private Scripts()
	{
		load();
	}

	/**
	 * Вызывается при загрузке сервера. Загрузает все скрипты в data/scripts. Не инициирует объекты и обработчики.
	 *
	 * @return true, если загрузка прошла успешно
	 */
	private void load()
	{
		_log.info("Scripts: Loading...");

		List<Class<?>> classes = load(new File(Config.DATAPACK_ROOT, "data/scripts"));

		if(classes.isEmpty())
			throw new Error("Failed loading scripts!");

		for(Class<?> clazz : classes)
			_classes.put(clazz.getName(), clazz);

		File f = new File("./lib/scripts.jar");
		if(f.exists())
		{
			_log.info("Scripts: Loading library...");

			JarInputStream stream = null;
			try
			{
				stream = new JarInputStream(new FileInputStream(f));
				JarEntry entry = null;
				while((entry = stream.getNextJarEntry()) != null)
				{
					//Вложенные класс
					if(entry.getName().contains(ClassUtils.INNER_CLASS_SEPARATOR) || !entry.getName().endsWith(".class"))
						continue;

					String name = entry.getName().replace(".class", "").replace("/", ".");
					if(_classes.containsKey(name))
						continue;

					Class<?> clazz = getClass().getClassLoader().loadClass(name);
					if(Modifier.isAbstract(clazz.getModifiers()))
						continue;

					_classes.put(clazz.getName(), clazz);
				}
			}
			catch(Exception e)
			{
				throw new Error("Failed loading scripts library!");
			}
			finally
			{
				try
				{
					if(stream != null)
						stream.close();
				}
				catch(IOException ioe)
				{
					//
				}
			}
		}

		_log.info("Scripts: Loaded " + _classes.size() + " classes.");

		for(Class<?> clazz : _classes.values())
		{
			try
			{
				Object o = getClassInstance(clazz);
				if(ClassUtils.isAssignable(clazz, OnLoadScriptListener.class))
				{
					if(o == null)
						o = clazz.newInstance();

					_listeners.add((OnLoadScriptListener) o);
				}

				for(Method method : clazz.getMethods())
				{
					if(method.isAnnotationPresent(OnScriptLoad.class))
					{
						Class<?>[] par = method.getParameterTypes();
						if(par.length != 0)
						{
							_log.error("Wrong parameters for load method: " + method.getName() + ", class: " + clazz.getSimpleName());
							continue;
						}

						try
						{
							if(Modifier.isStatic(method.getModifiers()))
								method.invoke(clazz);
							else
							{
								if(o == null)
									o = clazz.newInstance();
								method.invoke(o);
							}
						}
						catch(Exception e)
						{
							_log.error("Exception: " + e, e);
						}
					}
				}
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}

		_listeners.load();
	}

	/**
	 * Вызывается при загрузке сервера. Инициализирует объекты и обработчики.
	 */
	public void init()
	{
		for(Class<?> clazz : _classes.values())
			init(clazz);

		_listeners.init();
	}

	/**
	 * Загрузить все классы в data/scripts/target
	 *
	 * @param target путь до класса, или каталога со скриптами
	 * @return список загруженых скриптов
	 */
	public List<Class<?>> load(File target)
	{
		Collection<File> scriptFiles = Collections.emptyList();

		if(target.isFile())
		{
			scriptFiles = new ArrayList<File>(1);
			scriptFiles.add(target);
		}
		else if(target.isDirectory())
		{
			scriptFiles = FileUtils.listFiles(target, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
		}

		if(scriptFiles.isEmpty())
			return Collections.emptyList();

		List<Class<?>> classes = new ArrayList<Class<?>>();
		Compiler compiler = new Compiler();

		if(compiler.compile(scriptFiles))
		{
			MemoryClassLoader classLoader = compiler.getClassLoader();
			for(String name : classLoader.getLoadedClasses())
			{
				//Вложенные класс
				if(name.contains(ClassUtils.INNER_CLASS_SEPARATOR))
					continue;

				try
				{
					Class<?> clazz = classLoader.loadClass(name);
					if(Modifier.isAbstract(clazz.getModifiers()))
						continue;

					classes.add(clazz);
				}
				catch(ClassNotFoundException e)
				{
					_log.error("Scripts: Can't load script class: " + name, e);
					classes.clear();
					break;
				}
			}
		}

		return classes;
	}


	private Object init(Class<?> clazz)
	{
		Object o = getClassInstance(clazz);
		try
		{
			if(ClassUtils.isAssignable(clazz, OnInitScriptListener.class))
			{
				if(o == null)
					o = clazz.newInstance();
				_listeners.add((OnInitScriptListener) o);
			}

			for(Method method : clazz.getMethods())
			{
				if(method.isAnnotationPresent(Bypass.class))
				{
					Class<?>[] par = method.getParameterTypes();
					if(par.length == 0 || par[0] != Player.class || par[1] != NpcInstance.class || par[2] != String[].class)
					{
						_log.error("Wrong parameters for bypass method: " + method.getName() + ", class: " + clazz.getSimpleName());
						continue;
					}

					Bypass an = method.getAnnotation(Bypass.class);
					if(Modifier.isStatic(method.getModifiers()))
						BypassHolder.getInstance().registerBypass(an.value(), clazz, method);
					else
					{
						if(o == null)
							o = clazz.newInstance();
						BypassHolder.getInstance().registerBypass(an.value(), o, method);
					}
				}
				else if(method.isAnnotationPresent(OnScriptInit.class))
				{
					Class<?>[] par = method.getParameterTypes();
					if(par.length != 0)
					{
						_log.error("Wrong parameters for init method: " + method.getName() + ", class: " + clazz.getSimpleName());
						continue;
					}

					try
					{
						if(Modifier.isStatic(method.getModifiers()))
							method.invoke(clazz);
						else
						{
							if(o == null)
								o = clazz.newInstance();
							method.invoke(o);
						}
					}
					catch(Exception e)
					{
						_log.error("Exception: " + e, e);
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		return o;
	}


	public Map<String, Class<?>> getClasses()
	{
		return _classes;
	}

	public Object getClassInstance(Class<?> clazz)
	{
		return _instances.get(clazz);
	}

	public Object getClassInstance(String className)
	{
		Class<?> clazz = _classes.get(className);
		if(clazz != null)
			return getClassInstance(clazz);
		return null;
	}
}