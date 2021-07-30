package com.dohyun.amigoscodejwt.service;

import com.dohyun.amigoscodejwt.domain.AppUser;
import com.dohyun.amigoscodejwt.domain.Role;
import com.dohyun.amigoscodejwt.repository.AppUserRepository;
import com.dohyun.amigoscodejwt.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppUserServiceImpl implements AppUserService, UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("No such username in DB"));
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        appUser.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new User(appUser.getUsername(),appUser.getPassword(), authorities);
    }

    @Override
    public AppUser saveAppUser(AppUser appUser) { return appUserRepository.save(appUser); }

    @Override
    public Role saveRole(Role role) { return roleRepository.save(role); }

    @Override
    public void addRoleToAppUser(String username, String roleName) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such username in DB"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NoSuchElementException("No such role name in DB"));
        appUser.getRoles().add(role);
    }

    @Override
    public AppUser getUser(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such username in DB"));
    }

    @Override
    public List<AppUser> getUsers() { return appUserRepository.findAll(); }
}
