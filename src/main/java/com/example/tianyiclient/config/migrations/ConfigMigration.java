package com.example.tianyiclient.config.migrations;

import com.google.gson.JsonObject;

public abstract class ConfigMigration {
    public abstract int getFromVersion();
    public abstract int getToVersion();
    public abstract void migrate(JsonObject config);
}