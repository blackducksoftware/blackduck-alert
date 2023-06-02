import React from 'react';
import PropTypes from 'prop-types';
import { BLACKDUCK_URLS } from 'page/provider/blackduck/BlackDuckModel';

const AboutProviderCell = ({ data }) => (
    <a href={BLACKDUCK_URLS.blackDuckTableUrl}>
        {data.name}
    </a>
);

AboutProviderCell.propTypes = {
    data: PropTypes.shape({
        name: PropTypes.string
    })
};

export default AboutProviderCell;
