package com.github.omenstudio.hydra.utils;

public class HydraUrlResolver {
    private static String applicationAddress = "http://localhost:8080";

    private static String apiPath = "/api";

    private static String vocabPath = "/api/vocab";


    public static String getApplicationAddress() {
        return applicationAddress;
    }

    public static void setApplicationAddress(String applicationAddress) {
        if (applicationAddress.length() <= 0)
            throw new IllegalArgumentException("Application address must be valid");
        HydraUrlResolver.applicationAddress = applicationAddress;
    }

    public static String getApiPath() {
        return apiPath;
    }

    public static String getApiAddress() {
        return applicationAddress + apiPath;
    }

    public static void setApiPath(String apiPath) {
        HydraUrlResolver.apiPath = apiPath;
    }

    public static String getVocabPath() {
        return vocabPath;
    }

    public static String getVocabAddress() {
        return applicationAddress + vocabPath;
    }

    public static void setVocabPath(String vocabPath) {
        HydraUrlResolver.vocabPath = vocabPath;
    }


}
