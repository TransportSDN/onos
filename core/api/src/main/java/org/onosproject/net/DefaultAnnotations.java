/*
 * Copyright 2014-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.net;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a set of simple annotations that can be used to add arbitrary
 * attributes to various parts of the data model.
 */
public final class DefaultAnnotations implements SparseAnnotations {

    public static final SparseAnnotations EMPTY = DefaultAnnotations.builder().build();

    private final Map<String, ?> map;

    // For serialization
    private DefaultAnnotations() {
        this.map = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultAnnotations that = (DefaultAnnotations) o;

        return Objects.equals(this.map, that.map);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.map);
    }

    /**
     * Returns the annotations as a map.
     *
     * @return a copy of the contents of the annotations as a map.
     */
    public HashMap<String, String> asMap() {
        HashMap<String, String> tempMap = new HashMap<>();
        this.map.keySet().forEach(key-> {
            Object o = this.map.get(key);
            if (!(o instanceof String)) {
                throw new UnsupportedOperationException("Only string value is supported");
            }
            tempMap.put(key, (String) this.map.get(key));
        });
        return Maps.newHashMap(tempMap);
    }

    /**
     * Creates a new set of annotations using clone of the specified hash map.
     *
     * @param map hash map of key/value pairs
     */
    private DefaultAnnotations(Map<String, ?> map) {
        this.map = map;
    }

    /**
     * Creates a new annotations builder.
     *
     * @return new annotations builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Merges the specified base set of annotations and additional sparse
     * annotations into new combined annotations. If the supplied sparse
     * annotations are empty, the original base annotations are returned.
     * Any keys tagged for removal in the sparse annotations will be omitted
     * in the resulting merged annotations.
     *
     * @param annotations       base annotations
     * @param sparseAnnotations additional sparse annotations
     * @return combined annotations or the original base annotations if there
     * are not additional annotations
     */
    public static DefaultAnnotations merge(DefaultAnnotations annotations,
                                           SparseAnnotations sparseAnnotations) {
        checkNotNull(annotations, "Annotations cannot be null");
        if (sparseAnnotations == null || sparseAnnotations.keys().isEmpty()) {
            return annotations;
        }

        // Merge the two maps. Yes, this is not very efficient, but the
        // use-case implies small maps and infrequent merges, so we opt for
        // simplicity.
        Map<String, Object> merged = copy(annotations.map);
        for (String key : sparseAnnotations.keys()) {
            if (sparseAnnotations.isRemoved(key)) {
                merged.remove(key);
            } else {
                merged.put(key, sparseAnnotations.value(key));
            }
        }
        return new DefaultAnnotations(merged);
    }

    /**
     * Creates the union of two given SparseAnnotations.
     * Unlike the {@link #merge(DefaultAnnotations, SparseAnnotations)} method,
     * result will be {@link SparseAnnotations} instead of {@link Annotations}.
     * <p>
     * A key tagged for removal will remain in the output SparseAnnotations,
     * if the counterpart of the input does not contain the same key.
     *
     * @param annotations       base annotations
     * @param sparseAnnotations additional sparse annotations
     * @return combined annotations or the original base annotations if there
     * are not additional annotations
     */
    public static SparseAnnotations union(SparseAnnotations annotations,
                                          SparseAnnotations sparseAnnotations) {

        if (sparseAnnotations == null || sparseAnnotations.keys().isEmpty()) {
            return annotations;
        }

        final HashMap<String, Object> newMap;
        if (annotations instanceof DefaultAnnotations) {
            newMap = copy(((DefaultAnnotations) annotations).map);
        } else {
            newMap = new HashMap<>(annotations.keys().size() +
                                           sparseAnnotations.keys().size());
            putAllSparseAnnotations(newMap, annotations);
        }

        putAllSparseAnnotations(newMap, sparseAnnotations);
        return new DefaultAnnotations(newMap);
    }

    // adds the key-values contained in sparseAnnotations to
    // newMap, if sparseAnnotations had a key tagged for removal,
    // and corresponding key exist in newMap, entry will be removed.
    // if corresponding key does not exist, removal tag will be added to
    // the newMap.
    private static void putAllSparseAnnotations(
            final HashMap<String, Object> newMap,
            SparseAnnotations sparseAnnotations) {

        for (String key : sparseAnnotations.keys()) {
            if (sparseAnnotations.isRemoved(key)) {
                if (newMap.containsKey(key)) {
                    newMap.remove(key);
                } else {
                    newMap.put(key, Builder.REMOVED);
                }
            } else {
                String value = sparseAnnotations.value(key);
                newMap.put(key, value);
            }
        }
    }

    @Override
    public Set<String> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public String value(String key) {
        Object o = map.get(key);
        if (o == null) {
            return null;
        }

        if (o instanceof String) {
            String value = (String) o;
            return Objects.equals(Builder.REMOVED, value) ? null : value;
        }
        throw new IllegalArgumentException("Expecting String type");
    }

    @Override
    public <T> T value(String key, Class<T> typeClass) {
        Object o = map.get(key);
        if (typeClass.isInstance(o)) {
            return (T) o;
        }
        throw new IllegalArgumentException("Expecting specific instance");
    }

    @Override
    public <T> List<T> values(String key, Class<T> typeClass) {
        Object o = map.get(key);
        if (o instanceof List) {
        List<?> values = (List<?>) o;
            if (typeClass.isInstance(values.get(0))) {
                return (List<T>) values;
            }
            throw new IllegalArgumentException("Expecting specific instance");
        }

        throw new IllegalArgumentException("Expecting List instance");
    }

    @Override
    public boolean isRemoved(String key) {
        return Objects.equals(Builder.REMOVED, map.get(key));
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, Object> copy(Map<String, ?> original) {
        if (original instanceof HashMap) {
            return (HashMap<String, Object>) ((HashMap<?, ?>) original).clone();
        }
        throw new IllegalArgumentException("Expecting HashMap instance");
    }

    @Override
    public String toString() {
        return (map == null) ? "null" : map.toString();
    }

    /**
     * Facility for gradually building model annotations.
     */
    public static final class Builder {

        private static final String REMOVED = "~rEmOvEd~";
        private final Map<String, Object> builder = new HashMap<>();

        // Private construction is forbidden.
        private Builder() {
        }

        /**
         * Adds all specified annotation. Any previous value associated with
         * the given annotations will be overwritten.
         *
         * @param base annotations
         * @return self
         */
        public Builder putAll(Annotations base) {
            if (base instanceof DefaultAnnotations) {
                builder.putAll(((DefaultAnnotations) base).map);

            } else if (base instanceof SparseAnnotations) {
                final SparseAnnotations sparse = (SparseAnnotations) base;
                for (String key : base.keys()) {
                    if (sparse.isRemoved(key)) {
                        remove(key);
                    } else {
                        set(key, base.value(key));
                    }
                }

            } else {
                base.keys().forEach(key -> set(key, base.value(key)));

            }
            return this;
        }

        /**
         * Adds the specified annotation. Any previous value associated with
         * the given annotation key will be overwritten.
         *
         * @param key   annotation key
         * @param value annotation value
         * @return self
         */
        public Builder set(String key, String value) {
            builder.put(key, value);
            return this;
        }

        /**
         * Adds the specified annotation, the client is unaware of the specific
         * type of the value. Any previous value associated with the given
         * annotation key will be overwritten.
         *
         * @param key     annotation key
         * @param object  annotation value
         * @return self
         */
        public Builder set(String key, Object object) {
            builder.put(key, object);
            return this;
        }

        /**
         * Adds the specified annotation. Any previous value associated with
         * the given annotation key will be tagged for removal.
         *
         * @param key annotation key
         * @return self
         */
        public Builder remove(String key) {
            builder.put(key, REMOVED);
            return this;
        }

        /**
         * Returns immutable annotations built from the accrued key/values pairs.
         *
         * @return annotations
         */
        public DefaultAnnotations build() {
            return new DefaultAnnotations(copy(builder));
        }
    }
}
