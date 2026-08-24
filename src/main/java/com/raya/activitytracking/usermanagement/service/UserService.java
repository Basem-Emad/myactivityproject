package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.entity.Role_;
import com.raya.activitytracking.usermanagement.entity.User_;
import com.raya.activitytracking.usermanagement.repository.RoleRepository;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User_> getUsers(){
        return userRepository.findAll();
    }
    public User_ getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void addNewUser(User_ user) {
        Optional<User_>userByuserName= userRepository.findByUserName(user.getUserName());
        if(userByuserName.isPresent())
            throw new IllegalStateException("UserName is taken");

        Integer roleId=user.getRole().getId();
        Role_ role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);

        user.setPassword(
                passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

    }

    public void deleteUser(Integer id) {
        boolean exists=userRepository.existsById(id);
        if(!exists){
            throw new IllegalStateException("User with id"+id+"does not exist");
        }
        userRepository.deleteById(id);
    }

    public User_ getUserByUserName(String userName) {

        return userRepository.findByUserName(userName)
                .orElse(null);
    }
    public User_ updateUser(Integer id, User_ updatedUser) {

        User_ existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Integer roleId=updatedUser.getRole().getId();
        Role_ role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));


        existingUser.setUserName(updatedUser.getUserName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
        existingUser.setGender(updatedUser.getGender());
        existingUser.setRole(role);
        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            existingUser.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );
        }
        return userRepository.save(existingUser);
    }
//    public User_ assignRole(Integer userId, Integer roleId) {
//
//        User_ user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Role_ role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new RuntimeException("Role not found"));
//
//        user.setRole(role);
//
//        return userRepository.save(user);
//    }
}
