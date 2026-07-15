package com.visilog.api.service;

import com.visilog.api.dto.BookRoomRequest;
import com.visilog.api.dto.RoomBookingDto;
import com.visilog.api.entity.RoomBooking;
import com.visilog.api.exception.ApiException;
import com.visilog.api.repository.RoomBookingRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomBookingService {

    private final RoomBookingRepository roomBookingRepository;

    public RoomBookingService(RoomBookingRepository roomBookingRepository) {
        this.roomBookingRepository = roomBookingRepository;
    }

    // readOnly: RoomBookingDto.from reads the lazy participantIds
    // element collection — needs an open session for the whole mapping,
    // not just the initial query.
    @Transactional(readOnly = true)
    public List<RoomBookingDto> list(UUID organizationId) {
        return roomBookingRepository.findByOrganizationIdOrderByStartTimeDesc(organizationId).stream()
                .map(RoomBookingDto::from)
                .toList();
    }

    @Transactional
    public RoomBookingDto book(UUID organizationId, UUID organiserId, BookRoomRequest req) {
        if (organiserId == null) {
            throw ApiException.badRequest("Only staff accounts can book a meeting.");
        }
        if ((req.roomId() == null) == (req.location() == null || req.location().isBlank())) {
            throw ApiException.badRequest("Pick a meeting room or enter an outside location — not both.");
        }
        RoomBooking b = new RoomBooking();
        b.setOrganizationId(organizationId);
        b.setRoomId(req.roomId());
        b.setLocation(req.roomId() == null ? req.location().trim() : null);
        b.setOrganiserId(organiserId);
        b.setTitle(req.title().trim());
        b.setStartTime(req.startTime());
        b.setEndTime(req.endTime());
        if (req.participantIds() != null) {
            b.getParticipantIds().addAll(req.participantIds());
        }
        return RoomBookingDto.from(roomBookingRepository.save(b));
    }
}
