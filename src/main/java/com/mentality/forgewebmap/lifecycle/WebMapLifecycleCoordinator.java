package com.mentality.forgewebmap.lifecycle;

/** Production owner for enable/disable and network-recreation lifecycle work. */
public final class WebMapLifecycleCoordinator {
    public interface RenderService { void setServer(Object server); void start(); void stop(); boolean isRunning(); }
    public interface WebService { void setServer(Object server); void start() throws Exception; void stop(); boolean isRunning(); }
    public interface WebFactory { WebService create(); }
    public interface Settings { boolean enabled(); String bindAddress(); int port(); }
    private final Settings settings; private final RenderService render; private final WebFactory webFactory;
    private WebService web; private Object server;
    public WebMapLifecycleCoordinator(Settings settings, RenderService render, WebFactory webFactory) { this.settings=settings; this.render=render; this.webFactory=webFactory; }
    public void setServer(Object server) { this.server=server; }
    public Object server() { return server; }
    public WebService webService() { return web; }
    public boolean isEnabledAndRunning() { return settings.enabled() && render.isRunning() && web != null && web.isRunning(); }
    public void startOrStop() throws Exception { if (!settings.enabled()) { stop(); return; } if (server == null) return; render.setServer(server); if (!render.isRunning()) render.start(); if (web == null || !web.isRunning()) startWebService(); }
    public void reload(String oldBind, int oldPort) throws Exception { startOrStop(); if (settings.enabled() && web != null && web.isRunning() && (!settings.bindAddress().equals(oldBind) || settings.port()!=oldPort)) { web.stop(); web=null; startWebService(); } }
    public void stop() { if (render.isRunning()) render.stop(); if (web != null) { web.stop(); web=null; } }
    private void startWebService() throws Exception { WebService replacement=webFactory.create(); replacement.setServer(server); replacement.start(); web=replacement; }
}
