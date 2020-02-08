package com.parabola.data.utils;

import android.database.Cursor;

import java.util.Iterator;

public class RxCursorIterable implements Iterable<Cursor> {

    private final Cursor mIterableCursor;

    public RxCursorIterable(Cursor c) {
        mIterableCursor = c;
    }

    public static RxCursorIterable from(Cursor c) {
        return new RxCursorIterable(c);
    }

    @Override
    public Iterator<Cursor> iterator() {
        return RxCursorIterator.from(mIterableCursor);
    }

    static class RxCursorIterator implements Iterator<Cursor> {

        private final Cursor cursor;

        public RxCursorIterator(Cursor cursor) {
            this.cursor = cursor;
        }

        public static Iterator<Cursor> from(Cursor cursor) {
            return new RxCursorIterator(cursor);
        }

        @Override
        public boolean hasNext() {
            return !cursor.isClosed() && cursor.moveToNext();
        }

        @Override
        public Cursor next() {
            return cursor;
        }

    }
}