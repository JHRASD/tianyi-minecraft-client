package com.example.tianyiclient.modules;

public enum Category {
    战斗("战斗"),
    移动("移动"),
    渲染("渲染"),
    玩家("玩家"),
    世界("世界"),
    其他("其他");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

