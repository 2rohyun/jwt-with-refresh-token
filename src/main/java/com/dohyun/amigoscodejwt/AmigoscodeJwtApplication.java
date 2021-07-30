package com.dohyun.amigoscodejwt;

import com.dohyun.amigoscodejwt.domain.AppUser;
import com.dohyun.amigoscodejwt.domain.Role;
import com.dohyun.amigoscodejwt.service.AppUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@SpringBootApplication
public class AmigoscodeJwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmigoscodeJwtApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner run(AppUserService appUserService) {
        return args -> {
            appUserService.saveRole(new Role(null,"ROLE_USER"));
            appUserService.saveRole(new Role(null,"ROLE_MANAGER"));
            appUserService.saveRole(new Role(null,"ROLE_ADMIN"));
            appUserService.saveRole(new Role(null,"ROLE_SUPER_ADMIN"));

            appUserService.saveAppUser(new AppUser(null, "이도현", "도팔","1234", new ArrayList<>()));
            appUserService.saveAppUser(new AppUser(null, "채현욱", "채마스","1234", new ArrayList<>()));
            appUserService.saveAppUser(new AppUser(null, "전종혁", "전마스","1234", new ArrayList<>()));
            appUserService.saveAppUser(new AppUser(null, "홍길동", "홍마스","1234", new ArrayList<>()));

            appUserService.addRoleToAppUser("도팔", "ROLE_USER");
            appUserService.addRoleToAppUser("도팔", "ROLE_ADMIN");
            appUserService.addRoleToAppUser("도팔", "ROLE_SUPER_ADMIN");
            appUserService.addRoleToAppUser("채마스", "ROLE_ADMIN");
            appUserService.addRoleToAppUser("전마스", "ROLE_MANAGER");
            appUserService.addRoleToAppUser("전마스", "ROLE_USER");
            appUserService.addRoleToAppUser("홍마스", "ROLE_USER");

        };
    }
}
