import React from 'react';
import PropTypes from 'prop-types';
import { channelTranslation } from 'page/distribution/DistributionModel';

const ChannelCell = ({ data }) => {
    const { channelName: name } = data;

    return (
        <span>
            {channelTranslation.label(name) || name}
        </span>
    );
};

ChannelCell.propTypes = {
    data: PropTypes.shape({
        channelName: PropTypes.string
    })
};

export default ChannelCell;
