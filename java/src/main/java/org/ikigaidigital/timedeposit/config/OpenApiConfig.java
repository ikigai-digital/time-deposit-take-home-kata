package org.ikigaidigital.timedeposit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String TITLE = "XA Bank Time Deposit API";
	private static final String VERSION = "1.0.0";
	private static final String DESCRIPTION = "Lists time deposits and applies the monthly interest run to all of them.";

	@Bean
  OpenAPI timeDepositOpenApi(@Value("${server.servlet.context-path:/}") String contextPath) {
		return new OpenAPI()
				.info(new Info().title(TITLE).version(VERSION).description(DESCRIPTION))
				.servers(List.of(new Server().url(contextPath)));
	}
}
