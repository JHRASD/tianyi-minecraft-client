package com.example.tianyiclient.hud.binding;

/**
 * 数据绑定 - 将变量名绑定到数据提供器
 */
public class DataBinding {
    private final String placeholder; // 如 "${fps}"、"${coords}"
    private final ValueProvider provider;
    private String cachedValue;
    private long lastUpdateTime;
    private final long updateInterval; // 更新间隔(ms)

    public DataBinding(String placeholder, ValueProvider provider) {
        this(placeholder, provider, 100); // 默认100ms更新一次
    }

    public DataBinding(String placeholder, ValueProvider provider, long updateInterval) {
        this.placeholder = placeholder;
        this.provider = provider;
        this.updateInterval = updateInterval;
        this.cachedValue = "";
        this.lastUpdateTime = 0;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getValue() {
        long currentTime = System.currentTimeMillis();
        // 如果超过更新间隔或值未初始化，则更新
        if (currentTime - lastUpdateTime > updateInterval || cachedValue.isEmpty()) {
            cachedValue = provider.getValue();
            lastUpdateTime = currentTime;
        }
        return cachedValue;
    }

    /**
     * 数据提供器接口
     */
    public interface ValueProvider {
        String getValue();
    }
}