package com.wangyy.multilanes.core.infra.http.filter;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/*
 * 获取Http请求中的featureTag并将其设置到jvm进程内流量中
 *
 */
public class HttpMultiLanesRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String ft = httpServletRequest.getHeader(FTConstants.FEATURE_TAG_PATH);
        if (ft == null) {
            ft = FeatureTagContext.getDEFAULT();
        }
        FeatureTagContext.set(ft);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
