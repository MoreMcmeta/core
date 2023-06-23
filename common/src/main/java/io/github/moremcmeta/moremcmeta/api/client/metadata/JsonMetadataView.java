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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import com.google.common.primitives.Ints;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * {@link MetadataView} implementation with an underlying JSON format. This view class is provided because
 * it has been extensively tested, and other formats may be translated into JSON.
 * @author soir20
 * @since 4.0.0
 */
public final class JsonMetadataView implements MetadataView {
    private final Root ROOT;
    private final List<String> KEYS;
    private final int SIZE;

    /**
     * Creates a new metadata view with a JSON object as the root.
     * @param root              the root JSON object
     * @param keyComparator     comparator to order keys by index
     */
    public JsonMetadataView(JsonObject root, Comparator<? super String> keyComparator) {
        ROOT = new Root(requireNonNull(root, "Root cannot be null"));
        requireNonNull(keyComparator, "Key comparator cannot be null");
        KEYS = root.entrySet().stream()
                .map(Map.Entry::getKey)
                .filter((key) -> !root.get(key).isJsonNull()).sorted(keyComparator).toList();
        SIZE = KEYS.size();
    }

    /**
     * Creates a new metadata view with a JSON array as the root.
     * @param root              the root JSON array
     */
    public JsonMetadataView(JsonArray root) {
        ROOT = new Root(requireNonNull(root, "Array cannot be null"));

        // Remove all null elements
        for (int index = root.size() - 1; index >= 0; index--) {
            if (root.get(index).isJsonNull()) {
                root.remove(index);
            }
        }

        SIZE = root.size();
        KEYS = IntStream.range(0, SIZE).mapToObj(String::valueOf).toList();
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public Iterable<String> keys() {

        // The list is created to be unmodifiable
        return KEYS;

    }

    @Override
    public boolean hasKey(String key) {
        requireNonNull(key, "Key cannot be null");

        int keyAsIndex = strAsIndex(key);
        return ROOT.get(
                (obj) -> obj.has(key) && !obj.get(key).isJsonNull(),
                (array) -> keyAsIndex >= 0 && keyAsIndex < SIZE
        );
    }

    @Override
    public boolean hasKey(int index) {
        if (index < 0) {
            throw new NegativeKeyIndexException(index);
        }

        return index < SIZE;
    }

    @Override
    public Optional<String> stringValue(String key) {

        // The Gson implementation can convert any primitive to a string, even if isString() is false
        return primitiveFromKey(key, (element) -> true, JsonPrimitive::getAsString);

    }

    @Override
    public Optional<String> stringValue(int index) {

        // The Gson implementation can convert any primitive to a string, even if isString() is false
        return primitiveFromIndex(index, (element) -> true, JsonPrimitive::getAsString);

    }

    @Override
    public Optional<Integer> integerValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && isSignedInteger(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsInt
        );
    }

