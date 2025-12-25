package com.example.tianyiclient.managers;

public abstract class Manager {
    private final String name;
    private boolean initialized = false;

    public Manager(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void init() {
        if (!initialized) {
            onInit();
            initialized = true;
        }
    }

    protected abstract void onInit();

    public void shutdown() {
        if (initialized) {
            onShutdown();
            initialized = false;
        }
    }

    protected abstract void onShutdown();
}