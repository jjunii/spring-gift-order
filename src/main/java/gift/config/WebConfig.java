package gift.config;

import gift.service.MemberService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String ALLOWED_METHOD_NAMES = "GET,POST,PUT,DELETE,PATCH,OPTIONS";

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter(
            JwtProvider jwtProvider,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
            MemberService memberService
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(
                new JwtAuthenticationFilter(jwtProvider, handlerExceptionResolver, memberService));
        registrationBean.addUrlPatterns("/api/wishes/*");
        registrationBean.addUrlPatterns("/api/orders/*");

        return registrationBean;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods(ALLOWED_METHOD_NAMES.split(","))
                .exposedHeaders(HttpHeaders.LOCATION)
                .allowCredentials(true);
    }
}
