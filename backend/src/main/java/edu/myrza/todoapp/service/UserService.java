package edu.myrza.todoapp.service;

import edu.myrza.todoapp.model.entity.Role;
import edu.myrza.todoapp.model.entity.Status;
import edu.myrza.todoapp.model.entity.User;
import edu.myrza.todoapp.repos.RoleRepository;
import edu.myrza.todoapp.repos.StatusRepository;
import edu.myrza.todoapp.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepo;
    private StatusRepository statusRepo;
    private RoleRepository roleRepository;

    @Autowired
    public UserService(
            UserRepository userRepo,
            StatusRepository statusRepo,
            RoleRepository roleRepository)
    {
        this.statusRepo = statusRepo;
        this.roleRepository = roleRepository;
        this.userRepo = userRepo;
    }

    @Override
    public User loadUserByUsername(String s) throws UsernameNotFoundException {
        return userRepo.findByUsername(s).orElseThrow(() -> new UsernameNotFoundException(s));
    }

    public User createUser(String username, String password, String email) {
        User user = new User();

        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        user.setStatus(statusRepo.findByCode(Status.Code.ENABLED));
        user.setRoles(roleRepository.findByCodeIn(Collections.singleton(Role.Code.ROLE_USER)));

        return userRepo.save(user);
    }

}
