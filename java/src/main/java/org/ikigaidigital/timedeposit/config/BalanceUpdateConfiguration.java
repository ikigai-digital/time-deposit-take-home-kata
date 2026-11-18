package org.ikigaidigital.timedeposit.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BalanceUpdateProperties.class)
public class BalanceUpdateConfiguration {

  @Bean(destroyMethod = "shutdown")
  public ExecutorService balanceUpdateChunkExecutor(BalanceUpdateProperties properties) {
    return Executors.newFixedThreadPool(properties.threads(),
        Thread.ofPlatform().name("balance-update-chunk-", 0).factory());
  }
}
