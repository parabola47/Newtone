package com.parabola.data.repository;

import android.content.Context;

import com.parabola.domain.repository.ResourceRepository;

public final class ResourceRepositoryImpl implements ResourceRepository {

    private final Context context;

    public ResourceRepositoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public String getString(int strResId) {
        return context.getString(strResId);
    }

    @Override
    public String getString(int strResId, Object... formatArgs) {
        return context.getString(strResId, formatArgs);
    }


    @Override
    public String getQuantityString(int pluralStringId, int quantity) {
        return context.getResources().getQuantityString(pluralStringId, quantity, quantity);
    }
}
