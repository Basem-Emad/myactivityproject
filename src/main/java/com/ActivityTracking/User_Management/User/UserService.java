package com.ActivityTracking.User_Management.User;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User_> getUsers(){
        return userRepository.findAll();
    }
    public User_ getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void addNewUser(User_ user) {
        Optional<User_>userByEmail= userRepository.findUserByEmail(user.getEmail());
        if(userByEmail.isPresent())
            throw new IllegalStateException("Email is taken");

        userRepository.save(user);

    }

    public void deleteUser(Integer id) {
        boolean exists=userRepository.existsById(id);
        if(!exists){
            throw new IllegalStateException("User with id"+id+"does not exist");
        }
        userRepository.deleteById(id);
    }
}
