package org.example.lib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Injector {
    private static final Map<String, Injector> injectors = new HashMap<>();
    private final Map<Class<?>, Object> instancesOfClasses = new HashMap<>();
    private final List<Class<?>> classes = new ArrayList<>();

    private Injector(String mainPackageName) {
        try {
            classes.addAll(getClasses(mainPackageName));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Can't get information about all classes", e);
        }
    }

    public static Injector getInjector(String mainPackageName) {
        if (injectors.containsKey(mainPackageName)) {
            return injectors.get(mainPackageName);
        }
        Injector injector = new Injector(mainPackageName);
        injectors.put(mainPackageName, injector);
        return injector;
    }

    public Object getInstance(Class<?> certainInterface) {
        Object newInstanceOfClass = null;
        Class<?> clazz = findClassExtendingInterface(certainInterface);
        Object instanceOfCurrentClass = createInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (isFieldInitialized(field, instanceOfCurrentClass)) {
                continue;
            }
            if (field.getDeclaredAnnotation(Inject.class) != null) {
                Object classToInject = getInstance(field.getType());
                newInstanceOfClass = getNewInstance(clazz);
                setValueToField(field, newInstanceOfClass, classToInject);
            } else {
                throw new RuntimeException("Class " + field.getName() + " in class "
                        + clazz.getName() + " doesn't have annotation @Inject");
            }
        }
        if (newInstanceOfClass == null) {
            return getNewInstance(clazz);
        }
        return newInstanceOfClass;
    }

    private Class<?> findClassExtendingInterface(Class<?> certainInterface) {
        if (!certainInterface.isInterface()
                && certainInterface.isAnnotationPresent(Component.class)) {
            return certainInterface;
        }
        for (Class<?> clazz : classes) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> singleInterface : interfaces) {
                if (singleInterface.equals(certainInterface)
                        && clazz.isAnnotationPresent(Component.class)) {
                    return clazz;
                }
            }
        }
        throw new RuntimeException("Can't find class which implements "
                + certainInterface.getName()
                + " interface and has valid annotation @Component");
    }

    private Object getNewInstance(Class<?> certainClass) {
        if (instancesOfClasses.containsKey(certainClass)) {
            return instancesOfClasses.get(certainClass);
        }
        Object newInstance = createInstance(certainClass);
        instancesOfClasses.put(certainClass, newInstance);
        return newInstance;
    }

    private boolean isFieldInitialized(Field field, Object instance) {
        field.setAccessible(true);
        try {
            return field.get(instance) != null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't get access to field " + field.getName());
        }
    }

    private Object createInstance(Class<?> clazz) {
        Object newInstance;
        try {
            Constructor<?> classConstructor = clazz.getConstructor();
            newInstance = classConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create object of the class " + clazz.getName(), e);
        }
        return newInstance;
    }

    private void setValueToField(Field field, Object instanceOfClass, Object classToInject) {
        try {
            field.setAccessible(true);
            field.set(instanceOfClass, classToInject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't set value to field " + field.getName(), e);
        }
    }

    private static List<Class<?>> getClasses(String packageName)
            throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new RuntimeException("Class loader is null");
        }
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().contains(".")) {
                        throw new RuntimeException("File name shouldn't contain points.");
                    }
                    classes.addAll(findClasses(file, packageName + "."
                            + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.'
                            + file.getName().substring(0, file.getName().length() - 6)));
                }
            }
        }
        return classes;
    }
}
