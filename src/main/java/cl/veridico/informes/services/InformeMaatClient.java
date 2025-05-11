package cl.veridico.informes.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class InformeMaatClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${url.sis.maat}")
    private String urlSISMaat;  // Ruta parametrizada desde application.properties

    public String obtenerInforme(String run, String digito, String userInfo, String idConsulta, String nombreInforme) {
        String url = String.format("%s/%s/%s/%s/%s", urlSISMaat, run, digito, idConsulta, nombreInforme);

        try {
            System.out.println("InformeMaatClient->obtenerInforme->InformeMaatClient - userInfo: [" + userInfo + "]");

            // Crear headers y agregar el userInfo
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-apigateway-api-userinfo", userInfo); // Cambia el nombre si el header es distinto

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Llamar al API con los headers
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            //System.out.println("InformeMaatClient - Response: [" + response.getBody() + "]");

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
