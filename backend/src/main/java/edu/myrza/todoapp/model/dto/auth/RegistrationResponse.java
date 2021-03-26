package edu.myrza.todoapp.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrationResponse {
    private final String username;
    private final String email;
    private final String token;
    private final String rootFolderId;
}
