package cl.veridico.informes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "cl.veridico.informes")
public class InformesVeridicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(InformesVeridicoApplication.class, args);
	}

}
