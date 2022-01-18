import React from 'react';
import PropTypes from 'prop-types';
import { NavLink } from 'react-router-dom';

const MessageFormatter = ({
    id, errorIsDetailed, message, header, title, componentLink, componentLabel
}) => (
    <div id={id}>
        {!errorIsDetailed && message}
        {errorIsDetailed && (
            <div>
                <h3>{header}</h3>
                <div>{title}</div>
                <div>
                    {message}
                    <NavLink to={componentLink}>
                        {componentLabel}
                    </NavLink>
                </div>
            </div>
        )}
    </div>
);

MessageFormatter.propTypes = {
    id: PropTypes.string,
    errorIsDetailed: PropTypes.bool,
    message: PropTypes.string,
    header: PropTypes.string,
    title: PropTypes.string,
    componentLink: PropTypes.string,
    componentLabel: PropTypes.string
};

MessageFormatter.defaultProps = {
    id: 'messageFormatterId',
    errorIsDetailed: false,
    message: null,
    header: null,
    title: null,
    componentLink: null,
    componentLabel: null
};

export default MessageFormatter;
