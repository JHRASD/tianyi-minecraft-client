package com.example.tianyiclient.hud.elements;

/**
 * 客户端水印HUD元素 - 使用动态数据绑定
 */
public class WatermarkElement extends TextHudElement {

    public WatermarkElement() {
        // 调用父类构造器：元素名称为"Watermark"，默认位置在屏幕(10, 10)像素处
        super("Watermark", 10.0f, 10.0f);

        // 设置水印文本（包含动态FPS数据）
        setText("TianyiClient ${fps} FPS");

        // 自定义颜色和阴影
        setColor(0xFFD700); // 金色
        setShadow(true);

        // 可以在这里添加额外的设置（可选）
        // 例如：设置不同的字体大小
        setFontSize(10);

        // 如果需要，可以添加更多数据绑定
        // addDataBinding(DataProviderRegistry.getMemoryBinding());
    }

    // 注意：我们不需要重写render()方法了，因为父类TextHudElement已经处理得很好
    // 动态数据绑定和渲染都由父类完成
}