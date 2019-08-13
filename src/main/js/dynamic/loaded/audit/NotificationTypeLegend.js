import React from 'react';
import PropTypes from 'prop-types';
import { OverlayTrigger, Popover } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const policyViolationIcon = <span key="policyViolationIcon" alt="Policy Violation" className="fa-layers fa-fw policyViolation"><FontAwesomeIcon icon="ban" className="alert-icon" size="lg" /></span>;
const policyViolationClearedIcon = <span key="policyViolationClearedIcon" alt="Policy Violation Cleared" className="fa-layers fa-fw policyViolationCleared"><FontAwesomeIcon icon="eraser" className="alert-icon" size="lg" /></span>;
const policyViolationOverrideIcon = <span key="policyViolationOverrideIcon" alt="Policy Override" className="fa-layers fa-fw policyViolationOverride"><FontAwesomeIcon icon="exclamation-circle" className="alert-icon" size="lg" /></span>;
const highVulnerabilityIcon = <span key="highVulnerabilityIcon" alt="High Vulnerability" className="fa-layers fa-fw highVulnerability"><FontAwesomeIcon icon="shield-alt" className="alert-icon" size="lg" /></span>;
const mediumVulnerabilityIcon = <span key="mediumVulnerabilityIcon" alt="Medium Vulnerability" className="fa-layers fa-fw mediumVulnerability"><FontAwesomeIcon icon="shield-alt" className="alert-icon" size="lg" /></span>;
const lowVulnerabilityIcon = <span key="lowVulnerabilityIcon" alt="Low Vulnerability" className="fa-layers fa-fw lowVulnerability"><FontAwesomeIcon icon="shield-alt" className="alert-icon" size="lg" /></span>;
const vulnerabilityIcon = <span key="vulnerabilityIcon" alt="Vulnerability" className="fa-layers fa-fw highVulnerability"><FontAwesomeIcon icon="shield-alt" className="alert-icon" size="lg" /></span>;
const issueCountIncreasedIcon = <span key="issueCountIncreasing" alt="Issue Count Increased" className="fa-layers fa-fw issueCountIncreased"><FontAwesomeIcon icon="angle-double-up" className="alert-icon" size="lg" /></span>;
const issueCountDecreasedIcon = <span key="issueCountDecreased" alt="Issue Count Decreased" className="fa-layers fa-fw issueCountDecreased"><FontAwesomeIcon icon="angle-double-down" className="alert-icon" size="lg" /></span>;
const licenseLimitIcon = <span key="licenseLimit" alt="License Limit" className="fa fa-database fa-fw licenseLimit" aria-hidden="true"><FontAwesomeIcon icon="gavel" className="alert-icon" size="lg" /></span>;
const bomEditIcon = <span key="bomEdit" alt="Bom Edit" className="fa-layers fa-fw bomEdit"><FontAwesomeIcon icon="user-edit" className="alert-icon" size="lg" /></span>;

const NotificationTypeLegend = ({
                                    hasPolicyViolation,
                                    hasPolicyViolationCleared,
                                    hasPolicyViolationOverride,
                                    hasHighVulnerability,
                                    hasMediumVulnerability,
                                    hasLowVulnerability,
                                    hasVulnerability,
                                    hasIssueCountIncreased,
                                    hasIssueCountDecreased,
                                    hasLicenseLimit,
                                    hasBomEdit
                                }) => (
    <OverlayTrigger
        trigger={['hover', 'focus']}
        placement="right"
        overlay={(
            <Popover id="popover" title="Notification Type Legend">
                {hasPolicyViolation && <div>{policyViolationIcon} Policy Violation</div>}
                {hasPolicyViolationCleared && <div>{policyViolationClearedIcon} Policy Violation Cleared</div>}
                {hasPolicyViolationOverride && <div>{policyViolationOverrideIcon} Policy Override</div>}
                {hasHighVulnerability && <div>{highVulnerabilityIcon} High Vulnerability</div>}
                {hasMediumVulnerability && <div>{mediumVulnerabilityIcon} Medium Vulnerability</div>}
                {hasLowVulnerability && <div>{lowVulnerabilityIcon} Low Vulnerability</div>}
                {hasVulnerability && <div>{vulnerabilityIcon} Vulnerability</div>}
                {hasIssueCountIncreased && <div>{issueCountIncreasedIcon} Issue Count Increased</div>}
                {hasIssueCountDecreased && <div>{issueCountDecreasedIcon} Issue Count Decreased</div>}
                {hasLicenseLimit && <div>{licenseLimitIcon} License Limit</div>}
                {hasBomEdit && <div>{bomEditIcon} Bom Edit</div>}
            </Popover>
        )}
    >
        <span>
            {hasPolicyViolation && policyViolationIcon}
            {hasPolicyViolationCleared && policyViolationClearedIcon}
            {hasPolicyViolationOverride && policyViolationOverrideIcon}
            {hasHighVulnerability && highVulnerabilityIcon}
            {hasMediumVulnerability && mediumVulnerabilityIcon}
            {hasLowVulnerability && lowVulnerabilityIcon}
            {hasVulnerability && vulnerabilityIcon}
            {hasIssueCountIncreased && issueCountIncreasedIcon}
            {hasIssueCountDecreased && issueCountDecreasedIcon}
            {hasLicenseLimit && licenseLimitIcon}
            {hasBomEdit && bomEditIcon}
        </span>
    </OverlayTrigger>
);

NotificationTypeLegend.propTypes = {
    hasPolicyViolation: PropTypes.bool,
    hasPolicyViolationCleared: PropTypes.bool,
    hasPolicyViolationOverride: PropTypes.bool,
    hasHighVulnerability: PropTypes.bool,
    hasMediumVulnerability: PropTypes.bool,
    hasLowVulnerability: PropTypes.bool,
    hasVulnerability: PropTypes.bool,
    hasIssueCountIncreased: PropTypes.bool,
    hasIssueCountDecreased: PropTypes.bool,
    hasLicenseLimit: PropTypes.bool,
    hasBomEdit: PropTypes.bool
};

NotificationTypeLegend.defaultProps = {
    hasPolicyViolation: false,
    hasPolicyViolationCleared: false,
    hasPolicyViolationOverride: false,
    hasHighVulnerability: false,
    hasMediumVulnerability: false,
    hasLowVulnerability: false,
    hasVulnerability: false,
    hasIssueCountIncreased: false,
    hasIssueCountDecreased: false,
    hasLicenseLimit: false,
    hasBomEdit: false
};

export default NotificationTypeLegend;
