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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableSet.of;
import static org.junit.Assert.*;
import static org.onosproject.net.DefaultAnnotations.builder;

/**
 * Tests of the default annotations.
 */
public class DefaultAnnotationsTest {

    private DefaultAnnotations annotations;

    @Test
    public void basics() {
        annotations = builder().set("foo", "1").set("bar", "2").build();
        assertEquals("incorrect keys", of("foo", "bar"), annotations.keys());
        assertEquals("incorrect value", "1", annotations.value("foo"));
        assertEquals("incorrect value", "2", annotations.value("bar"));
    }

    @Test
    public void objectKey() {
        InternalObject object1 = new InternalObject(1 ,"hello");
        InternalObject object2 = new InternalObject(2 ,"world");
        annotations = builder()
                .set("object1", object1)
                .set("object2", object2)
                .build();
        assertEquals("incorrect key", of("object1", "object2"), annotations.keys());
        assertEquals("incorrect value", object1, annotations.value("object1", InternalObject.class));
        assertEquals("incorrect value", object2, annotations.value("object2", InternalObject.class));
    }

    @Test
    public void objectKeys() {
        List<InternalObject> objects = new ArrayList<>();
        InternalObject object1 = new InternalObject(1 ,"hello");
        InternalObject object2 = new InternalObject(2 ,"world");
        objects.add(object1);
        objects.add(object2);
        annotations = builder().set("objects", objects).build();
        assertEquals("incorrect key", of("objects"), annotations.keys());
        assertEquals("incorrect value", objects, annotations.values("objects", InternalObject.class));
    }
    @Test
    public void empty() {
        annotations = builder().build();
        assertTrue("incorrect keys", annotations.keys().isEmpty());
    }

    @Test
    public void remove() {
        annotations = builder().remove("foo").set("bar", "2").build();
        assertEquals("incorrect keys", of("foo", "bar"), annotations.keys());
        assertNull("incorrect value", annotations.value("foo"));
        assertEquals("incorrect value", "2", annotations.value("bar"));
    }

    @Test
    public void union() {
        annotations = builder().set("foo", "1").set("bar", "2").remove("buz").build();
        assertEquals("incorrect keys", of("foo", "bar", "buz"), annotations.keys());

        SparseAnnotations updates = builder().remove("foo").set("bar", "3").set("goo", "4").remove("fuzz").build();

        SparseAnnotations result = DefaultAnnotations.union(annotations, updates);

        assertTrue("remove instruction in original remains", result.isRemoved("buz"));
        assertTrue("remove instruction in update remains", result.isRemoved("fuzz"));
        assertEquals("incorrect keys", of("buz", "goo", "bar", "fuzz"), result.keys());
        assertNull("incorrect value", result.value("foo"));
        assertEquals("incorrect value", "3", result.value("bar"));
        assertEquals("incorrect value", "4", result.value("goo"));
    }

    @Test
    public void merge() {
        annotations = builder().set("foo", "1").set("bar", "2").build();
        assertEquals("incorrect keys", of("foo", "bar"), annotations.keys());

        SparseAnnotations updates = builder().remove("foo").set("bar", "3").set("goo", "4").build();

        annotations = DefaultAnnotations.merge(annotations, updates);
        assertEquals("incorrect keys", of("goo", "bar"), annotations.keys());
        assertNull("incorrect value", annotations.value("foo"));
        assertEquals("incorrect value", "3", annotations.value("bar"));
    }

    @Test
    public void noopMerge() {
        annotations = builder().set("foo", "1").set("bar", "2").build();
        assertEquals("incorrect keys", of("foo", "bar"), annotations.keys());

        SparseAnnotations updates = builder().build();
        assertSame("same annotations expected", annotations,
                   DefaultAnnotations.merge(annotations, updates));
        assertSame("same annotations expected", annotations,
                   DefaultAnnotations.merge(annotations, null));
    }

    @Test(expected = NullPointerException.class)
    public void badMerge() {
        DefaultAnnotations.merge(null, null);
    }

    private class InternalObject {
        private int id;
        private String content;

        InternalObject(int id, String content) {
            this.id = id;
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }
    }

}
