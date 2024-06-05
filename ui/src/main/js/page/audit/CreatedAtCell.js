import React from 'react';
import PropTypes from 'prop-types';

const CreatedAtCell = ({ data }) => {
    const { createdAt } = data.notification;

    return (
        <>
            {createdAt}
        </>
    );
};

CreatedAtCell.propTypes = {
    data: PropTypes.object
};

export default CreatedAtCell;
