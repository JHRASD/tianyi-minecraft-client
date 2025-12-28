package com.example.tianyiclient.config;

import com.example.tianyiclient.TianyiClient;
import com.example.tianyiclient.modules.Module;
import com.example.tianyiclient.settings.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ModuleConfig extends JsonConfig {
    private Map<String, ModuleData> modules = new HashMap<>();

    // 用于存储模块数据的内部类
    public static class ModuleData {
        public boolean enabled;
        public int keybind;
        public Map<String, SettingValue> settings = new HashMap<>();

        public static class SettingValue {
            public String type;
            public Object value;

            public SettingValue() {}

            public SettingValue(String type, Object value) {
                this.type = type;
                this.value = value;
            }
        }
    }

    public ModuleConfig(Path configPath) {
        super(configPath);
    }

    @Override
    protected JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("version", versionInfo.getVersion());
        root.addProperty("lastModified", versionInfo.getLastModified());

        JsonObject modulesJson = new JsonObject();
        for (Map.Entry<String, ModuleData> entry : modules.entrySet()) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", entry.getValue().enabled);
            moduleJson.addProperty("keybind", entry.getValue().keybind);

            JsonObject settingsJson = new JsonObject();
            for (Map.Entry<String, ModuleData.SettingValue> setting : entry.getValue().settings.entrySet()) {
                JsonObject settingJson = new JsonObject();
                settingJson.addProperty("type", setting.getValue().type);

                // 根据类型序列化值
                Object value = setting.getValue().value;
                if (value instanceof Boolean) {
                    settingJson.addProperty("value", (Boolean) value);
                } else if (value instanceof Number) {
                    settingJson.addProperty("value", (Number) value);
                } else if (value instanceof String) {
                    settingJson.addProperty("value", (String) value);
                } else if (value != null) {
                    settingJson.addProperty("value", value.toString());
                }

                settingsJson.add(setting.getKey(), settingJson);
            }
            moduleJson.add("settings", settingsJson);

            modulesJson.add(entry.getKey(), moduleJson);
        }

        root.add("modules", modulesJson);
        return root;
    }

    @Override
    protected void fromJson(JsonObject json) {
        modules.clear();

        if (json.has("modules")) {
            JsonObject modulesJson = json.getAsJsonObject("modules");
            for (String moduleName : modulesJson.keySet()) {
                JsonObject moduleJson = modulesJson.getAsJsonObject(moduleName);
                ModuleData data = new ModuleData();

                data.enabled = moduleJson.has("enabled") ? moduleJson.get("enabled").getAsBoolean() : false;
                data.keybind = moduleJson.has("keybind") ? moduleJson.get("keybind").getAsInt() : 0;

                if (moduleJson.has("settings")) {
                    JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                    for (String settingName : settingsJson.keySet()) {
                        JsonObject settingJson = settingsJson.getAsJsonObject(settingName);
                        String type = settingJson.get("type").getAsString();
                        Object value = null;

                        if (settingJson.has("value")) {
                            switch (type) {
                                case "BOOLEAN":
                                    value = settingJson.get("value").getAsBoolean();
                                    break;
                                case "DOUBLE":
                                case "INTEGER":
                                    value = settingJson.get("value").getAsNumber();
                                    break;
                                case "STRING":
                                    value = settingJson.get("value").getAsString();
                                    break;
                                case "KEYBIND":
                                    value = settingJson.get("value").getAsInt();
                                    break;
                                default:
                                    value = settingJson.get("value").getAsString();
                            }
                        }

                        data.settings.put(settingName, new ModuleData.SettingValue(type, value));
                    }
                }

                modules.put(moduleName, data);
            }
        }
    }

    public void saveFromManager() {
        if (TianyiClient.getInstance() == null || TianyiClient.getInstance().getModuleManager() == null) {
            return;
        }

        modules.clear();

        for (Module module : TianyiClient.getInstance().getModuleManager().getModules()) {
            ModuleData data = new ModuleData();
            data.enabled = module.isEnabled();
            data.keybind = module.getKeybind();

            for (Setting<?> setting : module.getSettings()) {
                String type = getSettingType(setting);
                Object value = getSettingValue(setting);

                if (type != null && value != null) {
                    data.settings.put(setting.getName(), new ModuleData.SettingValue(type, value));
                }
            }

            modules.put(module.getName(), data);
        }

        try {
            save();
        } catch (Exception e) {
            TianyiClient.LOGGER.error("保存模块配置失败", e);
        }
    }

    public void loadToManager() {
        if (TianyiClient.getInstance() == null || TianyiClient.getInstance().getModuleManager() == null) {
            return;
        }

        try {
            load();
        } catch (Exception e) {
            TianyiClient.LOGGER.error("加载模块配置失败，使用默认配置", e);
            return;
        }

        for (Map.Entry<String, ModuleData> entry : modules.entrySet()) {
            Module module = TianyiClient.getInstance().getModuleManager().getModuleByName(entry.getKey());
            if (module != null) {
                // 应用启用状态
                if (entry.getValue().enabled && !module.isEnabled()) {
                    module.enable();
                } else if (!entry.getValue().enabled && module.isEnabled()) {
                    module.disable();
                }

                // 应用快捷键
                if (entry.getValue().keybind != 0) {
                    module.setKeybind(entry.getValue().keybind);
                }

                // 应用设置
                for (Map.Entry<String, ModuleData.SettingValue> settingEntry : entry.getValue().settings.entrySet()) {
                    Setting<?> setting = module.getSettingByName(settingEntry.getKey());
                    if (setting != null) {
                        applySettingValue(setting, settingEntry.getValue());
                    }
                }
            }
        }
    }

    private String getSettingType(Setting<?> setting) {
        if (setting instanceof BoolSetting) return "BOOLEAN";
        if (setting instanceof DoubleSetting) return "DOUBLE";
        if (setting instanceof IntegerSetting) return "INTEGER";
        if (setting instanceof StringSetting) return "STRING";
        if (setting instanceof KeybindSetting) return "KEYBIND";
        if (setting instanceof BindSetting) return "BIND";
        return null;
    }

    private Object getSettingValue(Setting<?> setting) {
        if (setting instanceof BoolSetting) {
            return ((BoolSetting) setting).getValue();
        } else if (setting instanceof DoubleSetting) {
            return ((DoubleSetting) setting).getValue();
        } else if (setting instanceof IntegerSetting) {
            return ((IntegerSetting) setting).getValue();
        } else if (setting instanceof StringSetting) {
            return ((StringSetting) setting).getValue();
        } else if (setting instanceof KeybindSetting) {
            return ((KeybindSetting) setting).getValue();
        } else if (setting instanceof BindSetting) {
            return ((BindSetting) setting).getValue();
        }
        return null;
    }

    private void applySettingValue(Setting<?> setting, ModuleData.SettingValue settingValue) {
        try {
            if (setting instanceof BoolSetting && settingValue.value instanceof Boolean) {
                ((BoolSetting) setting).setValue((Boolean) settingValue.value);
            } else if (setting instanceof DoubleSetting && settingValue.value instanceof Number) {
                ((DoubleSetting) setting).setValue(((Number) settingValue.value).doubleValue());
            } else if (setting instanceof IntegerSetting && settingValue.value instanceof Number) {
                ((IntegerSetting) setting).setValue(((Number) settingValue.value).intValue());
            } else if (setting instanceof StringSetting && settingValue.value instanceof String) {
                ((StringSetting) setting).setValue((String) settingValue.value);
            } else if (setting instanceof KeybindSetting && settingValue.value instanceof Number) {
                ((KeybindSetting) setting).setValue(((Number) settingValue.value).intValue());
            } else if (setting instanceof BindSetting && settingValue.value instanceof Number) {
                ((BindSetting) setting).setValue(((Number) settingValue.value).intValue());
            }
        } catch (Exception e) {
            TianyiClient.LOGGER.warn("应用设置失败: {} = {}", setting.getName(), settingValue.value, e);
        }
    }
}