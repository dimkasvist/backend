package ru.dimkasvist.dimkasvist.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import ru.dimkasvist.dimkasvist.config.GoogleProperties;
import ru.dimkasvist.dimkasvist.security.GoogleUserPrincipal;

import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private final GoogleProperties googleProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        String token = extractToken(request);
        
        if (token != null && !token.isEmpty()) {
            try {
                GoogleUserPrincipal principal = verifyIdToken(token);
                
                if (principal == null) {
                    principal = verifyAccessToken(token);
                }
                
                if (principal != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
                    
                    attributes.put("SPRING_SECURITY_CONTEXT", authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.info("WebSocket authentication successful for user: {}", principal.email());
                    return true;
                }
            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage());
                return false;
            }
        }
        
        log.warn("WebSocket handshake without access_token parameter. URI: {}", request.getURI());
        return false;
    }

    private String extractToken(ServerHttpRequest request) {
        try {
            String token = UriComponentsBuilder.fromUri(request.getURI())
                    .build()
                    .getQueryParams()
                    .getFirst("access_token");
            
            if (token != null && !token.isEmpty()) {
                log.info("Token extracted from URI query params, length: {}", token.length());
                return URLDecoder.decode(token, StandardCharsets.UTF_8);
            }
            
            log.info("No access_token in URI query params. Full URI: {}", request.getURI());
        } catch (Exception e) {
            log.error("Error extracting token from URI: {}", e.getMessage());
        }
        
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String paramToken = servletRequest.getServletRequest().getParameter("access_token");
            if (paramToken != null) {
                log.info("Token extracted from ServletRequest parameter");
                return paramToken;
            }
        }
        
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private GoogleUserPrincipal verifyIdToken(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(List.of(googleProperties.getClientId()))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                return new GoogleUserPrincipal(
                        payload.getSubject(),
                        payload.getEmail(),
                        (String) payload.get("name"),
                        (String) payload.get("picture")
                );
            }
        } catch (Exception e) {
            log.debug("ID token verification failed: {}", e.getMessage());
        }
        return null;
    }

    private GoogleUserPrincipal verifyAccessToken(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_USERINFO_URL))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> userInfo = objectMapper.readValue(response.body(), Map.class);
                
                return new GoogleUserPrincipal(
                        (String) userInfo.get("sub"),
                        (String) userInfo.get("email"),
                        (String) userInfo.get("name"),
                        (String) userInfo.get("picture")
                );
            }
        } catch (Exception e) {
            log.error("Error verifying access token: {}", e.getMessage());
        }
        
        return null;
    }
}
