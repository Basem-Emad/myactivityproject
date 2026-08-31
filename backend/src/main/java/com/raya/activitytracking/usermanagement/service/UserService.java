package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.dto.request.UserRequest;
import com.raya.activitytracking.usermanagement.entity.Role;
import com.raya.activitytracking.usermanagement.entity.User;
import com.raya.activitytracking.usermanagement.repository.RoleRepository;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;



   public List<User> getUsers(){
        return userRepository.findAll();
    }
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void addNewUser(UserRequest request) {
        Optional<User>userByuserName= userRepository.findByUserName(request.getUserName());
        if(userByuserName.isPresent())
            throw new IllegalStateException("UserName is taken");

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));


        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

    }

    public void deleteUser(Long id) {
        boolean exists=userRepository.existsById(id);
        if(!exists){
            throw new IllegalStateException("User with id " + id + " does not exist");
        }
        userRepository.deleteById(id);
    }

    public User getUserByUserName(String userName) {

        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + userName));
    }
    public User updateUser(Long id, UserRequest updatedUser) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long roleId=updatedUser.getRoleId();
        Role role = roleRepository.findById(roleId)
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
    public Page<User> getAllUsers(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size
        );

        return userRepository.findAll(pageable);
    }

}
