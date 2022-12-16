package net.guilhermejr.sistema.autenticacaoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class AutenticacaoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutenticacaoServiceApplication.class, args);
	}

}
