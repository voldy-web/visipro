package com.visilog.api.repository;

import com.visilog.api.entity.RoomBooking;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomBookingRepository extends JpaRepository<RoomBooking, UUID> {
    List<RoomBooking> findByOrganizationIdOrderByStartTimeDesc(UUID organizationId);
}
