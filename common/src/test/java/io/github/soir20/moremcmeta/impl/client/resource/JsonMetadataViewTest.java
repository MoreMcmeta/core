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

package io.github.soir20.moremcmeta.impl.client.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link JsonMetadataView}.
 * @author soir20
 */
public class JsonMetadataViewTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullObject_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(null, String::compareTo);
    }

    @Test
    public void construct_NullComparator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(new JsonObject(), null);
    }

    @Test
    public void construct_NullArray_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new JsonMetadataView(null);
    }

    @Test
    public void sizeObject_Empty_0() {
        JsonMetadataView view = new JsonMetadataView(new JsonObject(), String::compareTo);
        assertEquals(0, view.size());
    }

    @Test
    public void sizeObject_SingleLevelKeys_NumberOfKeys() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(3, view.size());
    }

    @Test
    public void sizeObject_NullValue_NullExcluded() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        root.add("testing", null);
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(3, view.size());
    }

    @Test
    public void sizeObject_MultiLevelKeys_NumberOfTopLevelKeys() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        root.add("testing", new JsonObject());
        root.get("testing").getAsJsonObject().add("metadata", new JsonPrimitive(20));
        root.get("testing").getAsJsonObject().add("view", new JsonPrimitive(false));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(4, view.size());
    }

    @Test
    public void sizeArray_Empty_0() {
        JsonMetadataView view = new JsonMetadataView(new JsonArray());
        assertEquals(0, view.size());
    }

    @Test
    public void sizeArray_SingleLevelKeys_NumberOfKeys() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(3, view.size());
    }

    @Test
    public void sizeArray_NullValue_NullExcluded() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        root.add((JsonObject) null);
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(3, view.size());
    }

    @Test
    public void sizeArray_MultiLevelKeys_NumberOfTopLevelKeys() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        root.add(new JsonObject());
        root.get(3).getAsJsonObject().add("metadata", new JsonPrimitive(20));
        root.get(3).getAsJsonObject().add("view", new JsonPrimitive(false));
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(4, view.size());
    }

    @Test
    public void keysObject_Empty_NoKeys() {
        JsonMetadataView view = new JsonMetadataView(new JsonObject(), String::compareTo);
        assertEquals(List.of(), collectKeys(view.keys()));
    }

    @Test
    public void keysObject_SingleLevelKeys_AllKeys() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(List.of("hello", "test", "world"), collectKeys(view.keys()));
    }

    @Test
    public void keysObject_MultiLevelKeys_TopLevelKeys() {
        JsonObject root = new JsonObject();
        root.add("hello", new JsonPrimitive(10));
        root.add("world", new JsonPrimitive(true));
        root.add("test", new JsonPrimitive("good morning"));
        root.add("testing", new JsonObject());
        root.get("testing").getAsJsonObject().add("metadata", new JsonPrimitive(20));
        root.get("testing").getAsJsonObject().add("view", new JsonPrimitive(false));
        JsonMetadataView view = new JsonMetadataView(root, String::compareTo);
        assertEquals(List.of("hello", "test", "testing", "world"), collectKeys(view.keys()));
    }

    @Test
    public void keysArray_Empty_NoKeys() {
        JsonMetadataView view = new JsonMetadataView(new JsonArray());
        assertEquals(List.of(), collectKeys(view.keys()));
    }

    @Test
    public void keysArray_SingleLevelKeys_AllKeys() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(List.of("0", "1", "2"), collectKeys(view.keys()));
    }

    @Test
    public void keysArray_MultiLevelKeys_TopLevelKeys() {
        JsonArray root = new JsonArray();
        root.add(10);
        root.add(true);
        root.add("good morning");
        root.add(new JsonObject());
        root.get(3).getAsJsonObject().add("metadata", new JsonPrimitive(20));
        root.get(3).getAsJsonObject().add("view", new JsonPrimitive(false));
        JsonMetadataView view = new JsonMetadataView(root);
        assertEquals(List.of("0", "1", "2", "3"), collectKeys(view.keys()));
    }

    @Test
    public void hasKeyStringObject_KeyNotPresent_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.hasKey("not present"));
    }

    @Test
    public void hasKeyStringObject_KeyAtNextLevel_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.hasKey("object val1"));
    }

    @Test
    public void hasKeyStringObject_NullVal_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.hasKey("null val0"));
    }

    @Test
    public void hasKeyStringObject_StringVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("string val0"));
    }

    @Test
    public void hasKeyStringObject_PosIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos int val0"));
    }

    @Test
    public void hasKeyStringObject_NegIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg int val0"));
    }

    @Test
    public void hasKeyStringObject_PosLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos long val0"));
    }

    @Test
    public void hasKeyStringObject_NegLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg long val0"));
    }

    @Test
    public void hasKeyStringObject_PosBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos int >64-bits val0"));
    }

    @Test
    public void hasKeyStringObject_NegBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg int >64-bits val0"));
    }

    @Test
    public void hasKeyStringObject_PosFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos float val0"));
    }

    @Test
    public void hasKeyStringObject_NegFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg float val0"));
    }

    @Test
    public void hasKeyStringObject_PosDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("pos double val0"));
    }

    @Test
    public void hasKeyStringObject_NegDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("neg double val0"));
    }

    @Test
    public void hasKeyStringObject_TrueVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("true val0"));
    }

    @Test
    public void hasKeyStringObject_FalseVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("false val0"));
    }

    @Test
    public void hasKeyStringObject_ObjectVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("object val0"));
    }

    @Test
    public void hasKeyStringObject_ArrayVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertTrue(view.hasKey("array val0"));
    }

    @Test
    public void hasKeyStringArray_KeyNegative_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey("-1"));
    }

    @Test
    public void hasKeyStringArray_KeyNotPresent_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey("not present"));
    }

    @Test
    public void hasKeyStringArray_KeyAtNextLevel_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey("object val1"));
    }

    @Test
    public void hasKeyStringArray_ValidIfNullNotFiltered_False() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.hasKey("15"));
    }

    @Test
    public void hasKeyStringArray_StringVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("0"));
    }

    @Test
    public void hasKeyStringArray_PosIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("1"));
    }

    @Test
    public void hasKeyStringArray_NegIntVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("2"));
    }

    @Test
    public void hasKeyStringArray_PosLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("3"));
    }

    @Test
    public void hasKeyStringArray_NegLongVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("4"));
    }

    @Test
    public void hasKeyStringArray_PosBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("5"));
    }

    @Test
    public void hasKeyStringArray_NegBeyond64BitsVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("6"));
    }

    @Test
    public void hasKeyStringArray_PosFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("7"));
    }

    @Test
    public void hasKeyStringArray_NegFloatVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("8"));
    }

    @Test
    public void hasKeyStringArray_PosDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("9"));
    }

    @Test
    public void hasKeyStringArray_NegDoubleVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("10"));
    }

    @Test
    public void hasKeyStringArray_TrueVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("11"));
    }

    @Test
    public void hasKeyStringArray_FalseVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("12"));
    }

    @Test
    public void hasKeyStringArray_ObjectVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("13"));
    }

    @Test
    public void hasKeyStringArray_ArrayVal_True() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertTrue(view.hasKey("14"));
    }

    @Test
    public void stringValueStringObject_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("not present").isPresent());
    }

    @Test
    public void stringValueStringObject_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("object val1").isPresent());
    }

    @Test
    public void stringValueStringObject_NullVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("null val0").isPresent());
    }

    @Test
    public void stringValueStringObject_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("hello world", view.stringValue("string val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Integer.MAX_VALUE), view.stringValue("pos int val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Integer.MIN_VALUE), view.stringValue("neg int val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Long.MAX_VALUE), view.stringValue("pos long val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Long.MIN_VALUE), view.stringValue("neg long val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("9223372036854775808", view.stringValue("pos int >64-bits val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("-9223372036854775809", view.stringValue("neg int >64-bits val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Float.MAX_VALUE), view.stringValue("pos float val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(-Float.MAX_VALUE), view.stringValue("neg float val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(Double.MAX_VALUE), view.stringValue("pos double val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals(String.valueOf(-Double.MAX_VALUE), view.stringValue("neg double val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("true", view.stringValue("true val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertEquals("false", view.stringValue("false val0").orElseThrow());
    }

    @Test
    public void stringValueStringObject_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("object val0").isPresent());
    }

    @Test
    public void stringValueStringObject_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoObject(), String::compareTo);
        assertFalse(view.stringValue("array val0").isPresent());
    }

    @Test
    public void stringValueStringArray_KeyNegative_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("-1").isPresent());
    }

    @Test
    public void stringValueStringArray_KeyNotPresent_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("not present").isPresent());
    }

    @Test
    public void stringValueStringArray_KeyAtNextLevel_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("object val1").isPresent());
    }

    @Test
    public void stringValueStringArray_ValidIfNullNotFiltered_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("15").isPresent());
    }

    @Test
    public void stringValueStringArray_StringVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("hello world", view.stringValue("0").orElseThrow());
    }

    @Test
    public void stringValueStringArray_PosIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Integer.MAX_VALUE), view.stringValue("1").orElseThrow());
    }

    @Test
    public void stringValueStringArray_NegIntVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Integer.MIN_VALUE), view.stringValue("2").orElseThrow());
    }

    @Test
    public void stringValueStringArray_PosLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Long.MAX_VALUE), view.stringValue("3").orElseThrow());
    }

    @Test
    public void stringValueStringArray_NegLongVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Long.MIN_VALUE), view.stringValue("4").orElseThrow());
    }

    @Test
    public void stringValueStringArray_PosBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("9223372036854775808", view.stringValue("5").orElseThrow());
    }

    @Test
    public void stringValueStringArray_NegBeyond64BitsVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("-9223372036854775809", view.stringValue("6").orElseThrow());
    }

    @Test
    public void stringValueStringArray_PosFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Float.MAX_VALUE), view.stringValue("7").orElseThrow());
    }

    @Test
    public void stringValueStringArray_NegFloatVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(-Float.MAX_VALUE), view.stringValue("8").orElseThrow());
    }

    @Test
    public void stringValueStringArray_PosDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(Double.MAX_VALUE), view.stringValue("9").orElseThrow());
    }

    @Test
    public void stringValueStringArray_NegDoubleVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals(String.valueOf(-Double.MAX_VALUE), view.stringValue("10").orElseThrow());
    }

    @Test
    public void stringValueStringArray_TrueVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("true", view.stringValue("11").orElseThrow());
    }

    @Test
    public void stringValueStringArray_FalseVal_ValueFound() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertEquals("false", view.stringValue("12").orElseThrow());
    }

    @Test
    public void stringValueStringArray_ObjectVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("13").isPresent());
    }

    @Test
    public void stringValueStringArray_ArrayVal_Empty() {
        JsonMetadataView view = new JsonMetadataView(makeDemoArray());
        assertFalse(view.stringValue("14").isPresent());
    }

    private JsonObject makeDemoObject() {
        JsonObject root = new JsonObject();
        addAllTypeVals(root, 0);
        addAllTypeVals(root.getAsJsonObject("object val0"), 1);
        addAllTypeVals(root.getAsJsonArray("array val0"));
        return root;
    }

    private JsonArray makeDemoArray() {
        JsonArray root = new JsonArray();
        addAllTypeVals(root);
        addAllTypeVals(root.get(14).getAsJsonObject(), 1);
        addAllTypeVals(root.get(15).getAsJsonArray());
        return root;
    }

    private void addAllTypeVals(JsonObject root, int level) {
        root.add("null val" + level, null);
        root.add("string val" + level, new JsonPrimitive("hello world"));
        root.add("pos int val" + level, new JsonPrimitive(Integer.MAX_VALUE));
        root.add("neg int val" + level, new JsonPrimitive(Integer.MIN_VALUE));
        root.add("pos long val" + level, new JsonPrimitive(Long.MAX_VALUE));
        root.add("neg long val" + level, new JsonPrimitive(Long.MIN_VALUE));
        root.add("pos int >64-bits val" + level, new JsonPrimitive(new BigInteger("9223372036854775808")));
        root.add("neg int >64-bits val" + level, new JsonPrimitive(new BigInteger("-9223372036854775809")));
        root.add("pos float val" + level, new JsonPrimitive(Float.MAX_VALUE));
        root.add("neg float val" + level, new JsonPrimitive(-Float.MAX_VALUE));
        root.add("pos double val" + level, new JsonPrimitive(Double.MAX_VALUE));
        root.add("neg double val" + level, new JsonPrimitive(-Double.MAX_VALUE));
        root.add("true val" + level, new JsonPrimitive(true));
        root.add("false val" + level, new JsonPrimitive(false));
        root.add("object val" + level, new JsonObject());
        root.add("array val" + level, new JsonArray());
    }

    private void addAllTypeVals(JsonArray root) {
        root.add((JsonObject) null);
        root.add(new JsonPrimitive("hello world"));
        root.add(new JsonPrimitive(Integer.MAX_VALUE));
        root.add(new JsonPrimitive(Integer.MIN_VALUE));
        root.add(new JsonPrimitive(Long.MAX_VALUE));
        root.add(new JsonPrimitive(Long.MIN_VALUE));
        root.add(new JsonPrimitive(new BigInteger("9223372036854775808")));
        root.add(new JsonPrimitive(new BigInteger("-9223372036854775809")));
        root.add(new JsonPrimitive(Float.MAX_VALUE));
        root.add(new JsonPrimitive(-Float.MAX_VALUE));
        root.add(new JsonPrimitive(Double.MAX_VALUE));
        root.add(new JsonPrimitive(-Double.MAX_VALUE));
        root.add(new JsonPrimitive(true));
        root.add(new JsonPrimitive(false));
        root.add(new JsonObject());
        root.add(new JsonArray());
    }

    private List<String> collectKeys(Iterable<String> keys) {
        List<String> keyList = new ArrayList<>();
        for (String key : keys) {
            keyList.add(key);
        }
        return keyList;
    }

}