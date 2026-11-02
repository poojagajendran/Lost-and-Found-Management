package com.lostfound.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClaimDecisionDto {

    @NotBlank(message = "Decision explanation/reason is required")
    @Size(min = 5, max = 2000, message = "Reason must be between 5 and 2000 characters")
    private String decisionReason;

    public ClaimDecisionDto() {
    }

    public ClaimDecisionDto(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }
}
