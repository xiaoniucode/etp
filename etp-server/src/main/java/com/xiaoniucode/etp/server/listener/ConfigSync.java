//package com.xiaoniucode.etp.server.listener;
//
//import com.xiaoniucode.etp.core.notify.EventBus;
//import com.xiaoniucode.etp.core.notify.EventListener;
//import com.xiaoniucode.etp.server.config.domain.*;
//import com.xiaoniucode.etp.server.config.AppConfig;
//import com.xiaoniucode.etp.server.config.ConfigHelper;
//import com.xiaoniucode.etp.server.event.TunnelBindEvent;
//import com.xiaoniucode.etp.server.web.common.DigestUtil;
//import com.xiaoniucode.etp.server.web.domain.Config;
//import com.xiaoniucode.etp.server.web.domain.User;
//import com.xiaoniucode.etp.server.web.repository.ConfigsRepository;
//import com.xiaoniucode.etp.server.web.repository.UserRepository;
//import jakarta.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * 数据同步，如果开启了管理界面，则将静态配置同步到数据库
// */
//@Component
//public class ConfigSync implements EventListener<TunnelBindEvent> {
//    private static final Logger logger = LoggerFactory.getLogger(ConfigSync.class);
//    @Autowired
//    private EventBus eventBus;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private ConfigsRepository configsRepository;
//
//    @PostConstruct
//    public void init() {
//        eventBus.register(this);
//    }
//
//    @Override
//    public void onEvent(TunnelBindEvent event) {
//        AppConfig config = ConfigHelper.get();
//        if (config.getDashboard().getEnable()) {
//            syncDashboardUser();
//            syncSystemSettings();
//            logger.debug("数据初始化完毕");
//        }
//    }
//
//    private void syncSystemSettings() {
//        if (ConfigHelper.get().getDashboard().getEnable()) {
//            PortRange range = ConfigHelper.get().getPortRange();
//            // 同步端口范围设置
//            Config portRange = configsRepository.findByKey("port_range");
//            if (portRange == null) {
//                Config config = new Config();
//                config.setValue(range.getStart() + ":" + ConfigHelper.get().getPortRange().getEnd());
//                config.setKey("port_range");
//                configsRepository.save(config);
//                logger.info("同步端口范围配置到数据库");
//            } else {
//                //如果数据库存在配置，采用数据库配置作为全局配置
//                String value = portRange.getValue();
//                String[] split = value.split(":");
//                PortRange configRange = ConfigHelper.get().getPortRange();
//                configRange.setStart(Integer.parseInt(split[0]));
//                configRange.setEnd(Integer.parseInt(split[1]));
//            }
//        }
//    }
//
//    private void syncDashboardUser() {
//        Dashboard dashboard = ConfigHelper.get().getDashboard();
//        Boolean reset = dashboard.getReset();
//        String username = dashboard.getUsername();
//        String password = dashboard.getPassword();
//        User users = new User();
//        users.setUsername(username);
//        users.setPassword(DigestUtil.encode(password, username));
//
//        //没有直接添加用户
//        if (userRepository.findByUsername(username) == null) {
//            userRepository.save(users);
//            logger.info("注册用户 {}", username);
//        }
//        //如果数据库已经存在用户了，如果reset=true则重置
//        if (reset) {
//            //删除所有用户
//            userRepository.deleteAll();
//            //重新注册
//            userRepository.save(users);
//            logger.info("重置面板用户登录信息 {}", username);
//        }
//    }
//}
