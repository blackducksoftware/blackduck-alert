package com.synopsys.integration.alert.database.deprecated.channel.email;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.deprecated.channel.DistributionChannelConfigEntity;

@Entity
@Table(schema = "alert", name = "email_group_distribution_config")
public class EmailGroupDistributionConfigEntity extends DistributionChannelConfigEntity {
    @Column(name = "email_template_logo_image")
    private String emailTemplateLogoImage;

    @Column(name = "email_subject_line")
    private String emailSubjectLine;

    @Column(name = "project_owner_only")
    private Boolean projectOwnerOnly;

    public EmailGroupDistributionConfigEntity() {
        // JPA requires default constructor definitions
    }

    public EmailGroupDistributionConfigEntity(final String emailTemplateLogoImage, final String emailSubjectLine, final boolean projectOwnerOnly) {
        this.emailTemplateLogoImage = emailTemplateLogoImage;
        this.emailSubjectLine = emailSubjectLine;
        this.projectOwnerOnly = projectOwnerOnly;
    }

    public String getEmailTemplateLogoImage() {
        return emailTemplateLogoImage;
    }

    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    public Boolean getProjectOwnerOnly() {
        return projectOwnerOnly;
    }
}
