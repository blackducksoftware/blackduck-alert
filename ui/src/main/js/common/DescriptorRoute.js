import React from 'react';
import * as PropTypes from 'prop-types';
import { Route } from 'react-router-dom';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';

const DescriptorRoute = ({
    descriptor, uriPrefix, hasTestFields, render, paths
}) => {
    const [hasTest, hasSave, hasDelete] = DescriptorUtilities.getButtonPermissions(descriptor, hasTestFields);
    const readonly = (descriptor) ? descriptor.readOnly : true;
    const path = (paths.length > 0) ? paths : [`${uriPrefix}${descriptor && descriptor.urlName}`];

    return descriptor
        ? (
            <Route
                exact
                key={descriptor.urlName}
                path={path}
            >
                {render(readonly, hasTest, hasSave, hasDelete)}
            </Route>
        )
        : null;
};

DescriptorRoute.propTypes = {
    uriPrefix: PropTypes.string,
    render: PropTypes.func.isRequired,
    descriptor: PropTypes.object,
    hasTestFields: PropTypes.bool,
    paths: PropTypes.arrayOf(PropTypes.string)
};

DescriptorRoute.defaultProps = {
    uriPrefix: '',
    descriptor: undefined,
    hasTestFields: false,
    paths: []
};

export default DescriptorRoute;
