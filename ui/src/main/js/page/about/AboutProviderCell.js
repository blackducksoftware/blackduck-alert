import React from 'react';
import PropTypes from 'prop-types';
import { BLACKDUCK_URLS } from 'page/provider/blackduck/BlackDuckModel';
import { NavLink } from 'react-router-dom';

const AboutProviderCell = ({ data }) => (
    <NavLink to={BLACKDUCK_URLS.blackDuckTableUrl} id={data.urlName}>
        {data.name}
    </NavLink>
);

AboutProviderCell.propTypes = {
    data: PropTypes.shape({
        name: PropTypes.string,
        urlName: PropTypes.string
    })
};

export default AboutProviderCell;
