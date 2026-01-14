package com.xiaoniucode.etp.server.web.serivce;

public enum ServiceFactory {
    INSTANCE;
    private final ClientService clientService = new ClientService();
    private final ProxyService proxyService = new ProxyService();
    private final AuthService authTokenService = new AuthService();
    private final SettingService settingService = new SettingService();
    private final StatsService statsService = new StatsService();
    private final UserService userService = new UserService();

    public ClientService getClientService() {
        return clientService;
    }

    public ProxyService getProxyService() {
        return proxyService;
    }

    public AuthService getAuthTokenService() {
        return authTokenService;
    }

    public SettingService getSettingService() {
        return settingService;
    }

    public StatsService getStatsService() {
        return statsService;
    }

    public UserService getUserService() {
        return userService;
    }
}
