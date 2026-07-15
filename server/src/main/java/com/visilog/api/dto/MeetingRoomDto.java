package com.visilog.api.dto;

import com.visilog.api.entity.MeetingRoom;
import java.util.UUID;

public record MeetingRoomDto(UUID id, String name, Integer capacity, String floor) {
    public static MeetingRoomDto from(MeetingRoom r) {
        return new MeetingRoomDto(r.getId(), r.getName(), r.getCapacity(), r.getFloor());
    }
}
