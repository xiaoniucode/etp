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
import com.xiaoniucode.etp.server.event.ProxyCreateEvent;
import com.xiaoniucode.etp.server.event.ProxyUpdateEvent;
import com.xiaoniucode.etp.server.exceptions.DomainConflictException;
import com.xiaoniucode.etp.server.exceptions.PortConflictException;
import com.xiaoniucode.etp.server.manager.ProxyManager;
import com.xiaoniucode.etp.server.port.PortManager;
import com.xiaoniucode.etp.server.service.DomainConfigService;
import com.xiaoniucode.etp.server.service.ProxyConfigService;
import com.xiaoniucode.etp.server.service.checker.ProxyConfigChecker;
import com.xiaoniucode.etp.server.service.diff.ConfigChangeDetector;
import com.xiaoniucode.etp.server.service.diff.RegisterResult;
import com.xiaoniucode.etp.server.statemachine.agent.*;
import com.xiaoniucode.etp.server.vhost.DomainGenerator;
import com.xiaoniucode.etp.server.vhost.DomainInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
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
    @Autowired
    private ProxyManager proxyManager;
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
    @Resource
    private AppConfig appConfig;
    @Autowired
    private DomainConfigService domainConfigService;

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Channel control = context.getControl();
        try {
            Message.NewProxy proxy = context.getAndRemoveAs(AgentConstants.NEWA_PROXY, Message.NewProxy.class);
            AgentInfo agentInfo = context.getAgentInfo();
            String agentId = agentInfo.getAgentId();

            ProxyConfig newConfig = buildProxyConfig(proxy);
            newConfig.setAgentId(agentId);
            newConfig.setAgentType(context.getAgentInfo().getAgentType());


            ProxyConfig oldConfig = proxyConfigService.findByAgentAndName(agentId, newConfig.getName());
            if (oldConfig == null) {
                logger.debug("代理配置 {} 不存在，准备创建", newConfig.getName());
                newConfig.setProxyId(uidGenerator.getUIDAsString());
                if (newConfig.isTcp()) {
                    Integer remotePort = newConfig.getRemotePort();
                    //如果没有指定远程端口，自动分配一个可用端口
                    if (remotePort == null || remotePort == 0) {
                        int listenPort = portManager.acquire();
                        newConfig.setListenPort(listenPort);
                    } else {
                        //如果指定了远程端口，检查端口是否可用
                        if (!portManager.isAvailable(remotePort)) {
                            throw new PortConflictException(remotePort);
                        }
                        //设置监听端口
                        newConfig.setListenPort(remotePort);
                    }
                    proxyManager.register(newConfig);
                    eventBus.publishAsync(new ProxyCreateEvent(agentInfo, newConfig));

                } else if (newConfig.isHttp()) {
                    RouteConfig routeConfig = newConfig.getRouteConfig();
                    DomainType domainType = routeConfig.getDomainType();
                    List<DomainInfo> subdomains;
                    if (domainType.isCustomDomain()) {
                        routeConfig.getCustomDomains().forEach(domain -> {
                            if (domainConfigService.exists(domain)) {
                                throw new DomainConflictException("域名[" + domain + "]已被占用");
                            }
                        });
                        proxyManager.register(newConfig);
                        eventBus.publishAsync(new ProxyCreateEvent(agentInfo, newConfig));
                    } else {
                        String baseDomain = appConfig.getBaseDomain();
                        subdomains = domainGenerator.generateSubdomain(baseDomain, routeConfig);
                        proxyManager.register(newConfig);
                        eventBus.publishAsync(new ProxyCreateEvent(agentInfo, subdomains, newConfig));
                    }
                }
            } else {
                logger.debug("代理配置 {} 已存在，准备更新", newConfig.getName());
                newConfig.setProxyId(oldConfig.getProxyId());
                if (configChangeDetector.hasChanges(oldConfig, newConfig)) {
                    logger.debug("代理配置 {} 发生变更，准备更新", newConfig.getName());
                    proxyManager.reregister(oldConfig, newConfig);
                    eventBus.publishAsync(new ProxyUpdateEvent(context.getAgentInfo(), newConfig));
                } else {
                    logger.debug("代理配置 {} 没有发生变更，无需更新", newConfig.getName());
                }
            }

            Message.NewProxyResp newProxyResp = buildResponse(registerResult);
            ByteBuf payload = ProtobufUtil.toByteBuf(newProxyResp, control.alloc());
            TMSPFrame frame = new TMSPFrame(0, TMSP.MSG_PROXY_CREATE_RESP, payload);
            control.writeAndFlush(frame).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.debug("代理配置 {} 创建结果发送成功", register.getName());
                } else {
                    logger.error("代理配置 {} 创建失败", register.getName(), future.cause());
                }
            });
            context.fireEvent(AgentEvent.REBUILD_CONTEXT);
            logger.debug("代理注册成功: {}", register);
        } catch (Exception e) {
            logger.error("代理配置注册失败", e);
            sendErrorMessage(e.getMessage(), control);
        }
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

    private Message.NewProxyResp buildResponse(RegisterResult registerResult) {
        ProxyConfig config = registerResult.getProxyConfig();
        ProtocolType protocol = config.getProtocol();
        StringBuilder remoteAddr = new StringBuilder();
        if (protocol.isHttp()) {
            List<DomainInfo> domains = registerResult.getDomainBindings();
            if (domains != null) {
                if (ProtocolType.isHttp(protocol)) {
                    int httpProxyPort = appConfig.getHttpProxyPort();
                    for (DomainInfo domain : domains) {
                        remoteAddr.append("http://").append(domain.getDomain());
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
