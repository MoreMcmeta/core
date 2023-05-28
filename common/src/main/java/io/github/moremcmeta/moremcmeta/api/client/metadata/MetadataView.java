/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

import java.io.InputStream;
import java.util.Optional;

/**
 * <p>Provides immutable access to texture metadata. A view acts like an object and an array. As an object,
 * the view provides direct access to keys by name. As an array, the view's keys are ordered based on
 * highest section priority (at the topmost level) or lexicographically (at all lower levels) and can be
 * accessed by index.</p>
 *
 * <p>No underlying file format or file location for this metadata is guaranteed. Arrays in the underlying
 * format are treated as views with the keys "0", "1", etc. for each item in the array.</p>
 *
 * <p>All index-based methods throw {@link NegativeKeyIndexException} if the provided index is negative. It
 * is always an error to access a key with a negative index. In contrast, these methods return
 * {@link Optional#empty()} if a positive index out of bounds is provided, as a positive index would be
 * valid under some circumstances, even though a key at that index does not exist for this particular view.
 * Similarly, the string-based methods return {@link Optional#empty()} for keys that could exist but
 * don't in this particular view.</p>
 * @author soir20
 * @since 4.0.0
 */
public interface MetadataView {

    /**
     * Gets the number of top-level keys in this view.
     * @return number of top-level keys
     */
    int size();

    /**
     * Gets all the top-level keys in this view. The iteration order of keys determines the order in which
     * MoreMcmeta applies plugins. Minimally, it should always return a consistent order for a given view.
     * It is advisable to implement some way for users to control the ordering of keys.
     * @return all the top-level keys in this view
     */
    Iterable<String> keys();

    /**
     * Checks if this view has a data value or sub-view for a top-level key with the
     * given name.
     * @param key       the key to check for
     * @return true if the view has a data value or sub-view for this key or false otherwise
     */
    boolean hasKey(String key);

    /**
     * Checks if this view has a data value or sub-view for a top-level key at the
     * given index (starting at 0). Returns false for any positive index out of bounds.
     * @param index       the index of the key to check for
     * @return true if the view has a data value or sub-view for this key or false otherwise
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    boolean hasKey(int index);

    /**
     * <p>Retrieves the value of a string for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid string, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a string.</p>
     *
     * <p>Any value that is not a sub-view can be converted to a string, so this method will return a
     * string whenever {@link #hasKey(String)} returns true.</p>
     * @param key       the key whose string value to retrieve
     * @return An {@link Optional} containing the string value or {@link Optional#empty()} if there is
     *         string value associated with the key. The string inside the {@link Optional} will never
     *         be null.
     */
    Optional<String> stringValue(String key);

    /**
     * <p>Retrieves the value of a string for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid string, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a string.</p>
     *
     * <p>Any value that is not a sub-view can be converted to a string, so this method will return a
     * string whenever {@link #hasKey(int)} returns true.</p>
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the string value or {@link Optional#empty()} if there is
     *         string value associated with the key. The string inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    Optional<String> stringValue(int index);

    /**
     * Retrieves the value of a signed 32-bit integer for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid signed 32-bit integer, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not an integer.
     * @param key       the key whose signed 32-bit integer value to retrieve
     * @return An {@link Optional} containing the integer value or {@link Optional#empty()} if there is
     *         integer value associated with the key. The integer inside the {@link Optional} will never
     *         be null.
     */
    Optional<Integer> integerValue(String key);

    /**
     * Retrieves the value of a signed 32-bit integer for the key at the given index if any exists. If there is no
     * such key or the key's associated value is not a valid signed 32-bit integer, this method returns
     * {@link Optional#empty()}. {@link #hasKey(int)} can be used to determine whether the key is present or
     * the value is not an integer.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the integer value or {@link Optional#empty()} if there is
     *         integer value associated with the key. The integer inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    Optional<Integer> integerValue(int index);

    /**
     * Retrieves the value of a signed 64-bit long for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid signed 64-bit long, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a long.
     * @param key       the key whose signed 64-bit long value to retrieve
     * @return An {@link Optional} containing the long value or {@link Optional#empty()} if there is
     *         long value associated with the key. The long inside the {@link Optional} will never
     *         be null.
     */
    Optional<Long> longValue(String key);

