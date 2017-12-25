package com.github.omenstudio.hydra.builder;

import com.github.omenstudio.hydra.utils.HydraUrlResolver;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class VocabBuilder {

    private String apiDoc = null;

    public String buildVocabulary() {
        log.info("#buildVocabulary: started");

        if (apiDoc == null) {
            readMainVocab();
        }

        return apiDoc;
    }


    private void readMainVocab() {

        String readedData = readFileContent("public/vocab/vocab.json")
                .replaceAll("API_ADDR", HydraUrlResolver.getApiAddress())
                .replaceAll("VOCAB_ADDR", HydraUrlResolver.getVocabAddress());

        final JsonParser parser = new JsonParser();

        JsonObject resultJson = parser.parse(readedData).getAsJsonObject();
        JsonArray classes = resultJson.getAsJsonArray("supportedClass");

        log.info("#readMainVocab: vocab parsed as json");

        findFilesInDir("public/vocab/").stream()
                .filter(e -> !e.toString().endsWith("vocab.json"))
                .map(this::readFileContent)
                .map(parser::parse)
                .forEach(classes::add);

        log.info("#readMainVocab: additional files are readed");

        apiDoc = resultJson.toString();
    }


    private String readFileContent(String resourcePath) {
        try {
            return readFileContent(new ClassPathResource(resourcePath).getURI().toURL());
        } catch (IOException e) {
            log.error("#readFileContent: " + e.toString());
            log.error(e.toString());
        }
        return "";
    }

    private List<URL> findFilesInDir(String resourceDir) {
        List<URL> res = new ArrayList<>();

        try {

            ClassLoader cl = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource[] resources = resolver.getResources(resourceDir + "*.json");
            for (Resource resource : resources) {
                res.add(resource.getURL());
            }

        } catch (Exception e) {
            return new ArrayList<>();
        }

        return res;
    }

    private String readFileContent(URL fileUrl) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();

            return out.toString();
        } catch (Exception e) {
            log.error(e.toString());
        }
        return "";
    }


}
