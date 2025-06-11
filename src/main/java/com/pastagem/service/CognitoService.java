package com.pastagem.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.pastagem.model.Usuario;
import com.pastagem.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.regions.Regions;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import jakarta.annotation.PostConstruct;

@Service
public class CognitoService {
    private static final Logger logger = LoggerFactory.getLogger(CognitoService.class);

    private AWSCognitoIdentityProvider cognitoClient;
    private final UsuarioRepository usuarioRepository;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    @Value("${aws.cognito.region:sa-east-1}")
    private String region;

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    public CognitoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostConstruct
    public void init() {
        if (region == null || region.trim().isEmpty()) {
            region = "sa-east-1"; // valor padrão
        }
        
        logger.info("Inicializando cliente Cognito com região: {}", region);
        logger.debug("Access Key: {}", accessKey);
        
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        
        this.cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard()
                .withRegion(Regions.fromName(region.trim()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public boolean userExistsInCognito(String email) {
        try {
            logger.info("Verificando se usuário existe no Cognito: {}", email);
            logger.debug("UserPoolId: {}", userPoolId);
            
            if (userPoolId == null || userPoolId.trim().isEmpty()) {
                logger.error("UserPoolId não configurado");
                throw new RuntimeException("UserPoolId não configurado");
            }

            AdminGetUserRequest request = new AdminGetUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(email);
            
            try {
                cognitoClient.adminGetUser(request);
                logger.info("Usuário encontrado no Cognito: {}", email);
                return true;
            } catch (UserNotFoundException e) {
                logger.info("Usuário não encontrado no Cognito: {}", email);
                return false;
            } catch (Exception e) {
                logger.error("Erro ao verificar usuário no Cognito: {}", e.getMessage(), e);
                throw new RuntimeException("Erro ao verificar usuário no Cognito: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar usuário no Cognito: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar usuário no Cognito: " + e.getMessage(), e);
        }
    }

    public void deleteUserFromCognito(String email) {
        try {
            logger.info("Removendo usuário do Cognito: {}", email);
            AdminDeleteUserRequest request = new AdminDeleteUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(email);
            cognitoClient.adminDeleteUser(request);
            logger.info("Usuário removido com sucesso do Cognito");
        } catch (Exception e) {
            logger.error("Erro ao remover usuário do Cognito: {}", e.getMessage());
            throw new RuntimeException("Erro ao remover usuário do Cognito", e);
        }
    }

    public String registerUserInCognito(String email, String password, String nome) {
        try {
            logger.info("Iniciando registro de usuário no Cognito: {}", email);
            
            // Verificar se o usuário já existe
            if (userExistsInCognito(email)) {
                logger.error("Usuário já existe no Cognito: {}", email);
                throw new UsernameExistsException("Usuário já existe no Cognito");
            }
            
            // Criar atributos do usuário
            List<AttributeType> userAttributes = new ArrayList<>();
            userAttributes.add(new AttributeType().withName("email").withValue(email));
            userAttributes.add(new AttributeType().withName("name").withValue(nome));

            // Criar solicitação de registro
            SignUpRequest signUpRequest = new SignUpRequest()
                    .withClientId(clientId)
                    .withUsername(email)
                    .withPassword(password)
                    .withUserAttributes(userAttributes);

            // Adicionar secret hash se necessário
            if (clientSecret != null && !clientSecret.isEmpty()) {
                String secretHash = calculateSecretHash(email);
                signUpRequest.setSecretHash(secretHash);
                logger.debug("SecretHash calculado: {}", secretHash);
            }

            logger.info("Enviando requisição de registro para o Cognito");
            // Registrar usuário no Cognito
            SignUpResult signUpResult = cognitoClient.signUp(signUpRequest);
            logger.info("Usuário registrado com sucesso no Cognito. UserSub: {}", signUpResult.getUserSub());

            // Confirmar o usuário automaticamente
            confirmUser(email);

            // Retornar o ID do usuário (sub)
            return signUpResult.getUserSub();
        } catch (UsernameExistsException e) {
            logger.error("Usuário já existe no Cognito: {}", email);
            throw new RuntimeException("Usuário já existe no Cognito", e);
        } catch (InvalidPasswordException e) {
            logger.error("Senha inválida para o usuário: {}", email);
            throw new RuntimeException("A senha não atende aos requisitos de segurança", e);
        } catch (Exception e) {
            logger.error("Erro ao registrar usuário no Cognito: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao registrar usuário no Cognito: " + e.getMessage(), e);
        }
    }

    public void confirmUser(String email) {
        try {
            logger.info("Confirmando usuário no Cognito: {}", email);
            
            AdminConfirmSignUpRequest request = new AdminConfirmSignUpRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(email);

            cognitoClient.adminConfirmSignUp(request);
            logger.info("Usuário confirmado com sucesso no Cognito: {}", email);
        } catch (Exception e) {
            logger.error("Erro ao confirmar usuário no Cognito: {}", e.getMessage());
            throw new RuntimeException("Erro ao confirmar usuário no Cognito: " + e.getMessage(), e);
        }
    }

    private String calculateSecretHash(String username) {
        try {
            String message = username + clientId;
            SecretKeySpec signingKey = new SecretKeySpec(
                clientSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            logger.error("Erro ao calcular secret hash: {}", e.getMessage());
            throw new RuntimeException("Erro ao calcular secret hash", e);
        }
    }

    public Usuario getOrCreateUserFromCognito(String cognitoId, String email, String nome) {
        return usuarioRepository.findByCognitoId(cognitoId)
                .orElseGet(() -> {
                    Usuario novoUsuario = new Usuario();
                    novoUsuario.setCognitoId(cognitoId);
                    novoUsuario.setEmail(email);
                    novoUsuario.setNome(nome);
                    return usuarioRepository.save(novoUsuario);
                });
    }

    public String getCognitoIdFromToken(String token) {
        // Implementar a lógica para extrair o cognitoId do token JWT
        // Normalmente está no campo 'sub' do token
        return null; // TODO: Implementar
    }

    public void updateUserEmailInCognito(String oldEmail, String newEmail, String nome) {
        try {
            logger.info("Atualizando email do usuário no Cognito de {} para {}", oldEmail, newEmail);
            
            // Verificar se o novo email já existe
            if (userExistsInCognito(newEmail)) {
                logger.error("Novo email já existe no Cognito: {}", newEmail);
                throw new RuntimeException("Novo email já existe no Cognito");
            }

            // Criar lista de atributos para atualização
            List<AttributeType> userAttributes = new ArrayList<>();
            userAttributes.add(new AttributeType().withName("email").withValue(newEmail));
            userAttributes.add(new AttributeType().withName("name").withValue(nome));

            // Criar requisição de atualização
            AdminUpdateUserAttributesRequest request = new AdminUpdateUserAttributesRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(oldEmail)
                    .withUserAttributes(userAttributes);

            // Executar atualização
            cognitoClient.adminUpdateUserAttributes(request);
            logger.info("Email atualizado com sucesso no Cognito");
        } catch (Exception e) {
            logger.error("Erro ao atualizar email no Cognito: {}", e.getMessage());
            throw new RuntimeException("Erro ao atualizar email no Cognito: " + e.getMessage(), e);
        }
    }
} 