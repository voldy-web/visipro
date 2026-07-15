package com.visilog.api.dto;

import com.visilog.api.entity.Plan;
import java.math.BigDecimal;
import java.util.List;

public record PlanDto(String id, String name, BigDecimal pricePerMonth, Integer seatLimit, List<String> features) {
    public static PlanDto from(Plan p) {
        return new PlanDto(p.getId(), p.getName(), p.getPricePerMonth(), p.getSeatLimit(), List.copyOf(p.getFeatures()));
    }
}
