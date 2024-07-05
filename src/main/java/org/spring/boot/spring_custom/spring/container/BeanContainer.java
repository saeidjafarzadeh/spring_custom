package org.spring.boot.spring_custom.spring.container;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lalyos.jfiglet.FigletFont;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import org.spring.boot.spring_custom.spring.annotations.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


public class BeanContainer {
    private static Log logger = LogFactory.getLog( "");
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final Map<String, Method> getMappings = new HashMap<>();
    private final Map<String, Method> postMappings = new HashMap<>();
    private final Map<String, Method> putMappings = new HashMap<>();
    private final Map<String, Method> deleteMappings = new HashMap<>();
    private String basePackage;
    private ObjectMapper objectMapper;

    private static long count = 0;

    public BeanContainer(String basePackage) {
        String naji_nrdc = null;
        try {
            naji_nrdc = FigletFont.convertOneLine("NRDC NAJI COMPANI");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(naji_nrdc);
        System.out.println("-----------------------------------------------------------------------powered by nrdc naji----------------------------------------------------------------");

        logger.info("...........................................................start scan all of bean on project enjoy this..................................................");
        objectMapper = new ObjectMapper();
        this.basePackage = basePackage;
        scanPackage(basePackage);
        logger.info("...............................................................all of bean finded successfully lets go...................................................");
        logger.info(".............................................................start inject all of bean eager on project enjoy this........................................");

        injectDependencies();
        logger.info("...............................................................all of inject successfully lets go........................................................");
        logger.info("............................................................start check and find all endpoint in project.................................................");

        mapEndPoints();
        logger.info(".............................................................all of endpoint successfully lets go........................................................");

    }

    private void scanPath(File directory) {
        try {

            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (count == 0) {
                    for (File files : file.listFiles()) {

                        if (files.getName().endsWith(".class")) {

                            String className = basePackage + "/" + file.getName() + '.' + files.getName().replace(".class", "");
                            Class<?> clazz = Class.forName(className.replace('/', '.'));
                            if (clazz.isAnnotationPresent(Component.class) || clazz.isAnnotationPresent(Service.class) || clazz.isAnnotationPresent(Repository.class) || clazz.isAnnotationPresent(Configuration.class) || clazz.isAnnotationPresent(Bean.class) || clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
                                if (clazz.isAnnotationPresent(Qualifier.class)) {
                                    Object instance = clazz.getDeclaredConstructor().newInstance();
                                    Qualifier qualifier = clazz.getAnnotation(Qualifier.class);
                                    String qualifierName = qualifier != null ? qualifier.value() : "";

                                    Scope scope = clazz.getAnnotation(Scope.class);
                                    if (scope == null || scope.value().equals("singleton")) {
                                        if (clazz.isAnnotationPresent(Lazzy.class)){
                                            beans.computeIfAbsent(clazz, k -> new HashMap<>().put(qualifierName, null));
                                        }else {
                                            invokePostConstruct(instance);
                                            beans.computeIfAbsent(clazz, k -> new HashMap<>().put(qualifierName, instance));
                                        }
                                    } else if (scope != null && scope.value().equals("prototype")) {
                                        beans.computeIfAbsent(clazz, k -> new HashMap<>().put(qualifierName, null));
                                    }
                                }

                                Scope scope = clazz.getAnnotation(Scope.class);
                                if (scope == null || scope.value().equals("singleton")) {
                                    if (clazz.isAnnotationPresent(Lazzy.class)){
                                        Object instance = clazz.getDeclaredConstructor().newInstance();
                                        invokePostConstruct(instance);
                                        beans.put(clazz, null);
                                    }else {
                                        Object instance = clazz.getDeclaredConstructor().newInstance();
                                        invokePostConstruct(instance);
                                        beans.put(clazz, instance);
                                    }
                                } else if (scope != null && scope.value().equals("prototype")) {
                                    beans.put(clazz, null);
                                }
                            }
                        } else {
                            count++;
                            scanPath(files);
                        }


                    }
                } else {

                    if (file.getName().endsWith(".class")) {
                        String[] split = file.getPath().split("target.classes.", 2);
                        String replace = split[1].replace(".class", "");
                        Class<?> clazz = Class.forName(replace.replace('\\', '.'));
                        if (clazz.isAnnotationPresent(Component.class) || clazz.isAnnotationPresent(Service.class) || clazz.isAnnotationPresent(Repository.class) || clazz.isAnnotationPresent(Configuration.class) || clazz.isAnnotationPresent(Bean.class) || clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
                            if (clazz.isAnnotationPresent(Qualifier.class)) {
                                Object instance = clazz.getDeclaredConstructor().newInstance();
                                Qualifier qualifier = clazz.getAnnotation(Qualifier.class);
                                String qualifierName = qualifier != null ? qualifier.value() : "";

                                Scope scope = clazz.getAnnotation(Scope.class);
                                if (scope == null || scope.value().equals("singleton")) {
                                    invokePostConstruct(instance);
                                    beans.computeIfAbsent(clazz, k -> new HashMap<>().put(qualifierName, instance));
                                } else if (scope != null && scope.value().equals("prototype")) {
                                    beans.computeIfAbsent(clazz, k -> new HashMap<>().put(qualifierName, null));
                                }
                            }

                            Scope scope = clazz.getAnnotation(Scope.class);
                            if (scope == null || scope.value().equals("singleton")) {
                                Object instance = clazz.getDeclaredConstructor().newInstance();
                                invokePostConstruct(instance);
                                beans.put(clazz, instance);
                            } else if (scope != null && scope.value().equals("prototype")) {
                                beans.put(clazz, null);
                            }
                        }
                    } else {
                        count++;
                        scanPath(file);
                    }

                }


            }
        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

    private void scanPackage(String basePackage) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = basePackage.replace('.', '/');
            URL resource = classLoader.getResource(path);
            assert resource != null;
            File directory = new File(resource.getFile());
            scanPath(directory);

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }


    private void injectDependencies() {

        for (Object bean : beans.values()) {
            Field[] fields = bean.getClass().getDeclaredFields();
            for (Field field : fields) {

                if (field.isAnnotationPresent(Autowired.class)) {
                    if (field.isAnnotationPresent(Qualifier.class)) {
                        Qualifier qualifier = field.getAnnotation(Qualifier.class);
                        String qualifierName = qualifier != null ? qualifier.value() : "";
                        Object dependency = beans.get(qualifierName);
                        if (dependency != null) {
                            field.setAccessible(true);
                            try {
                                field.set(bean, dependency);
                            } catch (Exception e) {
                                throw new RuntimeException();
                            }
                        }

                    } else {
                        Object dependency = beans.get(field.getType());
                        if (dependency != null) {
                            field.setAccessible(true);
                            try {
                                field.set(bean, dependency);
                            } catch (Exception e) {
                                throw new RuntimeException();
                            }
                        }
                    }

                }

            }
            Constructor<?>[] constructors = bean.getClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    Object[] parameters = Arrays.stream(constructor.getParameterTypes()).map(beans::get).toArray();
                    try {
                        Object newInstance = constructor.newInstance(parameters);
                        beans.put(bean.getClass(), newInstance);
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }

                }
            }
        }

    }

    private void injectDependencies(Object bean) {

        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = beans.get(field.getType());
                if (dependency != null) {
                    field.setAccessible(true);
                    try {
                        field.set(bean, dependency);
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                }
            }

        }
    }


    public <T> T getBean(Class<T> clazz) {
        T bean = clazz.cast(beans.get(clazz));
        if (bean == null) {
            try {
                bean = clazz.getDeclaredConstructor().newInstance();
                injectDependencies(bean);
                invokePostConstruct(bean);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

        return bean;
    }


    private void invokePostConstruct(Object bean) {

        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(bean);
                } catch (Exception e) {
                    throw new RuntimeException();
                }

            }

        }
    }


    private void invokePreDestroy(Object bean) {

        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(bean);
                } catch (Exception e) {
                    throw new RuntimeException();
                }

            }

        }
    }

    public void destroy() {
        for (Object bean : beans.values()) {
            invokePreDestroy(bean);
        }
    }


    private void mapEndPoints() {
        for (Object bean : beans.values()) {
            if (bean.getClass().isAnnotationPresent(RestController.class)) {
                String basePath = "";
                if (bean.getClass().isAnnotationPresent(RequestMapping.class)) {
                    basePath = bean.getClass().getAnnotation(RequestMapping.class).value();
                }
                for (Method method : bean.getClass().getDeclaredMethods()) {

                    if (method.isAnnotationPresent(GetMapping.class)) {
                        String path = basePath + method.getAnnotation(GetMapping.class).value();
                        getMappings.put(path, method);
                    } else if (method.isAnnotationPresent(PostMapping.class)) {
                        String path = basePath + method.getAnnotation(PostMapping.class).value();
                        postMappings.put(path, method);


                    } else if (method.isAnnotationPresent(PutMapping.class)) {
                        String path = basePath + method.getAnnotation(PutMapping.class).value();
                        putMappings.put(path, method);
                    } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                        String path = basePath + method.getAnnotation(DeleteMapping.class).value();
                        deleteMappings.put(path, method);
                    }
                }
            }
        }
    }

    public Method getEndPointMethod(String path, String method) {
        switch (method) {
            case "GET":
                return getMappings.get(path);
            case "POST":
                return postMappings.get(path);
            case "PUT":
                return putMappings.get(path);
            case "DELETE":
                deleteMappings.get(path);
            default:
                return null;
        }
    }


    public Object[] getMethodParameters(HttpExchange exchange, Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];
        for (int i = 0; i < parameterValues.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> queryParameters = Arrays.stream(query.split("&")).map(s -> s.split("=")).collect(Collectors.toMap(strings -> strings[0], strings -> strings[1]));
                parameterValues[i] = queryParameters.get(requestParam.value());
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {

                InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String body = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));

                try {
                    parameterValues[i] = body;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }
        return parameterValues;

    }

}
