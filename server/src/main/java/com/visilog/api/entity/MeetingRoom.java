package com.visilog.api.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Admin-managed in Company Setup — the rooms selectable in
// BookMeetingForm on the frontend and shown on the visitor map tour.
@Entity
@Table(name = "meeting_rooms")
@Getter
@Setter
@NoArgsConstructor
public class MeetingRoom {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private String name;

    private Integer capacity;
    private String floor;
}
