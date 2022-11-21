package com.smsc.messenger;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sf.ehcache.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.EnableConfigurationProperties; TODO
//import org.springframework.cloud.context.config.annotation.RefreshScope; TODO
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
//import org.springframework.security.core.context.SecurityContextHolder; TODO

//@RefreshScope TODO
@Configuration
@EnableAsync
@ConditionalOnProperty(name = "messenger.async.enabled", matchIfMissing = true)
//@EnableConfigurationProperties(value = AsyncProperties.class) TODO
@RequiredArgsConstructor
@Getter
public class SchedulerConfiguration {

    @Value("${messenger.smsc.poolSize}")
    private Integer poolSize;

    @Value("${messenger.smsc.maxPoolSize}")
    private Integer maxPoolSize;

    @Value("${messenger.smsc.queueCapacity}")
    private Integer queueCapacity;
    public static final String MESSENGER_MS_EXECUTOR = "MessengerMsExecutor";

    @Bean(name = MESSENGER_MS_EXECUTOR)
    public Executor asyncExecutor() {
        //SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(MESSENGER_MS_EXECUTOR + "-");
        executor.setThreadFactory(new NamedThreadFactory("ParallelSMS"));
        executor.initialize();
        return executor;
    }
}
