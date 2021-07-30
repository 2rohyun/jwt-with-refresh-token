package com.dohyun.amigoscodejwt.service;

import com.dohyun.amigoscodejwt.domain.AppUser;
import com.dohyun.amigoscodejwt.domain.Role;

import java.util.List;

public interface AppUserService {

    AppUser saveAppUser(AppUser appUser);
    Role saveRole(Role role);
    void addRoleToAppUser(String username, String roleName);
    AppUser getUser(String username);
    List<AppUser> getUsers();
}
