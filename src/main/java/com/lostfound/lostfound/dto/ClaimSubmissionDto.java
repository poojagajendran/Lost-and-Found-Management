package com.lostfound.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClaimSubmissionDto {

    @NotBlank(message = "Please provide an explanation describing your ownership")
    @Size(min = 10, max = 3000, message = "Explanation must be at least 10 characters")
    private String explanation;

    @NotBlank(message = "Please select an evidence type")
    private String evidenceType = "IDENTIFYING_MARKS";

    @NotBlank(message = "Please provide specific evidence details")
    @Size(min = 10, max = 3000, message = "Evidence details must be at least 10 characters")
    private String evidenceDetails;

    public ClaimSubmissionDto() {
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getEvidenceDetails() {
        return evidenceDetails;
    }

    public void setEvidenceDetails(String evidenceDetails) {
        this.evidenceDetails = evidenceDetails;
    }
}
