package com.aweni.aweniBack.controller;


import com.aweni.aweniBack.config.KeycloakConfig;
import com.aweni.aweniBack.dto.LoginRequest;
import com.aweni.aweniBack.dto.LoginResponse;
import com.aweni.aweniBack.error.exception.BadRequestException;
import com.aweni.aweniBack.error.exception.NotAuthorizedException;
import com.aweni.aweniBack.model.Demande;
import com.aweni.aweniBack.model.Offre;
import com.aweni.aweniBack.model.Role;
import com.aweni.aweniBack.model.User;
import com.aweni.aweniBack.repository.IDemandeRepository;
import com.aweni.aweniBack.repository.IOffreRepository;
import com.aweni.aweniBack.repository.IRoleRepository;
import com.aweni.aweniBack.repository.IUserRepository;
import com.aweni.aweniBack.utils.RandomUtils;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/keycloak/auth")
public class LoginController {
    private final IUserRepository userRepository;
    private final Keycloak keycloak;
    private final KeycloakConfig keycloakConfig;
    private final IRoleRepository iRoleRepository;
    private final IOffreRepository iOffreRepository;
    private final IDemandeRepository iDemandeRepository;



    @GetMapping("/roles")
    public List<Role> getRoles() {
        log.debug("Request to get all Roles");
        return iRoleRepository.findAll();
    }
    @GetMapping("/echanges")
    public List<Offre> getOffer(@RequestParam String typeoffre) {
        List<Offre> allOffres = iOffreRepository.findAll();
            return filterOffresByType(allOffres, typeoffre);

    }

    private List<Offre> filterOffresByType(List<Offre> offres, String typeoffre) {
        List<Offre> filteredOffres = new ArrayList<>();
        for (Offre offre : offres) {
            if (offre.getType().trim().equalsIgnoreCase(typeoffre.trim())) {
                filteredOffres.add(offre);
            }
        }
        return filteredOffres;
    }
    @DeleteMapping("/echanges/{id}")
    public void deleteOffer(@PathVariable Long id)
    {
        iOffreRepository.deleteById(id);
    }
    @PostMapping("/echanges")
    public ResponseEntity<Offre> createOffer(@RequestBody Offre offre) {
        log.info("REST request to offre {}", offre);
        return ResponseEntity.ok().body( iOffreRepository.save(offre));
    }
    @PostMapping("/demandes")
    public ResponseEntity<Demande> createDemande(@RequestBody Demande demande) {
        log.info("REST request to offre {}", demande);
        return ResponseEntity.ok().body( iDemandeRepository.save(demande));
    }
    @GetMapping("/demandes")
    public List<Demande> getAllDemande() {
        return iDemandeRepository.findAll();
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.debug("REST request to login {}", loginRequest.getUsername());
        User user = null;

        if (!loginRequest.getUsername().equals("insy2s")) {
            user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new NotAuthorizedException("Utilisateur non trouvé avec le nom d'utilisateur"));
        }

