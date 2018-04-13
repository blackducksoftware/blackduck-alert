import React from 'react';
import PropTypes from 'prop-types';
import { Popover, OverlayTrigger } from 'react-bootstrap';

const policyViolationIcon = <span key="policyViolationIcon" alt="Policy Violation" className="fa fa-ban policyViolation" aria-hidden="true" />;
const policyViolationClearedIcon = <span key="policyViolationClearedIcon" alt="Policy Violation Cleared" className="fa fa-eraser policyViolationCleared" aria-hidden="true" />;
const policyViolationOverrideIcon = <span key="policyViolationOverrideIcon" alt="Policy Override" className="fa fa-exclamation-circle policyViolationOverride" aria-hidden="true" />;
const highVulnerabilityIcon = <span key="highVulnerabilityIcon" alt="High Vulnerability" className="fa fa-shield highVulnerability" aria-hidden="true" />;
const mediumVulnerabilityIcon = <span key="mediumVulnerabilityIcon" alt="Medium Vulnerability" className="fa fa-shield mediumVulnerability" aria-hidden="true" />;
const lowVulnerabilityIcon = <span key="lowVulnerabilityIcon" alt="Low Vulnerability" className="fa fa-shield lowVulnerability" aria-hidden="true" />;
const vulnerabilityIcon = <span key="vulnerabilityIcon" alt="Vulnerability" className="fa fa-shield vulnerability" aria-hidden="true" />;

const NotificationTypeLegend = ({
    hasPolicyViolation,
    hasPolicyViolationCleared,
    hasPolicyViolationOverride,
    hasHighVulnerability,
    hasMediumVulnerability,
    hasLowVulnerability,
    hasVulnerability
}) => (
    <OverlayTrigger
        trigger={['hover', 'focus']}
        placement="right"
        overlay={(
            <Popover id="popover" title="Notification Type Legend">
                { hasPolicyViolation && <div>{ policyViolationIcon } Policy Violation</div> }
                { hasPolicyViolationCleared && <div>{ policyViolationClearedIcon } Policy Violation Cleared</div> }
                { hasPolicyViolationOverride && <div>{ policyViolationOverrideIcon } Policy Override</div> }
                { hasHighVulnerability && <div>{highVulnerabilityIcon} High Vulnerability</div> }
                { hasMediumVulnerability && <div>{mediumVulnerabilityIcon} Medium Vulnerability</div> }
                { hasLowVulnerability && <div>{lowVulnerabilityIcon} Low Vulnerability</div> }
                { hasVulnerability && <div>{vulnerabilityIcon} Vulnerability</div> }
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
    hasVulnerability: PropTypes.bool
};

NotificationTypeLegend.defaultProps = {
    hasPolicyViolation: false,
    hasPolicyViolationCleared: false,
    hasPolicyViolationOverride: false,
    hasHighVulnerability: false,
    hasMediumVulnerability: false,
    hasLowVulnerability: false,
    hasVulnerability: false
};

export default NotificationTypeLegend;
