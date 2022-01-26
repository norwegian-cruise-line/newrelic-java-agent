/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.newrelic.agent.model;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class LogEvent extends AnalyticsEvent implements JSONStreamAware {

    private volatile float mutablePriority;

    public LogEvent(String type, long timestamp, Map<String, Object> attributes, float priority) {
        super(type, timestamp, priority, attributes);
        this.mutablePriority = priority;
    }

    @Override
    public float getPriority() {
        return mutablePriority;
    }

    public void setPriority(float priority) {
        this.mutablePriority = priority;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONObject.writeJSONString(getMutableUserAttributes(), out);
    }

}
