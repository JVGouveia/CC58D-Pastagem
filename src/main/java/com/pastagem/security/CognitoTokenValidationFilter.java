package com.pastagem.security;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;

@Component
public class CognitoTokenValidationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CognitoTokenValidationFilter.class);

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.url}")
    private String coginitoUrl;

    private final Map<String, PublicKey> publicKeyCache = new java.util.HashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        logger.info("Processando requisição para: {}", path);

        // Ignorar a rota de login
        if (path.equals("/auth/login")) {
            logger.info("Ignorando validação para rota de login");
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        logger.info("Header Authorization: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.error("Header Authorization inválido ou ausente");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
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
                return;
            }

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
            algorithm.verify(jwt);
            logger.info("Token verificado com sucesso");
            
            // Criar autenticação
            String username = jwt.getSubject();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            
            // Definir autenticação no contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Autenticação definida para usuário: {}", username);
            
            request.setAttribute("cognitoUser", jwt);
            filterChain.doFilter(request, response);

        } catch (JWTVerificationException e) {
            logger.error("Token JWT do Cognito inválido: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            logger.error("Erro ao validar token do Cognito: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
} 