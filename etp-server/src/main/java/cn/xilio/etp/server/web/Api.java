package cn.xilio.etp.server.web;

import cn.xilio.etp.server.web.framework.Filter;
import cn.xilio.etp.server.web.framework.FilterChain;
import cn.xilio.etp.server.web.framework.RequestContext;
import cn.xilio.etp.server.web.framework.Router;

import java.util.List;

/**
 * @author liuxin
 */
public class Api {
    private static String token;

    public static void initFilters(List<Filter> filters) {
        filters.add(new Filter() {
            @Override
            public void doFilter(RequestContext context, FilterChain chain) throws Exception {
                chain.doFilter();
            }

            @Override
            public int getOrder() {
                return 0;
            }
        });

    }

    public static void initRoutes(Router router) {

    }
}
