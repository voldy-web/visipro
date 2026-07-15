package com.visilog.api.service;

import com.visilog.api.dto.MeetingRoomDto;
import com.visilog.api.dto.MeetingRoomRequest;
import com.visilog.api.entity.MeetingRoom;
import com.visilog.api.exception.ApiException;
import com.visilog.api.repository.MeetingRoomRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeetingRoomService {

    private final MeetingRoomRepository meetingRoomRepository;

    public MeetingRoomService(MeetingRoomRepository meetingRoomRepository) {
        this.meetingRoomRepository = meetingRoomRepository;
    }

    public List<MeetingRoomDto> list(UUID organizationId) {
        return meetingRoomRepository.findByOrganizationId(organizationId).stream()
                .map(MeetingRoomDto::from)
                .toList();
    }

    @Transactional
    public MeetingRoomDto create(UUID organizationId, MeetingRoomRequest req) {
        MeetingRoom r = new MeetingRoom();
        r.setOrganizationId(organizationId);
        applyRequest(r, req);
        return MeetingRoomDto.from(meetingRoomRepository.save(r));
    }

    @Transactional
    public MeetingRoomDto update(UUID organizationId, UUID roomId, MeetingRoomRequest req) {
        MeetingRoom r = meetingRoomRepository.findByOrganizationIdAndId(organizationId, roomId)
                .orElseThrow(() -> ApiException.notFound("Meeting room not found."));
        applyRequest(r, req);
        return MeetingRoomDto.from(meetingRoomRepository.save(r));
    }

    @Transactional
    public void delete(UUID organizationId, UUID roomId) {
        MeetingRoom r = meetingRoomRepository.findByOrganizationIdAndId(organizationId, roomId)
                .orElseThrow(() -> ApiException.notFound("Meeting room not found."));
        meetingRoomRepository.delete(r);
    }

    private void applyRequest(MeetingRoom r, MeetingRoomRequest req) {
        r.setName(req.name().trim());
        r.setCapacity(req.capacity());
        r.setFloor(req.floor());
    }
}
