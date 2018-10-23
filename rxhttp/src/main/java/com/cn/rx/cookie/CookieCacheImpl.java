package com.cn.rx.cookie;



import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.reactivex.annotations.NonNull;
import okhttp3.Cookie;

public class CookieCacheImpl implements CookieCache {
    private Set<IdentifiableCookie> cookies;

    public CookieCacheImpl() {
        cookies = new HashSet<>();
    }

    public void addAll(Collection<Cookie> newCookies) {
        for (IdentifiableCookie cookie : IdentifiableCookie.decorateAll(newCookies)) {
            this.cookies.remove(cookie);
            this.cookies.add(cookie);
        }
    }

    public void clear() {
        cookies.clear();
    }

    @NonNull
    @Override
    public Iterator<Cookie> iterator() {
        return new SetCookieCacheIterator();
    }

    private class SetCookieCacheIterator implements Iterator<Cookie> {

        private Iterator<IdentifiableCookie> iterator;

        SetCookieCacheIterator() {
            iterator = cookies.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Cookie next() {
            return iterator.next().getCookie();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
