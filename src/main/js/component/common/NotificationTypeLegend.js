import React from 'react';
import PropTypes from 'prop-types';
import { OverlayTrigger, Popover } from 'react-bootstrap';

const policyViolationIcon = <span key="policyViolationIcon" alt="Policy Violation" className="fa fa-ban fa-fw policyViolation" aria-hidden="true" />;
const policyViolationClearedIcon = <span key="policyViolationClearedIcon" alt="Policy Violation Cleared" className="fa fa-eraser fa-fw policyViolationCleared" aria-hidden="true" />;
const policyViolationOverrideIcon = <span key="policyViolationOverrideIcon" alt="Policy Override" className="fa fa-exclamation-circle fa-fw policyViolationOverride" aria-hidden="true" />;
const highVulnerabilityIcon = <span key="highVulnerabilityIcon" alt="High Vulnerability" className="fa fa-shield fa-fw highVulnerability" aria-hidden="true" />;
const mediumVulnerabilityIcon = <span key="mediumVulnerabilityIcon" alt="Medium Vulnerability" className="fa fa-shield fa-fw mediumVulnerability" aria-hidden="true" />;
const lowVulnerabilityIcon = <span key="lowVulnerabilityIcon" alt="Low Vulnerability" className="fa fa-shield fa-fw lowVulnerability" aria-hidden="true" />;
const vulnerabilityIcon = <span key="vulnerabilityIcon" alt="Vulnerability" className="fa fa-shield fa-fw highVulnerability" aria-hidden="true" />;
const issueCountIncreasedIcon = <span key="issueCountIncreasing" alt="Issue Count Increased" className="fa fa-angle-double-up fa-lg fa-fw issueCountIncreased" aria-hidden="true" />;
const issueCountDecreasedIcon = <span key="issueCountDecreased" alt="Issue Count Decreased" className="fa fa-angle-double-down fa-lg fa-fw issueCountDecreased" aria-hidden="true" />;
const licenseLimitIcon = <span key="licenseLimit" alt="License Limit" className="fa fa-database fa-fw licenseLimit" aria-hidden="true" />;

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
                                    hasLicenseLimit
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
    hasLicenseLimit: PropTypes.bool
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
    hasLicenseLimit: false
};

export default NotificationTypeLegend;