    @Override
    public Optional<Integer> integerValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && isSignedInteger(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsInt
        );
    }

    @Override
    public Optional<Long> longValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && isSignedLong(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsLong
        );
    }

    @Override
    public Optional<Long> longValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && isSignedLong(primitive.getAsBigDecimal()),
                JsonPrimitive::getAsLong
        );
    }

    @Override
    public Optional<Float> floatValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && Float.isFinite(primitive.getAsBigDecimal().floatValue()),
                JsonPrimitive::getAsFloat
        );
    }

    @Override
    public Optional<Float> floatValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && Float.isFinite(primitive.getAsBigDecimal().floatValue()),
                JsonPrimitive::getAsFloat
        );
    }

    @Override
    public Optional<Double> doubleValue(String key) {
        return primitiveFromKey(key,
                (primitive) -> primitive.isNumber() && Double.isFinite(primitive.getAsBigDecimal().doubleValue()),
                JsonPrimitive::getAsDouble
        );
    }

    @Override
    public Optional<Double> doubleValue(int index) {
        return primitiveFromIndex(index,
                (primitive) -> primitive.isNumber() && Double.isFinite(primitive.getAsBigDecimal().doubleValue()),
                JsonPrimitive::getAsDouble
        );
    }

    @Override
    public Optional<Boolean> booleanValue(String key) {

        // The Gson implementation can convert any primitive to a boolean, even if isString() is false
        return primitiveFromKey(key, (element) -> true, JsonPrimitive::getAsBoolean);

    }

    @Override
    public Optional<Boolean> booleanValue(int index) {

        // The Gson implementation can convert any primitive to a boolean, even if isString() is false
        return primitiveFromIndex(index, (element) -> true, JsonPrimitive::getAsBoolean);

    }

    @Override
    public Optional<InputStream> byteStreamValue(String key) {

        // Currently, the JSON format does not need to support streams
        return Optional.empty();

    }

    @Override
    public Optional<InputStream> byteStreamValue(int index) {
        if (index < 0) {
            throw new NegativeKeyIndexException(index);
        }

        // Currently, the JSON format does not need to support streams
        return Optional.empty();

    }

    @Override
    public Optional<MetadataView> subView(String key) {
        return ROOT.get(
                (obj) -> {
                    if (!obj.has(key)) {
                        return Optional.empty();
                    }

                    return convertToSubView(obj.get(key));
                }, (array) -> {
                    int keyAsIndex = strAsIndex(key);

                    // Do not use hasKey(index) here as a negative index should not raise an exception
                    if (keyAsIndex < 0 || keyAsIndex >= SIZE) {
                        return Optional.empty();
                    }

                    return convertToSubView(array.get(keyAsIndex));
                }
        );
    }

    @Override
    public Optional<MetadataView> subView(int index) {
        if (!hasKey(index)) {
            return Optional.empty();
        }

        return ROOT.get(
                (obj) -> convertToSubView(objectElementByIndex(obj, index)),
                (array) -> convertToSubView(array.get(index))
        );
    }

    /**
     * Convert a string to an integer index.
     * @param str       the string to convert
     * @return the string as an integer or -1 if the string does not represent an integer
     */
    private int strAsIndex(String str) {

        @SuppressWarnings("UnstableApiUsage")
        int index = Optional.of(str).map(Ints::tryParse).orElse(-1);

        return index;
    }

    /**
     * Retrieves the primitive at the given key if it satisfies the requirement of the `checkFunction`.
     * @param key                   the key whose value to retrieve
     * @param checkFunction         checks whether the primitive is a value of the required type
     * @param convertFunction       converts the primitive to the value of the required type if it
     *                              passes the `checkFunction`
     * @return the value at the key if it exists, is a primitive, and satisfies the `checkFunction`
     * @param <T> the type of primitive to retrieve
     */
    private <T> Optional<T> primitiveFromKey(String key, Function<JsonPrimitive, Boolean> checkFunction,
                                             Function<JsonPrimitive, T> convertFunction) {
        requireNonNull(key, "Key cannot be null");

        if (!hasKey(key)) {
            return Optional.empty();
        }

        int keyAsIndex = strAsIndex(key);
        return ROOT.get(
                (obj) -> primitiveOfType(obj.get(key), checkFunction, convertFunction),
                (array) ->  primitiveOfType(array.get(keyAsIndex), checkFunction, convertFunction)
        );
    }

    /**
     * Retrieves the primitive at the given index if it satisfies the requirement of the `checkFunction`.
     * @param index                 the index whose value to retrieve
     * @param checkFunction         checks whether the primitive is a value of the required type
     * @param convertFunction       converts the primitive to the value of the required type if it
     *                              passes the `checkFunction`
     * @return the value at the index if it exists, is a primitive, and satisfies the `checkFunction`
     * @param <T> the type to convert the primitive to
     */
    private <T> Optional<T> primitiveFromIndex(int index, Function<JsonPrimitive, Boolean> checkFunction,
                                               Function<JsonPrimitive, T> convertFunction) {
        if (!hasKey(index)) {
            return Optional.empty();
        }

        return ROOT.get(
                (obj) -> primitiveOfType(objectElementByIndex(obj, index), checkFunction, convertFunction),
                (array) ->  primitiveOfType(array.get(index), checkFunction, convertFunction)
        );
    }

    /**
     * Converts a JSON element to a value of the desired type if it is a primitive and  passes the `checkFunction`.
     * @param element               element to convert
     * @param checkFunction         checks whether the primitive is a value of the required type
     * @param convertFunction       converts the primitive to the value of the required type if it
     *                              passes the `checkFunction`
     * @return the value if it satisfies the `checkFunction` or {@link Optional#empty()}
     * @param <T> the type to convert the primitive to
     */
    private <T> Optional<T> primitiveOfType(JsonElement element, Function<JsonPrimitive, Boolean> checkFunction,
                                            Function<JsonPrimitive, T> convertFunction) {
        if (!element.isJsonPrimitive()) {
            return Optional.empty();
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!checkFunction.apply(primitive)) {
            return Optional.empty();
        }

        return Optional.of(convertFunction.apply(primitive));
    }

    /**
     * Gets an element in a JSON object by index.
     * @param obj       the object to access by index
     * @param index     the index of the key to access
     * @return the item at that key or `null` if there is no item at the given index
     */
    private JsonElement objectElementByIndex(JsonObject obj, int index) {
        return obj.get(KEYS.get(index));
    }

    /**
     * Checks if a number is a signed 32-bit integer.
     * @param num       number to check
     * @return true if the number is a signed 32-bit integer, false otherwise
     */
    private boolean isSignedInteger(BigDecimal num) {
        return num.stripTrailingZeros().scale() <= 0
                && isBetweenInclusive(num, BigDecimal.valueOf(Integer.MIN_VALUE), BigDecimal.valueOf(Integer.MAX_VALUE));
    }

    /**
     * Checks if a number is a signed 64-bit long.
     * @param num       number to check
     * @return true if the number is a signed 64-bit long, false otherwise
     */
    private boolean isSignedLong(BigDecimal num) {
        return num.stripTrailingZeros().scale() <= 0
                && isBetweenInclusive(num, BigDecimal.valueOf(Long.MIN_VALUE), BigDecimal.valueOf(Long.MAX_VALUE));
    }

    /**
     * Checks if a value is in between a lower value (inclusive) and an upper value (inclusive).
     * @param value     the value to check
     * @param lower     the lower bound to compare the value to
     * @param upper     the upper bound to compare the value to
     * @return true if the value is between the lower bound and upper bound, inclusive
     * @param <T> the type of the values to compare
     */
    private <T extends Comparable<? super T>> boolean isBetweenInclusive(T value, T lower, T upper) {
        return value.compareTo(lower) >= 0 && value.compareTo(upper) <= 0;
    }

    /**
     * Converts a JSON object or a JSON array to a {@link MetadataView}.
     * @param element       the element to convert
     * @return a metadata view for the element or {@link Optional#empty()} if the element is neither
     *         an object nor an array
     */
    private Optional<MetadataView> convertToSubView(JsonElement element) {
        if (element.isJsonObject()) {

            // We only want to use a special comparator for the root of the JSON tree
            return Optional.of(new JsonMetadataView(element.getAsJsonObject(), String::compareTo));

        }

        if (element.isJsonArray()) {
            return Optional.of(new JsonMetadataView(element.getAsJsonArray()));
        }

        return Optional.empty();
    }

    /**
     * Union of the {@link JsonObject} and {@link JsonArray} types so that they can be
     * accessed similarly.
     * @author soir20
     */
    private static class Root {
        private final JsonObject OBJECT;
        private final JsonArray ARRAY;

        /**
         * Creates a new root based on an object.
         * @param object        the object that is the root
         */
        public Root(JsonObject object) {
            OBJECT = object;
            ARRAY = null;
        }

        /**
         * Creates a new root based on an array.
         * @param array         the array that is the root
         */
        public Root(JsonArray array) {
            OBJECT = null;
            ARRAY = array;
        }

        /**
         * Applies a function to the object root or the array root, depending on which type this root is.
         * @param objectFunction        the function to apply if this root holds an object
         * @param arrayFunction         the function to apply if this root holds an array
         * @return the return value of whichever function was applies
         * @param <T> the type of value to retrieve
         */
        public <T> T get(Function<JsonObject, T> objectFunction, Function<JsonArray, T> arrayFunction) {
            if (OBJECT != null) {
                return objectFunction.apply(OBJECT);
            }

            if (ARRAY != null) {
                return arrayFunction.apply(ARRAY);
            }

            throw new IllegalStateException("Either object or array must be present");
        }

    }
}