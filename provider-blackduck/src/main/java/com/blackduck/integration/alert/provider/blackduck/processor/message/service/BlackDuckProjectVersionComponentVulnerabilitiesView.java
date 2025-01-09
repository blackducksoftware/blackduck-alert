/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message.service;

import java.math.BigDecimal;

import com.blackduck.integration.blackduck.api.core.BlackDuckView;
import com.blackduck.integration.blackduck.api.generated.enumeration.VulnerabilityRemediationStatusType;
import com.google.gson.JsonElement;

// TODO replace with updated model from blackduck-common-api when available
//  Copied from: ProjectVersionComponentVersionVulnerabilityRemediationView (blackduck-common-api:2020.8.0.18)
public class BlackDuckProjectVersionComponentVulnerabilitiesView extends BlackDuckView {
    private String comment;
    private java.util.Date createdAt;
    private JsonElement createdBy;
    private ProjectVersionComponentVersionVulnerabilityRemediationCvssView cvss2;
    private ProjectVersionComponentVersionVulnerabilityRemediationCvssView cvss3;
    private ProjectVersionComponentVersionVulnerabilityRemediationCvssView cvss4;
    private java.util.Date disclosureDate;
    private Boolean exploitAvailable;
    private java.util.Date exploitPublishDate;
    private String id;
    private java.util.Date lastModifiedDate;
    private BigDecimal overallScore;
    private java.util.Date publishedDate;
    private String relatedVulnerability;
    private VulnerabilityRemediationStatusType remediationStatus;
    private String solution;
    private Boolean solutionAvailable;
    private java.util.Date solutionDate;
    private String summary;
    private String technicalDescription;
    private String title;
    private java.util.Date updatedAt;
    private JsonElement updatedBy;
    private Boolean useCvss3;   // TODO: For backwards compatability with hub 2024.10.0, remove when version is not supported
    private String cvssVersion;
    private Boolean workaroundAvailable;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public java.util.Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }

    public JsonElement getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(JsonElement createdBy) {
        this.createdBy = createdBy;
    }

    public ProjectVersionComponentVersionVulnerabilityRemediationCvssView getCvss2() {
        return cvss2;
    }

    public void setCvss2(ProjectVersionComponentVersionVulnerabilityRemediationCvssView cvss2) {
        this.cvss2 = cvss2;
    }

    public ProjectVersionComponentVersionVulnerabilityRemediationCvssView getCvss3() {
        return cvss3;
    }

    public void setCvss3(ProjectVersionComponentVersionVulnerabilityRemediationCvssView cvss3) {
        this.cvss3 = cvss3;
    }

    public ProjectVersionComponentVersionVulnerabilityRemediationCvssView getCvss4() {
        return cvss4;
    }

    public void setCvss4(ProjectVersionComponentVersionVulnerabilityRemediationCvssView cvss4) {
        this.cvss4 = cvss4;
    }

    public java.util.Date getDisclosureDate() {
        return disclosureDate;
    }

    public void setDisclosureDate(java.util.Date disclosureDate) {
        this.disclosureDate = disclosureDate;
    }

    public Boolean getExploitAvailable() {
        return exploitAvailable;
    }

    public void setExploitAvailable(Boolean exploitAvailable) {
        this.exploitAvailable = exploitAvailable;
    }

    public java.util.Date getExploitPublishDate() {
        return exploitPublishDate;
    }

    public void setExploitPublishDate(java.util.Date exploitPublishDate) {
        this.exploitPublishDate = exploitPublishDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public java.util.Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(java.util.Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public java.util.Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(java.util.Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getRelatedVulnerability() {
        return relatedVulnerability;
    }

    public void setRelatedVulnerability(String relatedVulnerability) {
        this.relatedVulnerability = relatedVulnerability;
    }

    public VulnerabilityRemediationStatusType getRemediationStatus() {
        return remediationStatus;
    }

    public void setRemediationStatus(VulnerabilityRemediationStatusType remediationStatus) {
        this.remediationStatus = remediationStatus;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public Boolean getSolutionAvailable() {
        return solutionAvailable;
    }

    public void setSolutionAvailable(Boolean solutionAvailable) {
        this.solutionAvailable = solutionAvailable;
    }

    public java.util.Date getSolutionDate() {
        return solutionDate;
    }

    public void setSolutionDate(java.util.Date solutionDate) {
        this.solutionDate = solutionDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTechnicalDescription() {
        return technicalDescription;
    }

    public void setTechnicalDescription(String technicalDescription) {
        this.technicalDescription = technicalDescription;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public java.util.Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.util.Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public JsonElement getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(JsonElement updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getUseCvss3() {
        return useCvss3;
    }

    public void setUseCvss3(Boolean useCvss3) {
        this.useCvss3 = useCvss3;
    }

    public String getCvssVersion() {
        return cvssVersion;
    }

    public void setCvssVersion(String cvssVersion) {
        this.cvssVersion = cvssVersion;
    }

    public Boolean getWorkaroundAvailable() {
        return workaroundAvailable;
    }

    public void setWorkaroundAvailable(Boolean workaroundAvailable) {
        this.workaroundAvailable = workaroundAvailable;
    }

}