    /**
     * Retrieves the value of a signed 64-bit long for the key at the given index if any exists. If there is
     * no such key or the key's associated value is not a valid signed 64-bit long, this method returns
     * {@link Optional#empty()}. {@link #hasKey(int)} can be used to determine whether the key is present
     * or the value is not a long.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the long value or {@link Optional#empty()} if there is
     *         long value associated with the key. The long inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    Optional<Long> longValue(int index);

    /**
     * Retrieves the value of a float for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid float, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a float.
     * @param key       the key whose float value to retrieve
     * @return An {@link Optional} containing the float value or {@link Optional#empty()} if there is
     *         float value associated with the key. The float inside the {@link Optional} will never
     *         be null.
     */
    Optional<Float> floatValue(String key);

    /**
     * Retrieves the value of a float for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid float, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a float.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the float value or {@link Optional#empty()} if there is
     *         float value associated with the key. The float inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    Optional<Float> floatValue(int index);

    /**
     * Retrieves the value of a double for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid double, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a double.
     * @param key       the key whose double value to retrieve
     * @return An {@link Optional} containing the double value or {@link Optional#empty()} if there is
     *         double value associated with the key. The double inside the {@link Optional} will never
     *         be null.
     */
    Optional<Double> doubleValue(String key);

    /**
     * Retrieves the value of a double for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid double, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a double.
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the double value or {@link Optional#empty()} if there is
     *         double value associated with the key. The double inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    Optional<Double> doubleValue(int index);

    /**
     * <p>Retrieves the value of a boolean for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid boolean, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a boolean.</p>
     *
     * <p>A value is considered true if it is equivalent to the string "true" (case-insensitive). Otherwise,
     * the value is considered false. However, a sub-view is not considered a valid boolean.</p>
     * @param key       the key whose boolean value to retrieve
     * @return An {@link Optional} containing the boolean value or {@link Optional#empty()} if there is
     *         boolean value associated with the key. The boolean inside the {@link Optional} will never
     *         be null.
     */
    Optional<Boolean> booleanValue(String key);

    /**
     * <p>Retrieves the value of a boolean for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid boolean, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a boolean.</p>
     *
     * <p>A value is considered true if it is equivalent to the string "true" (case-insensitive). Otherwise,
     * the value is considered false. However, a sub-view is not considered a valid boolean.</p>
     * @param index       the index of the key whose boolean value to retrieve
     * @return An {@link Optional} containing the boolean value or {@link Optional#empty()} if there is
     *         boolean value associated with the key. The boolean inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    Optional<Boolean> booleanValue(int index);

    /**
     * <p>Retrieves the value of a stream for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid stream, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a stream.</p>
     * @param key       the key whose stream value to retrieve
     * @return An {@link Optional} containing the stream value or {@link Optional#empty()} if there is
     *         stream value associated with the key. The stream inside the {@link Optional} will never
     *         be null.
     */
    Optional<InputStream> byteStreamValue(String key);

    /**
     * <p>Retrieves the value of a stream for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid stream, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a stream.</p>
     * @param index       the index of the key whose stream value to retrieve
     * @return An {@link Optional} containing the stream value or {@link Optional#empty()} if there is
     *         stream value associated with the key. The stream inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    Optional<InputStream> byteStreamValue(int index);

    /**
     * Retrieves the sub-view for the given key if any exists. If there is no such key or
     * the key's associated value is not a valid sub-view, this method returns {@link Optional#empty()}.
     * {@link #hasKey(String)} can be used to determine whether the key is present or the value is
     * not a sub-view.
     * @param key       the key whose sub-view to retrieve
     * @return An {@link Optional} containing the sub-view or {@link Optional#empty()} if there is
     *         sub-view associated with the key. The sub-view inside the {@link Optional} will never
     *         be null.
     */
    Optional<MetadataView> subView(String key);

    /**
     * Retrieves a sub-view for the key at the given index if any exists. If there is no such
     * key or the key's associated value is not a valid sub-view, this method returns {@link Optional#empty()}.
     * {@link #hasKey(int)} can be used to determine whether the key is present or the value is
     * not a sub-view.
     * @param index       the index of the key whose sub-view value to retrieve
     * @return An {@link Optional} containing the sub-view or {@link Optional#empty()} if there is
     *         sub-view associated with the key. The sub-view inside the {@link Optional} will never
     *         be null.
     * @throws NegativeKeyIndexException if the provided index is negative
     */
    Optional<MetadataView> subView(int index);

    /**
     * Indicates that an illegal index was used to access a key in a {@link MetadataView}.
     * @author soir20
     * @since 4.0.0
     */
    final class NegativeKeyIndexException extends IndexOutOfBoundsException {

        /**
         * Creates a new exception to indicate that an illegal index was used to access a key.
         * @param index     the illegal index used
         */
        public NegativeKeyIndexException(int index) {
            super("Key index is negative: " + index);
        }

    }

}
