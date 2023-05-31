import React from 'react';
import PropTypes from 'prop-types';
import IconButton from 'common/component/button/IconButton';
import { DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import { useHistory } from 'react-router-dom/cjs/react-router-dom';

const DistributionEditCell = ({ data, settings }) => {
    const history = useHistory();
    const { type } = settings;
    const url = type === 'edit'
        ? `${DISTRIBUTION_URLS.distributionConfigUrl}/${data.jobId}`
        : `${DISTRIBUTION_URLS.distributionConfigCopyUrl}/${data.jobId}`;

    function handleClick() {
        history.push(url);
    }

    return (
        <IconButton icon={type === 'edit' ? 'pencil-alt' : 'copy'} onClick={() => handleClick()} />
    );
};

DistributionEditCell.propTypes = {
    data: PropTypes.shape({
        jobId: PropTypes.string
    }),
    settings: PropTypes.shape({
        type: PropTypes.string
    })
};

export default DistributionEditCell;
