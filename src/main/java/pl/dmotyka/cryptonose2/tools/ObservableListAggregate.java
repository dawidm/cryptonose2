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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ObservableListAggregate<T> {

    private static final Logger logger = Logger.getLogger(ObservableListAggregate.class.getName());

    public interface AggregateChangeListener<T> {
        void onChange(ObservableList<? extends T> changedList);
        void added(List<? extends T> added);
        void removed(List<? extends T> removed);
    }

    private AggregateChangeListener<T> aggregateChangeListener;

    private final List<ObservableList<T>> lists = new LinkedList<>();
    private final ObservableList<T> aggregate = FXCollections.observableArrayList();

    private final Map<ObservableList<T>, ListChangeListener<T>> listListenersMap = new HashMap<>();

    public synchronized void addList(ObservableList<T> list) {
        lists.add(list);
        ListChangeListener<T> listener = c -> {
            refreshAggregate();
            if (aggregateChangeListener != null) {
                List<T> added = new LinkedList<>();
                List<T> removed = new LinkedList<>();
                while (c.next()) {
                    if (c.wasAdded()) {
                        added.addAll(c.getAddedSubList());
                    }
                    if (c.wasRemoved()) {
                        removed.addAll(c.getRemoved());
                    }
                }
                aggregateChangeListener.onChange(FXCollections.unmodifiableObservableList(aggregate));
                if (added.size() != 0) {
                    aggregateChangeListener.added(added);
                }
                if (removed.size() != 0) {
                    aggregateChangeListener.removed(removed);
                }
            }
        };
        list.addListener(listener);
        listListenersMap.put(list, listener);
        refreshAggregate();
        if (aggregateChangeListener != null) {
            aggregateChangeListener.added(FXCollections.unmodifiableObservableList(list));
        }
    }

    public synchronized void removeList(ObservableList<T> list) {
        var listener = listListenersMap.get(list);
        if (listener != null) {
            list.removeListener(listener);
        } else {
            logger.warning("no listener for a list");
        }
        listListenersMap.remove(list);
        lists.remove(list);
        if (aggregateChangeListener != null) {
            aggregateChangeListener.removed(FXCollections.unmodifiableObservableList(list));
        }
        refreshAggregate();
    }

    // get aggregate which is updated when base lists change
    public synchronized ObservableList<T> getObservableAggregate() {
        return aggregate;
    }

    // get aggregate which is not updated when base lists change
    public synchronized ObservableList<T> getAggregate() {
        return FXCollections.observableArrayList(aggregate);
    }

    public void setListener(AggregateChangeListener<T> listener) {
        this.aggregateChangeListener = listener;
    }

    private void refreshAggregate() {
        aggregate.clear();
        aggregate.addAll(concatLists());
    }

    private ObservableList<T> concatLists() {
        ObservableList<T> allPairsObservableList = FXCollections.emptyObservableList();
        for (var list : lists) {
            allPairsObservableList = FXCollections.concat(allPairsObservableList, list);
        }
        return allPairsObservableList;
    }


}
