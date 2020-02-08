package com.parabola.domain.repository;

public interface ResourceRepository {

    String getString(int strResId);

    String getString(int strResId, Object... formatArgs);


    String getQuantityString(int pluralStringId, int quantity);
}
