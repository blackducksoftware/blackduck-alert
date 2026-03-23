import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';

const useStyles = createUseStyles(theme => ({
    badge: {
        display: 'flex',
        alignItems: 'center',
        columnGap: '6px',
        borderRadius: '8px',
        width: 'fit-content',
        padding: '0 10px',
        margin: 'auto',
        fontSize: '13px'
    },
    enabledBadge: {
        color: 'oklch(44.8% 0.119 151.328)',
        border: '1px solid oklch(92.5% 0.084 155.995)',
        backgroundColor: 'oklch(96.2% 0.044 156.743)'
    },
    disabledBadge: {
        color: 'oklch(44.4% 0.177 26.899)',
        border: '1px solid oklch(88.5% 0.062 18.334)',
        backgroundColor: 'oklch(93.6% 0.032 17.717)'
    }
}));

const ProviderEnabledCell = ({ data }) => {
    const classes = useStyles();
    const { enabled } = data;

    const cellClass = classNames(classes.badge, {
        [classes.enabledBadge]: enabled,
        [classes.disabledBadge]: !enabled
    })

    return (
        <div className={cellClass}>
            {enabled ? 'Enabled' : 'Disabled'}
        </div>
    );
};

ProviderEnabledCell.propTypes = {
    data: PropTypes.shape({
        enabled: PropTypes.bool
    })
};

export default ProviderEnabledCell;
