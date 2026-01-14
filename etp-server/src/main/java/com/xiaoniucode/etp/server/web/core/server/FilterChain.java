package com.xiaoniucode.etp.server.web.core.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author liuxin
 */
public class FilterChain {
    private final List<Filter> filters = new ArrayList<>();
    private int index = 0;
    private final RequestContext context;

    public FilterChain(RequestContext context) {
        this.context = context;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
        filters.sort(Comparator.comparingInt(Filter::getOrder));
    }

    public void doFilter() throws Exception {
        if (index < filters.size() && !context.isAborted()) {
            Filter filter = filters.get(index++);
            filter.doFilter(context, this);
        }
    }
}
