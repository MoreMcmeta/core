/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.moremcmeta.forge.impl.client.reflection;

import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Retrieves all classes with a particular annotation.
 * @author soir20
 */
public final class AnnotatedClassLoader {
    private final Supplier<Iterable<ModFileScanData.AnnotationData>> ANNOTATION_DATA_SUPPLIER;
    private final Logger LOGGER;

    /**
     * Creates a loader for annotated classes.
     * @param annotationDataSupplier    supplies current mod file scan data
     * @param logger                    logger to record errors loading classes
     */
    public AnnotatedClassLoader(Supplier<Iterable<ModFileScanData.AnnotationData>> annotationDataSupplier,
                                Logger logger) {
        ANNOTATION_DATA_SUPPLIER = requireNonNull(annotationDataSupplier, "Scan data supplier cannot be null");
        LOGGER = requireNonNull(logger, "Logger cannot be null");
    }

    /**
     * Loads classes with the given annotation and have the expected super class.
     * @param annotationClass       annotation that loaded classes must have
     * @param superClass            super class that loaded classes must have
     * @return all classes with the given annotation and super class that can be loaded
     * @param <T> type of the super class
     */
    public <T> Collection<T> load(Class<?> annotationClass, Class<T> superClass) {
        requireNonNull(annotationClass, "Annotation class cannot be null");
        requireNonNull(superClass, "Plugin class cannot be null");

        Type annotationType = Type.getType(annotationClass);
        Collection<T> instances = new ArrayList<>();
        Set<String> classNames = new HashSet<>();

        for (ModFileScanData.AnnotationData annotation : ANNOTATION_DATA_SUPPLIER.get()) {
            String className = annotation.memberName();
            if (annotation.annotationType().equals(annotationType) && !classNames.contains(className)) {
                classNames.add(className);
                tryLoadClass(className, superClass).ifPresent(instances::add);
            }
        }

        return instances;
    }

    /**
     * Attempts to load a class with the given name.
     * @param className         name of the class to load
     * @param superClass        super class that loaded classes must have
     * @return the loaded class or {@link Optional#empty()} if it could not be loaded
     * @param <T> type of the super class
     */
    private <T> Optional<T> tryLoadClass(String className, Class<T> superClass) {
        try {
            Class<?> clazz = Class.forName(className);
            Class<? extends T> superClassInstance = clazz.asSubclass(superClass);
            Constructor<? extends T> constructor = superClassInstance.getDeclaredConstructor();
            return Optional.of(constructor.newInstance());
        } catch (ClassCastException | ReflectiveOperationException | LinkageError err) {
            LOGGER.error("Class {} failed to load. It might not be a subclass of {}, " +
                    "or it might not have a default constructor: {}", className, superClass, err);
        }

        return Optional.empty();
    }

}