        try (
                Keycloak instanceKeycloakUser = keycloakConfig.instantiateKeycloakUser(
                        loginRequest.getUsername(),
                        loginRequest.getPassword())

        ) {

            LoginResponse loginResponse = new LoginResponse();
            AccessTokenResponse accessTokenResponse = instanceKeycloakUser.tokenManager().grantToken();
            loginResponse.setAccess_token(accessTokenResponse.getToken());
            loginResponse.setRefresh_token(accessTokenResponse.getRefreshToken());
            return ResponseEntity.ok(loginResponse);
        }
    }
        @PostMapping("/register")
        public ResponseEntity<Boolean>create(@RequestBody User user)
        {

                log.debug("SERVICE : createUser : {}",user);
    UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setFirstName(user.getFirstname());
        newUser.setLastName(user.getLastname());
        newUser.setEnabled(true);
    CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(user.getPassword());
        newUser.setCredentials(List.of(credentials));

        if(userRepository.findByEmail(user.getEmail()).isPresent())
    {
        throw new BadRequestException("Email déjà existant");
    }
            System.out.println(newUser);
            log.info("SERVICE : createUser : {}",newUser);

    Response response = keycloak.realm(keycloakConfig.getRealm()).users().create(newUser);
            System.out.println(response.getStatus());
        if(response.getStatus()!=201)

    {
        throw new BadRequestException("Erreur lors de la création de l'utilisateur");
    }

    String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
    UserRepresentation createdUser = keycloak
            .realm(keycloakConfig.getRealm())
            .users()
            .get(userId)
            .toRepresentation();

    Collection<Role> roles = user.getRoles();
    // Assign the desired role to the user
        if(!roles.isEmpty())

    {

        for (Role r : roles) {
            RoleRepresentation roleRepresentation = keycloak
                    .realm(keycloakConfig.getRealm())
                    .roles()
                    .get(r.getName())
                    .toRepresentation();
            log.info("SERVICE : roleRepresentation : {}", roleRepresentation);
            keycloak.realm(keycloakConfig.getRealm()).users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(roleRepresentation));
        }
    }


    User userToCreateInLocalDb = User.builder()
            .username(user.getUsername())
            .firstname(user.getFirstname())
            .lastname(user.getLastname())
            .id(createdUser.getId())
            .email(user.getEmail())
            .roles(roles)
            .dateInscription(new Date())
            .build();
    userToCreateInLocalDb=userRepository.save(userToCreateInLocalDb);

        return ResponseEntity.ok().body(true);
}
    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable String userId) {
        log.debug("REST request to logout {}", userId);

        try {
            keycloak.realm(keycloakConfig.getRealm()).users().get(userId).logout();
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion : " + e.getMessage());
        }
        return ResponseEntity.ok(null);
    }

   /**
     * POST /api/keycloak/auth/findAccount/verificationCode : check verification code.
     *
     * @param email the email of the user
     * @param code  the verification code
     * @return the ResponseEntity with status 200 (OK) and the tokens in body,
     * or with status 401 (Unauthorized) if the code or email are incorrect.
     *//*

    @PostMapping("/findAccount/verificationCode")
    public ResponseEntity<String> checkVerificationCode(@RequestParam String email, @RequestParam String code) {
        System.out.println(email+code);
        log.info("REST request to check verification code {} of {}", code, email);
        loginService.checkVerificationCode(email, code);
        return ResponseEntity.ok("Code valide");
    }

    *//*
        TODO: this endpoints is public, should be secured by sending the verification code in the request
        to make sure the user sending the request is the owner of the account,
        and not someone else trying to reset the password.
        Maybe both checkVerificationCode() and resetPassword() should be merged in one endpoint?
     *//*
    *//**
     * POST /api/keycloak/auth/findAccount/restPassword : reset password.
     *
     * @param email    the email of the user
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK) and the tokens in body,
     * or with status 401 (Unauthorized) if the code or email are incorrect
     * or if trying to reset the password of another user,
     * or with status 404 (Not Found) if the user is not found.
     *//*
    @PostMapping("/findAccount/restPassword")
    public ResponseEntity<Void> resetPassword(@RequestParam String email, @RequestParam String password) {
        log.debug("REST request to reset password of {}", email);
        loginService.resetPassword(email, password);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/findAccount/{email}")
    public ResponseEntity<String> findAccount(@PathVariable String email ) {
        return ResponseEntity.status(200).body(loginService.findAccount(email));
    }
    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        log.debug("REST request to change password of {}", request.getUsername());
        final String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        if (!currentUserEmail.equals(request.getUsername())) {
            throw new BadRequestException("Vous ne pouvez pas changer le mot de passe d'un autre utilisateur");
        }
        final String username = request.getUsername();
        final String currentPassword = request.getCurrentPassword();
        final String newPassword = request.getNewPassword();
        loginService.changePassword(username, currentPassword, newPassword);
        return ResponseEntity.ok().build();
    }*/

}
