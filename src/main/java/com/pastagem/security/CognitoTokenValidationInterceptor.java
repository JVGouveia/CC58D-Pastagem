package com.pastagem.security;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CognitoTokenValidationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CognitoTokenValidationInterceptor.class);

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.url}")
    private String coginitoUrl;

    private final Map<String, PublicKey> publicKeyCache = new java.util.HashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    @SuppressWarnings("null")
    private PublicKey getPublicKey(String keyId) {
        logger.info("Buscando chave pública para o kid: {}", keyId);

        if (publicKeyCache.containsKey(keyId)) {
            logger.info("Chave pública encontrada no cache para kid: {}", keyId);
            return publicKeyCache.get(keyId);
        }

        try {
            String jwksUrl = String.format("%s/%s/.well-known/jwks.json", coginitoUrl, userPoolId);
            logger.info("Buscando JWKS em: {}", jwksUrl);
            
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(jwksUrl, JsonNode.class);
            logger.info("Resposta do JWKS: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode keys = response.getBody().get("keys");
                if (keys != null && keys.isArray()) {
                    for (JsonNode key : keys) {
                        if (key.has("kid") && key.get("kid").asText().equals(keyId)) {
                            String nStr = key.get("n").asText();
                            String eStr = key.get("e").asText();

                            byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
                            byte[] eBytes = Base64.getUrlDecoder().decode(eStr);

                            java.security.spec.RSAPublicKeySpec spec = new java.security.spec.RSAPublicKeySpec(
                                    new java.math.BigInteger(1, nBytes), new java.math.BigInteger(1, eBytes));
                            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
                            PublicKey publicKey = keyFactory.generatePublic(spec);
                            publicKeyCache.put(keyId, publicKey);
                            logger.info("Chave pública gerada e armazenada em cache para kid: {}", keyId);
                            return publicKey;
                        }
                    }
                }
            }
            logger.error("Chave pública não encontrada para kid: {}", keyId);

        } catch (Exception e) {
            logger.error("Erro ao buscar chaves públicas do Cognito: {}", e.getMessage(), e);
        }
        return null;
    }

    @SuppressWarnings("null")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String authorizationHeader = request.getHeader("Authorization");
        logger.info("Validando requisição para: {}", request.getRequestURI());

        if (authorizationHeader == null) {
            logger.error("Header Authorization não encontrado");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            logger.error("Header Authorization não começa com 'Bearer '");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authorizationHeader.substring(7);
        logger.info("Token recebido: {}", token.substring(0, 20) + "...");

        try {
            DecodedJWT jwt = JWT.decode(token);
            String keyId = jwt.getHeaderClaim("kid").asString();
            logger.info("Token decodificado, kid: {}", keyId);

            PublicKey publicKey = getPublicKey(keyId);
            if (publicKey == null) {
                logger.error("Chave pública não encontrada para o token");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
            algorithm.verify(jwt);
            logger.info("Token verificado com sucesso");
            
            request.setAttribute("cognitoUser", jwt);
            return true;

        } catch (JWTVerificationException e) {
            logger.error("Token JWT do Cognito inválido: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        } catch (Exception e) {
            logger.error("Erro ao validar token do Cognito: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
    }
} 