package com.web.appts.controllers;

import com.web.appts.DTO.JwtAuthRequest;
import com.web.appts.DTO.JwtAuthResponse;
import com.web.appts.DTO.UserDto;
import com.web.appts.configurations.JwtTokenHelper;
import com.web.appts.entities.Department;
import com.web.appts.entities.User;
import com.web.appts.exceptions.ApiException;
import com.web.appts.repositories.UserRepo;
import com.web.appts.services.OrderMaintenanceService;
import com.web.appts.services.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@RestController
@RequestMapping({"/api/v1/auth/"})
public class AuthController {
    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OrderMaintenanceService orderMaintenanceService;

    @Autowired
    private ModelMapper mapper;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping({"/login"})
    public ResponseEntity<JwtAuthResponse> createToken(@Valid @RequestBody JwtAuthRequest request) throws Exception {
        authenticate(request.getUsername(), request.getPassword());

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getUsername());

        String token = this.jwtTokenHelper.generateToken(userDetails);

        JwtAuthResponse response = new JwtAuthResponse();
        response.setToken(token);
        response.setUser((UserDto)this.mapper.map(userDetails, UserDto.class));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void authenticate(String username, String password) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        try {
            this.authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            logger.warn("Invalid authentication attempt for username={}", username);
            throw new ApiException("Invalid Username or Password");
        }
    }

    @PostMapping({"/register"})
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
        Department department = null;
        if (userDto.getDepId() < 1) {
            int retrievedValue = -1;

            Iterator<Department> iterator = userDto.getDepartmentsSet().iterator();
            if (iterator.hasNext()) {
                retrievedValue = iterator.next().getId();
                department = this.userService.getDepartmentById(retrievedValue);
            }
        } else {
            department = this.userService.getDepartmentById(userDto.getDepId());
        }






















        Set<Department> dep = new HashSet<>();
        dep.add(department);
        userDto.setDepartmentsSet(dep);

        UserDto registeredUser = this.userService.registerNewUser(userDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @GetMapping({"/current-user/"})
    public ResponseEntity<UserDto> getUser(Principal principal) {
        User user = this.userRepo.findByEmail(principal.getName()).get();
        return new ResponseEntity<>(this.mapper.map(user, UserDto.class), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"/removing-same-orders"})
    public ResponseEntity<Integer> removeArchiveAndMainOrdersLegacy() {
        return removeArchiveAndMainOrders();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/maintenance/removing-same-orders"})
    public ResponseEntity<Integer> removeArchiveAndMainOrders() {
        this.orderMaintenanceService.removeArchiveAndMainOrders();
        return ResponseEntity.ok(1);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"/mon-sub-orders"})
    public ResponseEntity<Integer> getMonSubLegacy() {
        return getMonSub();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/maintenance/mon-sub-orders"})
    public ResponseEntity<Integer> getMonSub() {
        this.orderMaintenanceService.createMonSubOrders();
        return ResponseEntity.ok(1);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"/update-tekst"})
    public ResponseEntity<String> updateTextLegacy() {
        return orderTextRefreshUnavailable();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/maintenance/update-tekst"})
    public ResponseEntity<String> updateText() {
        return orderTextRefreshUnavailable();
    }

    private ResponseEntity<String> orderTextRefreshUnavailable() {
        return ResponseEntity.status(HttpStatus.GONE)
                .body("Legacy order-text refresh is no longer supported. Product-note synchronization remains part of regular order refresh.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"/adjust-parent-orders"})
    public ResponseEntity<Integer> adjustingParentsLegacy() {
        return adjustingParents();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/maintenance/adjust-parent-orders"})
    public ResponseEntity<Integer> adjustingParents() {
        this.orderMaintenanceService.adjustParentOrders();
        return ResponseEntity.ok(1);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"/mark-orders-expired"})
    public ResponseEntity<Integer> markOrdersExpiredLegacy() {
        return markOrdersExpired();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/maintenance/mark-orders-expired"})
    public ResponseEntity<Integer> markOrdersExpired() {
        this.orderMaintenanceService.markOrdersExpired();
        return ResponseEntity.ok(1);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"/check-deleted-orders"})
    public ResponseEntity<Integer> checkDeletedOrdersLegacy() {
        return checkDeletedOrders();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/maintenance/check-deleted-orders"})
    public ResponseEntity<Integer> checkDeletedOrders() {
        this.orderMaintenanceService.checkDeletedOrders();
        return ResponseEntity.ok(1);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"/verify-crm-orders"})
    public ResponseEntity<Integer> verifyCRMOrdersLegacy() {
        return verifyCRMOrders();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/maintenance/verify-crm-orders"})
    public ResponseEntity<Integer> verifyCRMOrders() {
        this.orderMaintenanceService.verifyCrmOrders();
        return ResponseEntity.ok(1);
    }

}
