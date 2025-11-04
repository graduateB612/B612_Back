package com.b612.rose.service.service;

public interface AiEmailGeneratorService {
    boolean isEnabled();

    String generateNpcEmailHtml(String npcName, String userName, String purifiedTypeName, String concern);
}


