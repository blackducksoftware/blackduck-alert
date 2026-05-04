import React from 'react';
import * as PropTypes from 'prop-types';

/**
 * This component is used to conditionally render a route based on the presence of a descriptor. We use
 * descriptors as features that a user has access to based on their roles/permissions, so if a descriptor
 * is not present, the user should not be able to access the route.
 */
const DescriptorRoute = ({ descriptor, element }) => (descriptor ? element : null);

DescriptorRoute.propTypes = {
    descriptor: PropTypes.object,
    element: PropTypes.node.isRequired
};

export default DescriptorRoute;