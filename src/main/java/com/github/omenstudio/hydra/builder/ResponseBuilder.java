package com.github.omenstudio.hydra.builder;


import com.github.omenstudio.hydra.utils.HydraUrlResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;



@Component
public class ResponseBuilder {

    private JsonLdBuilder jsonLdBuilder;

    public ResponseBuilder() {
        jsonLdBuilder = new JsonLdBuilder();
    }


    public Object buildResponse(Object objectToReturn) {
        Object response = jsonLdBuilder.buildResponse(objectToReturn);

        if (response == null) {
            return ResponseEntity.notFound()
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Cache-Control", "no-cache")
                    .header("Access-Control-Expose-Headers", "Link")
                    .header("Link", "<" + HydraUrlResolver.getServerAddress() + HydraUrlResolver.getVocabAddress() + ">; " +
                            "rel=\"http://www.w3.org/ns/hydra/core#apiDocumentation\"");
        }

        return ResponseEntity.ok()
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
                .header("Access-Control-Allow-Origin", "*")
                .header("Cache-Control", "no-cache")
                .header("Access-Control-Expose-Headers", "Link")
                .header("Link", "<" + HydraUrlResolver.getServerAddress() + HydraUrlResolver.getVocabAddress() + ">; " +
                        "rel=\"http://www.w3.org/ns/hydra/core#apiDocumentation\"")
                .body(response);
    }
}
