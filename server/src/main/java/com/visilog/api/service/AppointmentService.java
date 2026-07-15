package com.visilog.api.service;

import com.visilog.api.dto.AppointmentDto;
import com.visilog.api.dto.BookAppointmentRequest;
import com.visilog.api.dto.RescheduleRequest;
import com.visilog.api.dto.UpdateAppointmentStatusRequest;
import com.visilog.api.entity.Appointment;
import com.visilog.api.entity.AppointmentStatus;
import com.visilog.api.exception.ApiException;
import com.visilog.api.repository.AppointmentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final VisitorService visitorService;

    public AppointmentService(AppointmentRepository appointmentRepository, VisitorService visitorService) {
        this.appointmentRepository = appointmentRepository;
        this.visitorService = visitorService;
    }

    public List<AppointmentDto> list(UUID organizationId) {
        return appointmentRepository.findByOrganizationIdOrderByScheduledAtDesc(organizationId).stream()
                .map(AppointmentDto::from)
                .toList();
    }

    public List<AppointmentDto> listForVisitor(UUID organizationId, String email) {
        return appointmentRepository
                .findByOrganizationIdAndBookedByEmailIgnoreCaseOrderByScheduledAtDesc(organizationId, email).stream()
                .map(AppointmentDto::from)
                .toList();
    }

    public AppointmentDto findByCode(UUID organizationId, String code) {
        Appointment a = appointmentRepository.findByOrganizationIdAndNfcCodeIgnoreCase(organizationId, code)
                .orElseThrow(() -> ApiException.notFound("No booking matches that code."));
        return AppointmentDto.from(a);
    }

    @Transactional
    public AppointmentDto book(UUID organizationId, BookAppointmentRequest req, String bookedByEmail) {
        Appointment a = new Appointment();
        a.setOrganizationId(organizationId);
        a.setVisitorName(req.visitorName().trim());
        a.setVisitorPhone(req.visitorPhone().trim());
        a.setVisitorCompany(req.visitorCompany());
        a.setPurpose(req.purpose());
        a.setHostId(req.hostId());
        a.setScheduledAt(req.scheduledAt() != null ? req.scheduledAt() : Instant.now());
        a.setStatus(AppointmentStatus.PENDING);
        a.setNfcCode(uniqueNfcCode(organizationId));
        a.setBookedByEmail(bookedByEmail);
        return AppointmentDto.from(appointmentRepository.save(a));
    }

    @Transactional
    public AppointmentDto updateStatus(UUID organizationId, UUID appointmentId, UpdateAppointmentStatusRequest req) {
        Appointment a = findOrThrow(organizationId, appointmentId);
        a.setStatus(parseStatus(req.status()));
        return AppointmentDto.from(appointmentRepository.save(a));
    }

    // Admitting = approve the pending request AND check the visitor in,
    // in one step, so reception doesn't repeat the visitor's details.
    @Transactional
    public AppointmentDto admit(UUID organizationId, UUID appointmentId) {
        Appointment a = findOrThrow(organizationId, appointmentId);
        a.setStatus(AppointmentStatus.ADMITTED);
        appointmentRepository.save(a);

        String[] parts = (a.getVisitorName() == null ? "" : a.getVisitorName()).trim().split(" ", 2);
        String firstName = parts.length > 0 ? parts[0] : "";
        String lastName = parts.length > 1 ? parts[1] : "";
        visitorService.registerAndCheckInInternal(
                organizationId, firstName, lastName, a.getVisitorPhone(), a.getVisitorCompany(),
                a.getPurpose(), a.getHostId());

        return AppointmentDto.from(a);
    }

    @Transactional
    public AppointmentDto reschedule(UUID organizationId, UUID appointmentId, RescheduleRequest req) {
        Appointment a = findOrThrow(organizationId, appointmentId);
        a.setScheduledAt(req.newScheduledAt());
        a.setRescheduleReason(req.reason());
        a.setRescheduledAt(Instant.now());
        return AppointmentDto.from(appointmentRepository.save(a));
    }

    private Appointment findOrThrow(UUID organizationId, UUID appointmentId) {
        return appointmentRepository.findByOrganizationIdAndId(organizationId, appointmentId)
                .orElseThrow(() -> ApiException.notFound("Appointment not found."));
    }

    private AppointmentStatus parseStatus(String raw) {
        try {
            return AppointmentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("status must be one of: pending, admitted, rejected.");
        }
    }

    private String uniqueNfcCode(UUID organizationId) {
        String code;
        do {
            code = CodeGenerator.generateVisitorCode();
        } while (appointmentRepository.findByOrganizationIdAndNfcCodeIgnoreCase(organizationId, code).isPresent());
        return code;
    }
}
