package org.ikigaidigital.timedeposit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("balance-update")
public record BalanceUpdateProperties(int chunkSize, int threads) {

  public BalanceUpdateProperties {
    if (chunkSize < 1 || threads < 1) {
      throw new IllegalArgumentException("balance-update.chunk-size and balance-update.threads must be >= 1");
    }
  }
}
