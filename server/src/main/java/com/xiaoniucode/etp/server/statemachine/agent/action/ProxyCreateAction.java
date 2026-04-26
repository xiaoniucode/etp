package com.xiaoniucode.etp.server.statemachine.agent.action;

import com.baidu.fsg.uid.UidGenerator;
import com.google.protobuf.ProtocolStringList;
import com.xiaoniucode.etp.core.domain.*;
import com.xiaoniucode.etp.core.enums.*;
import com.xiaoniucode.etp.core.message.Message;
import com.xiaoniucode.etp.core.message.TMSP;
import com.xiaoniucode.etp.core.message.TMSPFrame;
import com.xiaoniucode.etp.core.notify.EventBus;
import com.xiaoniucode.etp.core.utils.ProtobufUtil;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.service.DomainConfigService;
import com.xiaoniucode.etp.server.service.ProxyConfigService;
import com.xiaoniucode.etp.server.service.diff.ConfigChangeDetector;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import com.xiaoniucode.etp.server.vhost.DomainGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProxyCreateAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(ProxyCreateAction.class);
    @Resource
    private AppConfig appConfig;
    @Autowired
    private UidGenerator uidGenerator;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private ConfigChangeDetector configChangeDetector;
    @Autowired
    private ProxyConfigService proxyConfigService;
    @Autowired
    private DomainGenerator domainGenerator;
    @Autowired
    private PortManager portManager;
    @Autowired
    private DomainConfigService domainConfigService;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
