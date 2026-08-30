package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.service.UserService;
import com.raya.activitytracking.usermanagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/user")
public class UserController {
private final UserService userService;

@Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    /*@GetMapping()
    @PreAuthorize("hasAuthority('Read_User')")
    public List<User_>getUsers(){
        return userService.getUsers();
    }*/
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Read_User')")
    public User getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }
    @GetMapping("/getUserByUsername")
    @PreAuthorize("hasAuthority('Read_User')")
    public User getUserByName(@RequestParam String userName){
    return userService.getUserByUserName(userName);
    }
    @PostMapping
    @PreAuthorize("hasAuthority('Create_User')")
    public void registerNewUser(@RequestBody User user){
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
    public User updateUser(
            @PathVariable Integer id,
            @RequestBody User updatedUser) {

        return userService.updateUser(id, updatedUser);
    }
    @GetMapping
    @PreAuthorize("hasAuthority('Read_User')")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                userService.getAllUsers(page, size)
        );
    }
    @GetMapping("/authorities")
    public Object authorities(Authentication authentication ) {
        return authentication.getAuthorities();
    }
}
