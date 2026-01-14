package com.xiaoniucode.etp.server.web.dao;

public enum DaoFactory {
    INSTANCE;
    private final ClientDao clientDao = new ClientDao();
    private final UserDao userDao = new UserDao();
    private final ProxyDao proxyDao = new ProxyDao();
    private final AuthTokenDao authTokenDao = new AuthTokenDao();
    private final SettingDao settingDao = new SettingDao();

    public ClientDao getClientDao() {
        return clientDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public ProxyDao getProxyDao() {
        return proxyDao;
    }

    public AuthTokenDao getAuthTokenDao() {
        return authTokenDao;
    }

    public SettingDao getSettingDao() {
        return settingDao;
    }
}