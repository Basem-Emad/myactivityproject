package com.ActivityTracking.User_Management.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
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

}
