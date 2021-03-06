package com.dohyun.amigoscodejwt.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dohyun.amigoscodejwt.domain.AppUser;
import com.dohyun.amigoscodejwt.domain.Role;
import com.dohyun.amigoscodejwt.service.AppUserService;
import com.dohyun.amigoscodejwt.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.rmi.server.ExportException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class AppUserController {

    private final AppUserService appUserService;
    private final RedisUtil redisUtil;

    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getUsers() {
        return ResponseEntity.ok().body(appUserService.getUsers());
    }

    @PostMapping("/user/save")
    public ResponseEntity<AppUser> saveUser(@RequestBody AppUser appUser) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
        return ResponseEntity.created(uri).body(appUserService.saveAppUser(appUser));
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
        return ResponseEntity.created(uri).body(appUserService.saveRole(role));
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refreshToken = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refreshToken);
                String username = decodedJWT.getSubject();
                AppUser user = appUserService.getUser(username);

                // ?????? ????????? ?????? refresh token ?????? ????????? ?????? ?????? refresh token ??? ?????????.
                if (redisUtil.getIndexOfList(user.getUsername(), refreshToken) != null && !redisUtil.getRecentDataFromList(user.getUsername()).equals(refreshToken)) {
                    redisUtil.deleteData(user.getUsername());
                    throw new Exception("Invalid Refresh token access, Please Login again!");
                }
                // ???????????? refresh token ??? ?????? ??????, refresh token ??? ???????????? ?????? ??????????????? ????????????.
                else if (redisUtil.getIndexOfList(user.getUsername(), refreshToken) == null) {
                    throw new Exception("This is not refresh token or refresh token doesn't exist!");
                }
                else {
                    String accessToken = JWT.create()
                            .withSubject(user.getUsername())
                            .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                            .withIssuer(request.getRequestURL().toString())
                            .withClaim("roles",user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                            .sign(algorithm);

                    String newRefreshToken = JWT.create()
                            .withSubject(user.getUsername())
                            .withExpiresAt(new Date(System.currentTimeMillis() + 10080 * 60 * 1000))
                            .withIssuer(request.getRequestURL().toString())
                            .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                            .sign(algorithm);

                    redisUtil.addDataFromList(user.getUsername(),newRefreshToken);

                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("access_token",accessToken);
                    tokens.put("refresh_token",newRefreshToken);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), tokens);
                }
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error_message",e.getMessage());

                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String accessToken = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(accessToken);
                String username = decodedJWT.getSubject();

                // redis blacklist ??? ?????? ????????? access ?????? ??????
                redisUtil.setDataExpire(accessToken, "blacklist", 10 * 60);

                // refresh token ??????
                if (redisUtil.getRecentDataFromList(username)!= null) {
                    redisUtil.deleteData(username);
                }
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error_message", e.getMessage());

                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
                throw new RuntimeException("Access token is missing");
            }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testForAccessToken() {
        return ResponseEntity.status(HttpStatus.OK).body("?????? ??????!");
    }
}