//        Channel control = context.getControl();
//        try {
//            Message.NewProxy proxy = context.getAndRemoveAs(AgentConstants.NEWA_PROXY, Message.NewProxy.class);
//            AgentInfo agentInfo = context.getAgentInfo();
//            String agentId = agentInfo.getAgentId();
//
//            ProxyConfig newConfig = buildProxyConfig(proxy);
//            newConfig.setAgentId(agentId);
//            newConfig.setAgentType(context.getAgentInfo().getAgentType());
//
//            List<String>domains=null;
//            ProxyConfig oldConfig = proxyConfigService.findByAgentAndName(agentId, newConfig.getName());
//            if (oldConfig == null) {
//                logger.debug("代理配置 {} 不存在，准备创建", newConfig.getName());
//                newConfig.setProxyId(uidGenerator.getUIDAsString());
//                if (newConfig.isTcp()) {
//                    Integer remotePort = newConfig.getRemotePort();
//                    //如果没有指定远程端口，自动分配一个可用端口
//                    if (remotePort == null || remotePort == 0) {
//                        int listenPort = portManager.acquire();
//                        newConfig.setListenPort(listenPort);
//                    } else {
//                        //如果指定了远程端口，检查端口是否可用
//                        if (!portManager.isAvailable(remotePort)) {
//                            throw new PortConflictException(remotePort);
//                        }
//                        //设置监听端口
//                        newConfig.setListenPort(remotePort);
//                    }
//                    proxyManager.register(newConfig);
//                    eventBus.publishAsync(new ProxyCreateEvent(agentInfo, newConfig));
//
//                } else if (newConfig.isHttp()) {
//                    domains=new ArrayList<>();
//
//                    RouteConfig routeConfig = newConfig.getRouteConfig();
//                    DomainType domainType = routeConfig.getDomainType();
//                    if (domainType.isCustomDomain()) {
//                        routeConfig.getCustomDomains().forEach(domain -> {
//                            if (domainConfigService.exists(domain)) {
//                                throw new DomainConflictException("域名[" + domain + "]已被占用");
//                            }
//                        });
//                        proxyManager.register(newConfig);
//                        eventBus.publishAsync(new ProxyCreateEvent(agentInfo, newConfig));
//                    } else if (domainType.isAuto()) {
//                        String baseDomain = appConfig.getBaseDomain();
//                        List<String> autoSubdomains = domainGenerator.generateSubdomain(baseDomain, routeConfig);
//                    }else {
//                        String baseDomain = appConfig.getBaseDomain();
//                        Set<String> subDomains = routeConfig.getSubDomains();
//                        proxyManager.register(newConfig);
//                      //  eventBus.publishAsync(new ProxyCreateEvent(agentInfo, subdomains, newConfig));
//                    }
//                }
//            } else {
//                logger.debug("代理配置 {} 已存在，准备更新", newConfig.getName());
//                newConfig.setProxyId(oldConfig.getProxyId());
//                if (configChangeDetector.hasChanges(oldConfig, newConfig)) {
//                    logger.debug("代理配置 {} 发生变更，准备更新", newConfig.getName());
//
//                    ProtocolType newProtocol = newConfig.getProtocol();
//                    ProtocolType oldProtocol = oldConfig.getProtocol();
//                    //如果协议发生改变
//                    if (newProtocol != oldProtocol) {
//                        proxyManager.unregister(oldConfig.getProxyId());
//                    }
//
//                    Integer oldRemotePort = oldConfig.getRemotePort();
//                    Integer newRemotePort = newConfig.getRemotePort();
//                    if (newConfig.isTcp() && !newRemotePort.equals(oldRemotePort)) {
//                        if (!portManager.isAvailable(newRemotePort)) {
//                            throw new PortConflictException(newRemotePort);
//                        }
//                        //释放旧端口
//                        proxyManager.unregister(oldConfig.getProxyId());
//                        //设置监听端口为新远程端口
//                        newConfig.setListenPort(newRemotePort);
//                        proxyManager.register(newConfig);
//                    } else {
//                        newConfig.setListenPort(oldConfig.getListenPort());
//                    }
//                    if (oldProtocol.isHttp()&&newProtocol.isTcp()&&(newRemotePort==null||newRemotePort==0)){
//                        //如果原来是HTTP代理，新的TCP代理没有指定远程端口，自动分配一个可用端口
//                        int listenPort = portManager.acquire();
//                        newConfig.setListenPort(listenPort);
//                    }
//
//
//
//                    proxyManager.reregister(oldConfig, newConfig);
//                    eventBus.publishAsync(new ProxyUpdateEvent(context.getAgentInfo(), newConfig));
//                } else {
//                    logger.debug("代理配置 {} 没有发生变更，无需更新", newConfig.getName());
//                }
//            }
//
//            Message.NewProxyResp newProxyResp = buildResponse(newConfig,new ArrayList<>());
//            ByteBuf payload = ProtobufUtil.toByteBuf(newProxyResp, control.alloc());
//            TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_PROXY_CREATE_RESP, payload);
//            control.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {
//                if (future.isSuccess()) {
//                    logger.debug("代理配置 {} 创建结果发送成功", newConfig.getName());
//                } else {
//                    logger.error("代理配置 {} 创建失败", newConfig.getName(), future.cause());
//                }
//            });
//            context.fireEvent(AgentEvent.REBUILD_CONTEXT);
//            logger.debug("代理注册成功: {}", newConfig);
//        } catch (Exception e) {
//            logger.error("代理配置注册失败", e);
//            sendErrorMessage(e.getMessage(), control);
//        }
    }

    public void sendErrorMessage(String message, Channel control) {
        Message.Error msg = Message.Error.newBuilder().setMessage(message).build();
        ByteBuf payload = ProtobufUtil.toByteBuf(msg, control.alloc());
        TMSPFrame frame = new TMSPFrame(TMSP.MSG_ERROR, payload);
        control.writeAndFlush(frame);
    }

    private ProxyConfig buildProxyConfig(Message.NewProxy proxy) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setSourceType(ProxySourceType.AGENT);
        proxyConfig.setName(proxy.getName());
        List<Target> targets = proxy.getTargetsList().stream().map(p -> {
            Target target = new Target();
            target.setHost(p.getHost());
            target.setPort(p.getPort());
            if (p.hasName()) {
                target.setName(p.getName());
            }
            if (p.hasWeight()) {
                target.setWeight(p.getWeight());
            }
            return target;
        }).toList();
        proxyConfig.addTargets(targets);
        if (proxy.hasRemotePort()) {
            proxyConfig.setRemotePort(proxy.getRemotePort());
        }
        proxyConfig.setProtocol(ProtocolType.getByName(proxy.getProtocol().name()));
        if (proxy.hasEnable() && proxy.getEnable()) {
            proxyConfig.setStatus(ProxyStatus.OPEN);
        }
        if (proxy.hasDomain()) {
            Message.DomainInfo domainInfo = proxy.getDomain();
            RouteConfig routeConfig = new RouteConfig();
            if (domainInfo.hasAutoDomain()) {
                routeConfig.setAutoDomain(domainInfo.getAutoDomain());
            }
            ProtocolStringList customDomainsList = domainInfo.getCustomDomainsList();
            if (!customDomainsList.isEmpty()) {
                routeConfig.getCustomDomains().addAll(customDomainsList);
            }
            ProtocolStringList subDomainsList = domainInfo.getSubDomainsList();
            if (!subDomainsList.isEmpty()) {
                routeConfig.getSubDomains().addAll(subDomainsList);
            }
            proxyConfig.setRouteConfig(routeConfig);
        }
        if (proxy.hasTransport()) {
            TransportCustomConfig transportCustomConfig = new TransportCustomConfig();
            Message.Transport transport = proxy.getTransport();
            if (transport.hasMux()) {
                transportCustomConfig.setMultiplex(transport.getMux());
            }
            if (transport.hasCompress()) {
                transportCustomConfig.setCompress(transport.getCompress());
            }
            if (transport.hasEncrypt()) {
                transportCustomConfig.setEncrypt(transport.getEncrypt());
            }
            proxyConfig.setTransport(transportCustomConfig);
        }

        if (proxy.hasAccessControl()) {
            Message.AccessControl accessControl = proxy.getAccessControl();
            boolean enable = accessControl.getEnable();
            AccessControl accessControlMode = AccessControl.fromValue(accessControl.getMode().name());
            Set<String> allow = new HashSet<>(accessControl.getAllowList());
            Set<String> deny = new HashSet<>(accessControl.getDenyList());
            proxyConfig.setAccessControl(new AccessControlConfig(enable, accessControlMode, allow, deny));
        }
        if (proxyConfig.getProtocol().isHttp() && proxy.hasBasicAuth()) {
            Message.BasicAuth basicAuth = proxy.getBasicAuth();
            Set<HttpUser> users = basicAuth.getHttpUsersList().stream()
                    .map(httpUser -> new HttpUser(httpUser.getUser(), passwordEncoder.encode(httpUser.getPass())))
                    .collect(Collectors.toSet());

            BasicAuthConfig basicAuthConfig = proxyConfig.getOrCreateBasicAuthConfig();
            basicAuthConfig.setEnabled(basicAuth.getEnable());
            basicAuthConfig.addUsers(users);
            proxyConfig.setBasicAuth(basicAuthConfig);
        }
        if (proxy.hasBandwidth()) {
            Message.Bandwidth bandwidth = proxy.getBandwidth();
            BandwidthConfig bandwidthConfig = new BandwidthConfig(bandwidth.getLimit(),
                    bandwidth.getLimitIn(),
                    bandwidth.getLimitOut());
            proxyConfig.setBandwidth(bandwidthConfig);
        }
        if (proxy.hasLoadBalance()) {
            LoadBalanceConfig loadBalanceConfig = new LoadBalanceConfig();
            Message.LoadBalance loadBalance = proxy.getLoadBalance();
            if (loadBalance.hasStrategy()) {
                loadBalanceConfig.setStrategy(toJavaType(loadBalance.getStrategy()));
            }
            proxyConfig.setLoadBalance(loadBalanceConfig);
        }
        return proxyConfig;
    }

    private LoadBalanceType toJavaType(Message.LoadBalanceStrategy strategy) {
        if (strategy == null) {
            return LoadBalanceConfig.DEFAULT_STRATEGY;
        }
        return switch (strategy) {
            case WEIGHT -> LoadBalanceType.WEIGHT;
            case RANDOM -> LoadBalanceType.RANDOM;
            case LEAST_CONN -> LoadBalanceType.LEAST_CONN;
            default -> LoadBalanceConfig.DEFAULT_STRATEGY;
        };
    }

    private Message.NewProxyResp buildResponse(ProxyConfig config,List<String>domains) {

        ProtocolType protocol = config.getProtocol();
        StringBuilder remoteAddr = new StringBuilder();
        if (protocol.isHttp()) {
            if (domains != null) {
                if (ProtocolType.isHttp(protocol)) {
                    int httpProxyPort = appConfig.getHttpProxyPort();
                    for (String domain : domains) {
                        remoteAddr.append("http://").append(domain);
                        if (httpProxyPort != 80) {
                            remoteAddr.append(":").append(httpProxyPort);
                        }
                        remoteAddr.append("\n");
                    }
                }
            }

        } else if (protocol.isTcp()) {
            String serverAddr = appConfig.getServerAddr();
            Integer listenPort = config.getListenPort();
            remoteAddr.append(serverAddr).append(":").append(listenPort);
        }
        return Message.NewProxyResp.newBuilder()
                .setProxyId(config.getProxyId())
                .setProxyName(config.getName())
                .setRemoteAddr(remoteAddr.toString()).build();
    }
}
