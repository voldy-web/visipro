package com.visilog.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A paying tenant. Self-registered via POST /companies/register — see
// AuthService.registerCompany. Every other tenant-scoped table carries
// an organizationId FK back to this and every query is scoped to it.
@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
public class Organization {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false)
    private String name;

    private String logoUrl;

    // Brand theme — defaults applied at creation (see AuthService) so a
    // freshly registered org isn't blank/unstyled before the admin
    // customizes it in Company Setup.
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    private String brandDark;
    @Column(nullable = false)
    private String brandTint;
    @Column(name = "primary_color", nullable = false)
    private String primary;
    @Column(nullable = false)
    private String primaryPressed;
    @Column(nullable = false)
    private String primarySurface;
    @Column(nullable = false)
    private String primarySurfaceStrong;

    // Null until the Administrator sets it in Company Setup — the
    // clock-in geofence check (see ClockRecordService) is skipped
    // entirely when this is unset.
    private Double officeLatitude;
    private Double officeLongitude;
    private Integer officeRadiusMeters;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
