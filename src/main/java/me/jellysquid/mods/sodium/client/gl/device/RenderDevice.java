package me.jellysquid.mods.sodium.client.gl.device;

public interface RenderDevice {
    RenderDevice INSTANCE = new GLRenderDevice();

    static void enterManagedCode() {
        RenderDevice.INSTANCE.makeActive();
    }

    static void exitManagedCode() {
        RenderDevice.INSTANCE.makeInactive();
    }

    CommandList createCommandList();

    void makeActive();

    void makeInactive();
}
