package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.service.UserService;
import com.raya.activitytracking.usermanagement.entity.User_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/user")
public class UserController {
private final UserService userService;

@Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping()
    @PreAuthorize("hasAuthority('Read_User')")
    public List<User_>getUsers(){
        return userService.getUsers();
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Read_User')")
    public User_ getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }
    @GetMapping("/getUserByUsername")
    @PreAuthorize("hasAuthority('Read_User')")
    public User_ getUserByName(@RequestParam String userName){
    return userService.getUserByUserName(userName);
    }
    @PostMapping
    @PreAuthorize("hasAuthority('Create_User')")
    public void registerNewUser(@RequestBody User_ user){
        userService.addNewUser(user);
    }
    @DeleteMapping(
            path = "{userId}")
    @PreAuthorize("hasAuthority('Delete_User')")
    public void deleteUser(
            @PathVariable("userId")Integer Id){
            userService.deleteUser(Id);
    }
    @PutMapping("/updateUser/{id}")
    @PreAuthorize("hasAuthority('Update_User')")
    public User_ updateUser(
            @PathVariable Integer id,
            @RequestBody User_ updatedUser) {

        return userService.updateUser(id, updatedUser);
    }
    @GetMapping("/authorities")
    public Object authorities(Authentication authentication ) {
        return authentication.getAuthorities();
    }
}
