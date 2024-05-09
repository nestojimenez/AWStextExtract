// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.example.bedrockruntime.libs.demo.scenarios;

import com.example.bedrockruntime.libs.demo.DemoRunner;
import org.json.JSONObject;

import java.io.IOException;
import java.util.function.BiFunction;

public abstract class Scenario {
    public static final String WAITING_FOR_RESPONSE = "Waiting for the model's response...\n";

    protected final BiFunction<String, String, JSONObject> action;
    private final String title;

    protected Scenario(BiFunction<String, String, JSONObject> action, String title) {
        this.action = action;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public abstract void run(DemoRunner.DemoState state) throws IOException;
}
