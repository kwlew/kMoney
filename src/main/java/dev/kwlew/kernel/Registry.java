// dev/kwlew/kernel/Registry.java
package dev.kwlew.kernel;

import dev.kwlew.managers.exceptions.CircularDependencyException;
import dev.kwlew.managers.exceptions.UnresolvedDependencyException;

import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Registry {

    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final ThreadLocal<Deque<Class<?>>> resolutionStack = ThreadLocal.withInitial(ArrayDeque::new);

    public <T> void register(Class<T> type, T instance) {
        instances.put(type, instance);
    }

    public <T> T resolve(Class<T> type) {
        Object existing = instances.get(type);
        if (existing != null) {
            return type.cast(existing);
        }

        if (NotConstructable(type)) {
            throw new RuntimeException("No registered instance for non-constructable type " + type.getName());
        }

        Deque<Class<?>> stack = resolutionStack.get();
        if (stack.contains(type)) {
            throw new CircularDependencyException(formatCycle(stack, type));
        }

        try {
            stack.addLast(type);
            Constructor<?> constructor = selectConstructor(type);
            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(this::resolveDependency)
                    .toArray();

            constructor.setAccessible(true);

            T instance = type.cast(constructor.newInstance(params));
            instances.put(type, instance);

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + type.getName(), e);
        } finally {
            if (!stack.isEmpty() && stack.peekLast() == type) {
                stack.removeLast();
            }

            if (stack.isEmpty()) {
                resolutionStack.remove();
            }
        }
    }

    private Constructor<?> selectConstructor(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        if (constructors.length == 0) {
            throw new RuntimeException("No constructor found for " + type.getName());
        }

        Constructor<?> injectConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (!constructor.isAnnotationPresent(Inject.class)) {
                continue;
            }

            if (injectConstructor != null) {
                throw new RuntimeException("Multiple @Inject constructors found for " + type.getName());
            }

            injectConstructor = constructor;
        }

        if (injectConstructor != null) {
            return injectConstructor;
        }

        return Arrays.stream(constructors)
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new RuntimeException("No constructor found for " + type.getName()));
    }

    public Collection<Object> getAll() {
        return Collections.unmodifiableCollection(instances.values());
    }

    private Object resolveDependency(Class<?> dependencyType) {
        Object existing = instances.get(dependencyType);
        if (existing != null) {
            return existing;
        }

        if (NotConstructable(dependencyType)) {
            throw new UnresolvedDependencyException(dependencyType);
        }

        return resolve(dependencyType);
    }

    private boolean NotConstructable(Class<?> type) {
        return type.isInterface() || Modifier.isAbstract(type.getModifiers());
    }

    public <T> void bind(Class<T> abstraction, Class<? extends T> implementation) {
        instances.put(abstraction, resolve(implementation));
    }

    private String formatCycle(Deque<Class<?>> stack, Class<?> repeatedType) {
        StringBuilder cycle = new StringBuilder();
        boolean append = false;

        for (Class<?> type : stack) {
            if (type == repeatedType) {
                append = true;
            }

            if (append) {
                if (!cycle.isEmpty()) {
                    cycle.append(" -> ");
                }
                cycle.append(type.getSimpleName());
            }
        }

        if (!cycle.isEmpty()) {
            cycle.append(" -> ");
        }
        cycle.append(repeatedType.getSimpleName());

        return cycle.toString();
    }
}
