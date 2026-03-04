package com.xiaoniucode.etp.server.statemachine.tunnel;

import com.xiaoniucode.etp.core.statemachine.TunnelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TunnelManager {
    @Autowired
    private DirectTunnelPoolManager directTunnelPoolManager;
    @Autowired
    private MuxTunnelManager muxTunnelManager;
    private Map<String, TunnelContext> contexts = new ConcurrentHashMap<>();

    public TunnelContext registerContext(TunnelContext context) {
        boolean mux = context.isMux();
        TunnelType tunnelType = mux ? TunnelType.MUX : TunnelType.DIRECT;
        context.setTunnelType(tunnelType);
        if (mux) {
            return muxTunnelManager.register(context);
        } else {
            return directTunnelPoolManager.register(context);
        }
    }
    public Optional<TunnelContext> getTunnel(boolean mux,  String tunnelId) {
        if (mux){
           return Optional.empty();
        }else {
          return Optional.ofNullable(directTunnelPoolManager.borrow( tunnelId)) ;
        }
    }
}
