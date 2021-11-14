package ru.dmitriyt.dcs.core.data.classloader

import java.io.File
import java.lang.ClassLoader
import java.lang.reflect.Constructor
import java.net.URLClassLoader

class ExtensionLoader<C> {
    /**
     * Ищет класс
     * @param classpath
     * в директории
     * @param directory
     * имплементирующий интерфейс
     * @param parentClass
     *
     * @return Возвращает класс типа интерфейса, если он найден, иначе null
     */
    fun loadClass(directory: String, classpath: String, parentClass: Class<C>): C? {
        val pluginsDir = File(System.getProperty("user.dir") + directory)
        for (jar in pluginsDir.listFiles().orEmpty()) {
            val jarClass = loadClass(jar, classpath, parentClass)
            if (jarClass != null) {
                return jarClass
            }
        }
        return null
    }

    private fun loadClass(file: File, classpath: String, parentClass: Class<C>): C? {
        return try {
            val loader: ClassLoader = URLClassLoader.newInstance(
                arrayOf(file.toURL()),
                javaClass.classLoader
            )
            val clazz = Class.forName(classpath, true, loader)
            val newClass = clazz.asSubclass(parentClass)
            val constructor: Constructor<out C> = newClass.getConstructor()
            constructor.newInstance()
        } catch (e: ClassNotFoundException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}