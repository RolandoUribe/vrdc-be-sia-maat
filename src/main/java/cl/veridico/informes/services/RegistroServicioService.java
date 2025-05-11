package cl.veridico.informes.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cl.veridico.informes.dtos.TransaccionCliente;

@Service
public class RegistroServicioService{
	
	@Value("${vrdc.be.sas.administracion.url}")
	private String VRDC_BE_SAS_ADMINISTRACION_URL;

	public String registrarLogTransaccionCliente(TransaccionCliente request, String userInfo) {
		RestTemplate restTemplate = new RestTemplate();
        // Encabezados
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-apigateway-api-userinfo", userInfo);
        headers.setAccept(List.of(MediaType.ALL));

        HttpEntity<TransaccionCliente> entity = new HttpEntity<>(request, headers);

        // Realizar el POST
        String response = restTemplate.postForObject(
            VRDC_BE_SAS_ADMINISTRACION_URL+"/log-transaccion-cliente",
            entity,
            String.class
        );
       
       return response;
	}

}
