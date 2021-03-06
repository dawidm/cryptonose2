/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

import static org.junit.Assert.*;

public class ObservableListAggregateTest {

    @Test
    public void testList() {
        ObservableListAggregate<String> agg = new ObservableListAggregate<>();
        ObservableList<String> obsAgg = agg.getObservableAggregate(); // will change as lists are modified
        ObservableList<String> l1 = FXCollections.observableArrayList("a", "b", "c");
        ObservableList<String> l2 = FXCollections.observableArrayList("f");
        ObservableList<String> l3 = FXCollections.observableArrayList("d", "e");
        agg.addList(l1);
        ObservableList<String> statAgg = agg.getAggregate(); // will not change
        agg.addList(l2);
        agg.addList(l3);
        l3.add("g");
        assertEquals(3, statAgg.size());
        assertEquals(7, obsAgg.size());
        Set<String> aggSet = new HashSet<>(obsAgg);
        Set<String> testSet = Set.of("b", "a", "c", "d", "e", "f", "g");
        assertTrue(testSet.containsAll(aggSet));

        agg.removeList(l1);
        assertEquals(4, obsAgg.size());

        AtomicBoolean changed = new AtomicBoolean(false);
        agg.setListener(new ObservableListAggregate.AggregateChangeListener<String>() {
            @Override
            public void onChange(ObservableList<? extends String> changedList) {
                changed.set(true);
            }

            @Override
            public void added(List<? extends String> added) {
                assertArrayEquals(new String[] {"x"}, added.toArray());
            }

            @Override
            public void removed(List<? extends String> removed) {
                assertArrayEquals(new String[] {"y"}, removed.toArray());
            }
        });
        l1.add("x"); // should call listener cause the list was removed
        assertFalse(changed.get());
        l3.add("x");
        assertTrue(changed.get());
        l3.remove("y");
    }

}