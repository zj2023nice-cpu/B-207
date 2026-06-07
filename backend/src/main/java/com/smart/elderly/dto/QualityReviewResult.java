package com.smart.elderly.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class QualityReviewResult {
    private boolean suspicious = false;
    private int qualityScore = 100;
    private List<String> triggeredRules = new ArrayList<>();
    private List<String> issues = new ArrayList<>();
    private String issuesSummary;

    public void addIssue(String ruleCode, String issue, int scoreDeduction) {
        this.triggeredRules.add(ruleCode);
        this.issues.add(issue);
        this.qualityScore = Math.max(0, this.qualityScore - scoreDeduction);
        this.suspicious = true;
    }

    public String buildIssuesSummary() {
        if (issues.isEmpty()) {
            return "";
        }
        return String.join("；", issues);
    }
}
