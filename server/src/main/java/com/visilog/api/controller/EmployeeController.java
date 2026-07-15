package com.visilog.api.controller;

import com.visilog.api.dto.EmployeeDto;
import com.visilog.api.dto.EmployeeRequest;
import com.visilog.api.security.AuthPrincipal;
import com.visilog.api.security.CurrentUser;
import com.visilog.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// The staff directory. Reading it is open to any signed-in user in the
// org (host pickers on Book screens etc. need it); creating/editing/
// deleting entries — including who gets which role — is manager-only.
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> list(@CurrentUser AuthPrincipal me) {
        return ResponseEntity.ok(employeeService.list(me.organizationId()));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<EmployeeDto> create(@CurrentUser AuthPrincipal me, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.create(me.organizationId(), request));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(
            @CurrentUser AuthPrincipal me, @PathVariable UUID id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.update(me.organizationId(), id, request));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@CurrentUser AuthPrincipal me, @PathVariable UUID id) {
        employeeService.delete(me.organizationId(), id);
        return ResponseEntity.noContent().build();
    }
}
