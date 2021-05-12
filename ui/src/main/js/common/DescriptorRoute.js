import React from 'react';
import * as PropTypes from 'prop-types';
import { Route } from 'react-router-dom';
import * as DescriptorUtilities from 'common/util/descriptorUtilities';

const DescriptorRoute = ({
    descriptor, uriPrefix, hasTestFields, render
}) => {
    const [hasTest, hasSave, hasDelete] = DescriptorUtilities.getButtonPermissions(descriptor, hasTestFields);
    const readonly = (descriptor) ? descriptor.readOnly : true;

    return descriptor
        ? (
            <Route
                exact
                key={descriptor.urlName}
                path={`${uriPrefix}${descriptor.urlName}`}
            >
                {render(readonly, hasTest, hasSave, hasDelete)}
            </Route>
        )
        : null;
};

DescriptorRoute.propTypes = {
    uriPrefix: PropTypes.string.isRequired,
    render: PropTypes.func.isRequired,
    descriptor: PropTypes.object,
    hasTestFields: PropTypes.bool
};

DescriptorRoute.defaultProps = {
    descriptor: undefined,
    hasTestFields: false
};

export default DescriptorRoute;
