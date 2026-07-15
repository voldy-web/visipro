package com.visilog.api.repository;

import com.visilog.api.entity.Appointment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByOrganizationIdOrderByScheduledAtDesc(UUID organizationId);
    Optional<Appointment> findByOrganizationIdAndId(UUID organizationId, UUID id);
    Optional<Appointment> findByOrganizationIdAndNfcCodeIgnoreCase(UUID organizationId, String nfcCode);
    List<Appointment> findByOrganizationIdAndBookedByEmailIgnoreCaseOrderByScheduledAtDesc(UUID organizationId, String email);
}
