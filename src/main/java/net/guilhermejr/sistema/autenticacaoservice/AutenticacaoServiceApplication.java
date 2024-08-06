package net.guilhermejr.sistema.autenticacaoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
public class AutenticacaoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutenticacaoServiceApplication.class, args);
	}

}
