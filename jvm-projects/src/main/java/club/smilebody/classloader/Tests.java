package club.smilebody.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author jasonj
 * @date 2024/1/17
 * @time 22:11
 * @description
 **/
public class Tests {


    public static void main(String[] args) throws ClassNotFoundException {

        // bootstrap -> ext -> system(app) (类路径) -> 内存泄漏(如果不摧毁) 方法区激增
        // jdk 公用

        // 双亲委派
        MyClassLoader classLoader = new MyClassLoader(ClassLoader.getSystemClassLoader(),true);
        MyClassLoader classLoader2 = new MyClassLoader(ClassLoader.getSystemClassLoader(),true);

        Class<?> aClass = classLoader.loadClass("java.lang.String");
        Class<?> aClass1 = classLoader2.loadClass("java.lang.String");


        System.out.println(aClass1.hashCode());
        System.out.println(aClass.hashCode());
        System.out.println(aClass1 == aClass);

        // 打破双亲委派

        Class<?> aClass2 = classLoader.loadClass("club.smilebody.classloader.Tests$AClassLoaderWrapper$Aaab");
//        Class<?> aClass5 = classLoader.loadClass("club.smilebody.classloader.Tests$BClassLoaderWrapper$Aaac");

        Class<?> aClass3 = classLoader2.loadClass("club.smilebody.classloader.Tests$BClassLoaderWrapper$Aaac");
//        Class<?> aClass4 = classLoader2.loadClass("club.smilebody.classloader.Tests$AClassLoaderWrapper$Aaab");

        System.out.println(aClass2);
        System.out.println(aClass3);

        System.out.println(classLoader.getAabCount() == ClassLoader.getSystemClassLoader().loadClass("club.smilebody.classloader.Tests$AClassLoaderWrapper$Aaab").hashCode());
        System.out.println(classLoader2.getAacCount() ==ClassLoader.getSystemClassLoader().loadClass("club.smilebody.classloader.Tests$BClassLoaderWrapper$Aaac").hashCode());

        System.out.println(ClassLoader.getSystemClassLoader().loadClass("club.smilebody.classloader.Tests$BClassLoaderWrapper$Aaac").hashCode() == classLoader2.getAacCount());

    }

    public static class AClassLoaderWrapper {
        public static class Aaab {

        }


    }

    public static class BClassLoaderWrapper {
        public static class Aaac{

        }
    }

    public static class MyClassLoader extends ClassLoader{


        private int aacHash = 0;

        private int aabHash = 0;

        private boolean isCustom;

        public MyClassLoader(ClassLoader parent,boolean isCustom) {
            super(parent);
            this.isCustom = isCustom;
        }

        public void setCustom(boolean custom) {
            isCustom = custom;
        }

        // 如果重写findClass 没有打破双清委派
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if(name.contains("aab")) {
                if(aabHash == 0) {
                    aabHash = new Object().hashCode();
                }
                return AClassLoaderWrapper.Aaab.class;
            }

            if(name.contains("aac")) {
                if(aacHash == 0) {
                    aacHash = new Object().hashCode();
                }
                return BClassLoaderWrapper.Aaac.class;
            }

            return super.findClass(name);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if(isCustom) {
                if(name.contains("aab")) {
                   return findClass(name);
                }

                if(name.contains("aac")) {
                    return findClass(name);
                }

                return this.getParent().loadClass(name);
            }

            Class<?> aClass = super.loadClass(name);
            if(name.contains("aab")) {
                aabHash = aClass.hashCode();
            }

            if(name.contains("aac")) {
                aacHash = aClass.hashCode();
            }

           return aClass;
        }

        public int getAabCount() {
            return aabHash;
        }

        public int getAacCount() {
            return aacHash;
        }
    }
}
