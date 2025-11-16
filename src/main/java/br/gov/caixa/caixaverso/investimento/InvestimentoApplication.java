package br.gov.caixa.caixaverso.investimento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InvestimentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestimentoApplication.class, args);
        System.out.println("Sistema de investimento iniciou!");
	}

}
