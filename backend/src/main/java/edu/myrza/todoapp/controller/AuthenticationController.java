package edu.myrza.todoapp.controller;

import edu.myrza.todoapp.exceptions.BussinesException;
import edu.myrza.todoapp.model.dto.*;
import edu.myrza.todoapp.model.entity.Folder;
import edu.myrza.todoapp.model.entity.User;
import edu.myrza.todoapp.service.FolderService;
import edu.myrza.todoapp.service.UserService;
import edu.myrza.todoapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final FolderService folderService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthenticationController(
            AuthenticationManager authenticationManager,
            UserService userService,
            FolderService folderService,
            JwtUtil jwtUtil)
    {
        this.folderService = folderService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException ex) {
            throw new BussinesException(BussinesException.Code.AUTH_001);
        }

        User user = userService.loadUserByUsername(username);

        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(username, user.getEmail(), token, username));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        //Here we should save token into 'black list' table
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(@RequestBody RegistrationRequest req) {

        String username = req.getUsername();
        String password = req.getPassword();
        String email = req.getEmail();

        // Create user
        User user = userService.createUser(username, password, email);

        // Create root folder for a user
        Folder root = folderService.prepareUserRootFolder(user);

        // If the execution reached here then everything went fine
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new RegistrationResponse(username, email, token, root.getId()));
    }
}
