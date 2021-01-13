package com.newrelic.api.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread safe implementation of {@link Headers} using {@link ConcurrentHashMap} as the backing data structure. Concurrent
 * writes are supported. Reads return immutable data types.
 */
public class ConcurrentHashMapHeaders implements Headers {

    private final HeaderType headerType;
    private final Map<String, List<String>> headers;

    private ConcurrentHashMapHeaders(HeaderType headerType) {
        this.headerType = headerType;
        headers = new ConcurrentHashMap<>();
    }

    /**
     * Obtain a read only view of the backing map.
     *
     * @return The map view.
     */
    public Map<String, List<String>> getReadOnlyMap() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Remove any headers with the name.
     *
     * @param name The name of the header.
     */
    public void removeHeader(String name) {
        headers.remove(name);
    }

    @Override
    public HeaderType getHeaderType() {
        return headerType;
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        return values == null || values.size() == 0 ? null : values.get(0);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<String> values = headers.get(name);
        return values == null ? Collections.<String>emptyList() : Collections.unmodifiableList(values);
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);
    }

    @Override
    public void addHeader(String name, String value) {
        synchronized (headers) {
            List<String> values = headers.get(name);
            if (values == null) {
                setHeader(name, value);
            } else {
                values.add(value);
            }
        }
    }

    @Override
    public Collection<String> getHeaderNames() {
        return Collections.unmodifiableSet(headers.keySet());
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    /**
     * Build an empty instance with the given {@link HeaderType}.
     *
     * @param headerType The type of headers the instance represents.
     * @return The instance.
     */
    public static ConcurrentHashMapHeaders build(HeaderType headerType) {
        return new ConcurrentHashMapHeaders(headerType);
    }

    /**
     * Build an instance with the given {@link HeaderType} and call {@link #setHeader(String, String)} with each entry
     * in the {@code map}.
     *
     * @param headerType The type of headers the instance represents.
     * @param map A map of header names and values used to populate the instance.
     * @return The instance.
     */
    public static ConcurrentHashMapHeaders buildFromFlatMap(HeaderType headerType, Map<String, String> map) {
        ConcurrentHashMapHeaders headers = build(headerType);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            headers.setHeader(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * Build an instance with the given {@link HeaderType} and call {@link #addHeader(String, String)} with each entry
     * in the {@code map}.
     *
     * @param headerType The type of headers the instance represents.
     * @param map A map of header names and potentially several values each used to populate the instance.
     * @return The instance.
     */
    public static ConcurrentHashMapHeaders buildFromMap(HeaderType headerType, Map<String, List<String>> map) {
        ConcurrentHashMapHeaders headers = build(headerType);
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String value : entry.getValue()) {
                headers.addHeader(entry.getKey(), value);
            }
        }
        return headers;
    }

}