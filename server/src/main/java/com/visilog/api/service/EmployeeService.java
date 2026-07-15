package com.visilog.api.service;

import com.visilog.api.dto.EmployeeDto;
import com.visilog.api.dto.EmployeeRequest;
import com.visilog.api.entity.Employee;
import com.visilog.api.entity.Role;
import com.visilog.api.exception.ApiException;
import com.visilog.api.repository.EmployeeRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Company Setup > staff roster. This is what AuthService.signup checks
// against — adding someone here with role=RECEPTIONIST/MANAGER/EMPLOYEE
// is what lets them get that role automatically when they sign up with
// a matching email, instead of the old free role-picker.
//
// Note: editing an Employee's role here does NOT retroactively change
// any AppUser who already signed up — role is fixed at signup time by
// design (see AuthService). This only affects people who sign up after
// the change.
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeDto> list(UUID organizationId) {
        return employeeRepository.findByOrganizationId(organizationId).stream()
                .map(EmployeeDto::from)
                .toList();
    }

    @Transactional
    public EmployeeDto create(UUID organizationId, EmployeeRequest req) {
        if (employeeRepository.existsByOrganizationIdAndEmployeeCodeIgnoreCase(organizationId, req.employeeCode())) {
            throw ApiException.conflict("An employee with that code already exists.");
        }
        Employee e = new Employee();
        e.setOrganizationId(organizationId);
        applyRequest(e, req);
        return EmployeeDto.from(employeeRepository.save(e));
    }

    @Transactional
    public EmployeeDto update(UUID organizationId, UUID employeeId, EmployeeRequest req) {
        Employee e = employeeRepository.findByOrganizationIdAndId(organizationId, employeeId)
                .orElseThrow(() -> ApiException.notFound("Employee not found."));
        applyRequest(e, req);
        return EmployeeDto.from(employeeRepository.save(e));
    }

    @Transactional
    public void delete(UUID organizationId, UUID employeeId) {
        Employee e = employeeRepository.findByOrganizationIdAndId(organizationId, employeeId)
                .orElseThrow(() -> ApiException.notFound("Employee not found."));
        employeeRepository.delete(e);
    }

    private void applyRequest(Employee e, EmployeeRequest req) {
        e.setEmployeeCode(req.employeeCode().trim());
        e.setName(req.name().trim());
        e.setDepartment(req.department());
        e.setPhone(req.phone());
        e.setAvaya(req.avaya());
        e.setEmail(req.email().trim().toLowerCase(Locale.ROOT));
        e.setRole(parseRole(req.role()));
    }

    private Role parseRole(String raw) {
        try {
            return Role.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("role must be one of: employee, receptionist, manager, visitor.");
        }
    }
}
