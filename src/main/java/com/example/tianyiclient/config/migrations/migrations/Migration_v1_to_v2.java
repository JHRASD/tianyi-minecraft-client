package com.example.tianyiclient.config.migrations.migrations;

import com.example.tianyiclient.config.migrations.ConfigMigration;
import com.google.gson.JsonObject;

public class Migration_v1_to_v2 extends ConfigMigration {
    @Override
    public int getFromVersion() {
        return 1;
    }

    @Override
    public int getToVersion() {
        return 2;
    }

    @Override
    public void migrate(JsonObject config) {
        System.out.println("执行配置迁移: v1 -> v2");

        // 示例迁移：如果旧版本中没有某个字段，添加默认值
        if (!config.has("clientInfo")) {
            JsonObject clientInfo = new JsonObject();
            clientInfo.addProperty("mcVersion", "1.21.8");
            clientInfo.addProperty("modVersion", "1.0.0");
            config.add("clientInfo", clientInfo);
        }
    }
}