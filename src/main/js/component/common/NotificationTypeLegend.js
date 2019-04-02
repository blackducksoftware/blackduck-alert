import React from 'react';
import PropTypes from 'prop-types';
import { OverlayTrigger, Popover } from 'react-bootstrap';

const policyViolationIcon = <span key="policyViolationIcon" alt="Policy Violation" className="fa fa-ban policyViolation" aria-hidden="true" />;
const policyViolationClearedIcon = <span key="policyViolationClearedIcon" alt="Policy Violation Cleared" className="fa fa-eraser policyViolationCleared" aria-hidden="true" />;
const policyViolationOverrideIcon = <span key="policyViolationOverrideIcon" alt="Policy Override" className="fa fa-exclamation-circle policyViolationOverride" aria-hidden="true" />;
const highVulnerabilityIcon = <span key="highVulnerabilityIcon" alt="High Vulnerability" className="fa fa-shield highVulnerability" aria-hidden="true" />;
const mediumVulnerabilityIcon = <span key="mediumVulnerabilityIcon" alt="Medium Vulnerability" className="fa fa-shield mediumVulnerability" aria-hidden="true" />;
const lowVulnerabilityIcon = <span key="lowVulnerabilityIcon" alt="Low Vulnerability" className="fa fa-shield lowVulnerability" aria-hidden="true" />;
const vulnerabilityIcon = <span key="vulnerabilityIcon" alt="Vulnerability" className="fa fa-shield highVulnerability" aria-hidden="true" />;
const issueCountIncreasedIcon = <span key="issueCountIncreasing" alt="Issue Count Increased" className="fa fa-angle-double-up fa-lg issueCountIncreased" aria-hidden="true" />;
const issueCountDecreasedIcon = <span key="issueCountDecreased" alt="Issue Count Decreased" className="fa fa-angle-double-down fa-lg issueCountDecreased" aria-hidden="true" />;

const NotificationTypeLegend = ({
    hasPolicyViolation,
    hasPolicyViolationCleared,
    hasPolicyViolationOverride,
    hasHighVulnerability,
    hasMediumVulnerability,
    hasLowVulnerability,
    hasVulnerability,
    hasIssueCountIncreased,
    hasIssueCountDecreased
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
                {hasIssueCountIncreased && <div>{issueCountIncreasedIcon} Issue Count Increasing</div>}
                {hasIssueCountDecreased && <div>{issueCountDecreasedIcon} Issue Count Decreasing</div>}
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
    hasIssueCountDecreased: PropTypes.bool
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
    hasIssueCountDecreased: false
};

export default NotificationTypeLegend;
