package com.parabola.newtone.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java8.util.Optional;
import java8.util.OptionalInt;

public final class SelectableList<E> {

    //позиция выбранного элемента
    private int selectedItemIndex = -1;
    private final List<E> elements = new ArrayList<>();

    public E get(int position) {
        return elements.get(position);
    }

    public void add(int index, E element) {
        elements.add(index, element);
        if (selectedItemIndex != -1 && selectedItemIndex >= index) {
            selectedItemIndex++;
        }
    }

    public void add(E newElement) {
        elements.add(newElement);
    }

    public boolean addAll(int index, Collection<? extends E> newElements) {
        boolean result = elements.addAll(index, newElements);

        if (selectedItemIndex != -1 && selectedItemIndex >= index) {
            selectedItemIndex += newElements.size();
        }

        return result;
    }

    public void addAll(Collection<? extends E> newElements) {
        elements.addAll(newElements);
    }

    public void clear() {
        elements.clear();
        selectedItemIndex = -1;
    }

    public void move(int from, int to) {
        elements.add(to, elements.remove(from));

        if (selectedItemIndex == from) {
            selectedItemIndex = to;
        } else if (selectedItemIndex > from && selectedItemIndex <= to) {
            selectedItemIndex--;
        } else if (selectedItemIndex < from && selectedItemIndex >= to) {
            selectedItemIndex++;
        }
    }

    public E remove(int index) {
        E removedItem = elements.remove(index);

        if (selectedItemIndex != -1) {
            if (elements.isEmpty()) {
                selectedItemIndex = -1;
            } else if (index < selectedItemIndex) {
                selectedItemIndex--;
            } else if (index == selectedItemIndex && (index == 0 || index == elements.size())) {
                selectedItemIndex = 0;
            }
        }

        return removedItem;
    }

    public boolean remove(E object) {
        int removedItemIndex = elements.indexOf(object);
        if (removedItemIndex == -1) {
            return false;
        }

        boolean result = elements.remove(object);

        if (result && selectedItemIndex != -1) {
            if (elements.isEmpty()) {
                selectedItemIndex = -1;
            } else if (removedItemIndex < selectedItemIndex) {
                selectedItemIndex--;
            } else if (removedItemIndex == selectedItemIndex && (removedItemIndex == 0 || removedItemIndex == elements.size())) {
                selectedItemIndex = 0;
            }
        }

        return result;
    }

    public int size() {
        return elements.size();
    }

    public Optional<E> getSelectedItem() {

        if (selectedItemIndex == -1) {
            return Optional.empty();
        }

        return Optional.of(elements.get(selectedItemIndex));
    }

    public OptionalInt getSelectedItemIndex() {
        if (selectedItemIndex == -1) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(selectedItemIndex);
    }

    public boolean setSelectedItemIndex(int selectedItemIndex) {
        if (selectedItemIndex < -1 || selectedItemIndex > elements.size() - 1) {
            throw new IndexOutOfBoundsException("Index: " + selectedItemIndex + ", Size: " + elements.size());
        }

        boolean result = (this.selectedItemIndex == -1)
                || (this.selectedItemIndex != selectedItemIndex);

        if (result) {
            this.selectedItemIndex = selectedItemIndex;
        }

        return result;
    }

    public boolean hasSelectedItem() {
        return selectedItemIndex != -1;
    }

    public List<E> getAsList() {
        return Collections.unmodifiableList(elements);
    }

    public void clearSelected() {
        selectedItemIndex = -1;
    }
}
