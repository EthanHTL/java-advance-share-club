package org.example.spring.test.integration.support.each;

import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.core.DecoratingClassLoader;
import org.springframework.core.OverridingClassLoader;
import org.springframework.core.SmartClassLoader;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// copy
public class ContextTypeMatchClassLoader extends DecoratingClassLoader implements SmartClassLoader {
    private static Method findLoadedClassMethod;
    private final Map<String, byte[]> bytesCache = new ConcurrentHashMap(256);

    public ContextTypeMatchClassLoader(@Nullable ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return (new ContextOverridingClassLoader(this.getParent())).loadClass(name);
    }

    public boolean isClassReloadable(Class<?> clazz) {
        return clazz.getClassLoader() instanceof ContextOverridingClassLoader;
    }

    public Class<?> publicDefineClass(String name, byte[] b, @Nullable ProtectionDomain protectionDomain) {
        return this.defineClass(name, b, 0, b.length, protectionDomain);
    }

    static {
        ClassLoader.registerAsParallelCapable();

        try {
            findLoadedClassMethod = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
        } catch (NoSuchMethodException var1) {
            throw new IllegalStateException("Invalid [java.lang.ClassLoader] class: no 'findLoadedClass' method defined!");
        }
    }

    private class ContextOverridingClassLoader extends OverridingClassLoader {
        public ContextOverridingClassLoader(ClassLoader parent) {
            super(parent);
        }

        protected boolean isEligibleForOverriding(String className) {
            if (!this.isExcluded(className) && !ContextTypeMatchClassLoader.this.isExcluded(className)) {
                ReflectionUtils.makeAccessible(ContextTypeMatchClassLoader.findLoadedClassMethod);

                for(ClassLoader parent = this.getParent(); parent != null; parent = parent.getParent()) {
                    if (ReflectionUtils.invokeMethod(ContextTypeMatchClassLoader.findLoadedClassMethod, parent, new Object[]{className}) != null) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        protected Class<?> loadClassForOverriding(String name) throws ClassNotFoundException {
            byte[] bytes = (byte[])ContextTypeMatchClassLoader.this.bytesCache.get(name);
            if (bytes == null) {
                bytes = this.loadBytesForClass(name);
                if (bytes == null) {
                    return null;
                }

                ContextTypeMatchClassLoader.this.bytesCache.put(name, bytes);
            }

            return this.defineClass(name, bytes, 0, bytes.length);
        }
    }
}