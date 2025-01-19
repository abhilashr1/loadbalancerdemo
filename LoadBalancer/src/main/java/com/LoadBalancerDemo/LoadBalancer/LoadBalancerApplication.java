package com.LoadBalancerDemo.LoadBalancer;

import io.netty.channel.ChannelOption;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.List;
import com.LoadBalancerDemo.LoadBalancer.Models.Server;
import com.LoadBalancerDemo.LoadBalancer.Factory.ServerFactory;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class LoadBalancerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoadBalancerApplication.class, args);
	}

	@Bean
	public WebClient webClient() {
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(HttpClient.create()
						.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
						.responseTimeout(Duration.ofSeconds(3))
				))
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public List<Server> servers(ServerFactory serverFactory) {
		return serverFactory.initializeServers();
	}
}
