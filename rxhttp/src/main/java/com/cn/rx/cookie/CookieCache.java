package com.cn.rx.cookie;

import java.util.Collection;

import okhttp3.Cookie;

public interface CookieCache extends Iterable {


    public void addAll(Collection<Cookie> newCookies);


    public void clear();
}

