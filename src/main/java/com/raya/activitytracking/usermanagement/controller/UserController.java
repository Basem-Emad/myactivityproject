package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.service.UserService;
import com.raya.activitytracking.usermanagement.entity.User_;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<User_>getUsers(){
        return userService.getUsers();
    }
    @GetMapping("/{id}")
    public User_ getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }
    @GetMapping("/getUserByUsername")
    public User_ getUserByName(@RequestParam String userName){
    return userService.getUserByUserName(userName);
    }
    @PostMapping
    public void registerNewUser(@RequestBody User_ user){
        userService.addNewUser(user);
    }
    @DeleteMapping(
            path = "{userId}")
    public void deleteUser(
            @PathVariable("userId")Integer Id){
            userService.deleteUser(Id);
    }
    @PutMapping("/updateUser/{id}")
    public User_ updateUser(
            @PathVariable Integer id,
            @RequestBody User_ updatedUser) {

        return userService.updateUser(id, updatedUser);
    }

}
