package com.parallel.learn.jvm.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 类加载器演示
 * 用于演示JVM类加载机制
 */
@Slf4j
@RestController
@RequestMapping("/jvm/classloader")
public class ClassLoaderDemo {

    /**
     * 获取类加载器层次结构
     */
    @GetMapping("/hierarchy")
    public String getClassLoaderHierarchy() {
        StringBuilder sb = new StringBuilder();

        // 获取当前类的类加载器
        ClassLoader classLoader = this.getClass().getClassLoader();

        sb.append("类加载器层次结构: ");

        while (classLoader != null) {
            sb.append(classLoader.getClass().getName())
              .append(" (")
              .append(classLoader.toString())
              .append(") ");
            classLoader = classLoader.getParent();
        }

        // Bootstrap ClassLoader由C++实现，Java中无法获取，返回null
        sb.append("Bootstrap ClassLoader (由JVM内部实现) ");

        return sb.toString();
    }

    /**
     * 演示自定义类加载器
     */
    @GetMapping("/custom")
    public String customClassLoader() {
        try {
            // 创建自定义类加载器
            CustomClassLoader customLoader = new CustomClassLoader();

            // 加载String类（由于双亲委派机制，会由Bootstrap ClassLoader加载）
            Class<?> stringClass = customLoader.loadClass("java.lang.String");

            // 加载当前类（由于双亲委派机制，会由App ClassLoader加载）
            Class<?> currentClass = customLoader.loadClass("com.parallel.learn.jvm.demo.ClassLoaderDemo");

            return String.format(
                "自定义类加载器测试结果: " + "String类加载器: %s " + "ClassLoaderDemo类加载器: %s " +
                "两个类是否相同: %s",
                stringClass.getClassLoader(),
                currentClass.getClassLoader(),
                currentClass.equals(this.getClass())
            );
        } catch (ClassNotFoundException e) {
            log.error("类加载异常", e);
            return "类加载异常: " + e.getMessage();
        }
    }

    /**
     * 自定义类加载器
     */
    static class CustomClassLoader extends ClassLoader {
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // 对于java.开头的类，使用父类加载器加载
            if (name.startsWith("java.")) {
                return super.loadClass(name, resolve);
            }

            // 其他类使用自定义加载逻辑
            return findClass(name);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // 这里简化处理，实际应用中需要从文件系统或网络加载类字节码
            try {
                String path = name.replace('.', '/').concat(".class");
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);

                if (inputStream == null) {
                    return super.loadClass(name);
                }

                byte[] classBytes = readAllBytes(inputStream);
                return defineClass(name, classBytes, 0, classBytes.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        private byte[] readAllBytes(InputStream inputStream) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int bytesRead;
            byte[] data = new byte[4096];

            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        }
    }
}
