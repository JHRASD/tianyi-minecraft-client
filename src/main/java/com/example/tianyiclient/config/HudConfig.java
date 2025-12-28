package com.example.tianyiclient.config;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.hud.HudElement;
import com.example.tianyiclient.hud.HudManager;
import com.example.tianyiclient.settings.Setting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class HudConfig extends JsonConfig {
    private Map<String, HudElementData> elements = new HashMap<>();

    public static class HudElementData {
        public boolean enabled = true;
        public float x = 10;
        public float y = 10;
        public float scale = 1.0f;
        public float alpha = 1.0f;
        public boolean visible = true;
        public Map<String, Object> settings = new HashMap<>();
    }

    public HudConfig(Path configPath) {
        super(configPath);
    }

    @Override
    protected JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("version", versionInfo.getVersion());
        root.addProperty("lastModified", versionInfo.getLastModified());

        JsonObject elementsJson = new JsonObject();
        for (Map.Entry<String, HudElementData> entry : elements.entrySet()) {
            JsonObject elementJson = new JsonObject();
            elementJson.addProperty("enabled", entry.getValue().enabled);
            elementJson.addProperty("x", entry.getValue().x);
            elementJson.addProperty("y", entry.getValue().y);
            elementJson.addProperty("scale", entry.getValue().scale);
            elementJson.addProperty("alpha", entry.getValue().alpha);
            elementJson.addProperty("visible", entry.getValue().visible);

            // 保存设置
            if (!entry.getValue().settings.isEmpty()) {
                JsonObject settingsJson = new JsonObject();
                for (Map.Entry<String, Object> settingEntry : entry.getValue().settings.entrySet()) {
                    Object value = settingEntry.getValue();
                    if (value instanceof Boolean) {
                        settingsJson.addProperty(settingEntry.getKey(), (Boolean) value);
                    } else if (value instanceof Number) {
                        settingsJson.addProperty(settingEntry.getKey(), (Number) value);
                    } else if (value instanceof String) {
                        settingsJson.addProperty(settingEntry.getKey(), (String) value);
                    }
                }
                elementJson.add("settings", settingsJson);
            }

            elementsJson.add(entry.getKey(), elementJson);
        }

        root.add("elements", elementsJson);
        return root;
    }

    @Override
    protected void fromJson(JsonObject json) {
        elements.clear();

        if (json.has("elements")) {
            JsonObject elementsJson = json.getAsJsonObject("elements");
            for (String elementName : elementsJson.keySet()) {
                JsonObject elementJson = elementsJson.getAsJsonObject(elementName);
                HudElementData data = new HudElementData();

                if (elementJson.has("enabled")) data.enabled = elementJson.get("enabled").getAsBoolean();
                if (elementJson.has("x")) data.x = elementJson.get("x").getAsFloat();
                if (elementJson.has("y")) data.y = elementJson.get("y").getAsFloat();
                if (elementJson.has("scale")) data.scale = elementJson.get("scale").getAsFloat();
                if (elementJson.has("alpha")) data.alpha = elementJson.get("alpha").getAsFloat();
                if (elementJson.has("visible")) data.visible = elementJson.get("visible").getAsBoolean();

                // 加载设置
                if (elementJson.has("settings")) {
                    JsonObject settingsJson = elementJson.getAsJsonObject("settings");
                    for (String settingName : settingsJson.keySet()) {
                        if (settingsJson.get(settingName).isJsonPrimitive()) {
                            JsonPrimitive primitive = settingsJson.get(settingName).getAsJsonPrimitive();
                            if (primitive.isBoolean()) {
                                data.settings.put(settingName, primitive.getAsBoolean());
                            } else if (primitive.isNumber()) {
                                data.settings.put(settingName, primitive.getAsNumber());
                            } else if (primitive.isString()) {
                                data.settings.put(settingName, primitive.getAsString());
                            }
                        }
                    }
                }

                elements.put(elementName, data);
            }
        }
    }

    public void loadToManager() {
        try {
            load();
        } catch (Exception e) {
            TianyiClient.LOGGER.error("加载HUD配置失败，使用默认配置", e);
            return;
        }

        if (TianyiClient.getInstance() == null || TianyiClient.getInstance().getHudManager() == null) {
            return;
        }

        HudManager hudManager = TianyiClient.getInstance().getHudManager();
        for (HudElement element : hudManager.getElements()) {
            HudElementData data = elements.get(element.getName());
            if (data != null) {
                // 应用基础属性
                element.setEnabled(data.enabled);
                element.setPosition(data.x, data.y);
                element.setVisible(data.visible);

                // 应用设置值
                for (Map.Entry<String, Object> settingEntry : data.settings.entrySet()) {
                    for (Setting<?> setting : element.getSettings()) {
                        if (setting.getName().equals(settingEntry.getKey())) {
                            applySettingValue(setting, settingEntry.getValue());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void applySettingValue(Setting<?> setting, Object value) {
        try {
            if (setting instanceof com.example.tianyiclient.settings.BoolSetting && value instanceof Boolean) {
                ((com.example.tianyiclient.settings.BoolSetting) setting).setValue((Boolean) value);
            } else if (setting instanceof com.example.tianyiclient.settings.DoubleSetting && value instanceof Number) {
                ((com.example.tianyiclient.settings.DoubleSetting) setting).setValue(((Number) value).doubleValue());
            } else if (setting instanceof com.example.tianyiclient.settings.IntegerSetting && value instanceof Number) {
                ((com.example.tianyiclient.settings.IntegerSetting) setting).setValue(((Number) value).intValue());
            } else if (setting instanceof com.example.tianyiclient.settings.StringSetting && value instanceof String) {
                ((com.example.tianyiclient.settings.StringSetting) setting).setValue((String) value);
            }
        } catch (Exception e) {
            TianyiClient.LOGGER.warn("应用HUD设置失败: {} = {}", setting.getName(), value, e);
        }
    }

    public void saveFromManager() {
        if (TianyiClient.getInstance() == null || TianyiClient.getInstance().getHudManager() == null) {
            return;
        }

        elements.clear();

        HudManager hudManager = TianyiClient.getInstance().getHudManager();
        for (HudElement element : hudManager.getElements()) {
            HudElementData data = new HudElementData();
            data.enabled = element.isEnabled();
            data.x = element.getX();
            data.y = element.getY();
            data.visible = element.isVisible();

            // 获取所有设置
            for (Setting<?> setting : element.getSettings()) {
                Object value = getSettingValue(setting);
                if (value != null) {
                    data.settings.put(setting.getName(), value);
                }
            }

            // 获取特殊设置
            Double scale = element.getSettingByDescription("缩放", Double.class);
            Double alpha = element.getSettingByDescription("透明度", Double.class);
            if (scale != null) data.scale = scale.floatValue();
            if (alpha != null) data.alpha = alpha.floatValue();

            elements.put(element.getName(), data);
        }

        try {
            save();
        } catch (Exception e) {
            TianyiClient.LOGGER.error("保存HUD配置失败", e);
        }
    }

    private Object getSettingValue(Setting<?> setting) {
        if (setting instanceof com.example.tianyiclient.settings.BoolSetting) {
            return ((com.example.tianyiclient.settings.BoolSetting) setting).getValue();
        } else if (setting instanceof com.example.tianyiclient.settings.DoubleSetting) {
            return ((com.example.tianyiclient.settings.DoubleSetting) setting).getValue();
        } else if (setting instanceof com.example.tianyiclient.settings.IntegerSetting) {
            return ((com.example.tianyiclient.settings.IntegerSetting) setting).getValue();
        } else if (setting instanceof com.example.tianyiclient.settings.StringSetting) {
            return ((com.example.tianyiclient.settings.StringSetting) setting).getValue();
        }
        return null;
    }
}