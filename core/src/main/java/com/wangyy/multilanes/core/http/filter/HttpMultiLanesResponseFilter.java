package com.wangyy.multilanes.core.http.filter;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * Http返回Header打上featureTag
 *
 */
public class HttpMultiLanesResponseFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader(FTConstants.FEATURE_TAG_PATH, FeatureTagContext.get());
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
