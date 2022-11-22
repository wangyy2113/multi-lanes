package com.wangyy.multilanes.core.http;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.http.filter.HttpMultiLanesRequestFilter;
import com.wangyy.multilanes.core.http.filter.HttpMultiLanesResponseFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnConfig("multi-lanes.enable")
@Configuration
public class HttpMultiLanesIni {

    @Bean
    public HttpMultiLanesRequestFilter httpMultiLanesRequestFilter() {
        return new HttpMultiLanesRequestFilter();
    }

    @Bean
    public HttpMultiLanesResponseFilter httpMultiLanesResponseFilter() {
        return new HttpMultiLanesResponseFilter();
    }
}
