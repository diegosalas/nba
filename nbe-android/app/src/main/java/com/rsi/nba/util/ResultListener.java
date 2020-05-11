package com.rsi.nba.util;

public interface ResultListener<T> {
    void finish(T result);
}
