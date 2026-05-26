import React from 'react';
import PropTypes from 'prop-types';
import PageLayout from 'common/component/PageLayout';
import AzureBoardsTable from 'page/channel/azure/AzureBoardsTable';
import { AZURE_BOARDS_INFO } from 'page/channel/azure/AzureBoardsModel';
import useGetPermissions from 'common/hooks/useGetPermissions';

const AzureBoardsLayout = ({ descriptor }) => {
    const { readOnly, canDelete } = useGetPermissions(descriptor);

    return (
        <PageLayout
            title={AZURE_BOARDS_INFO.label}
            description="Configure the Azure Boards instance that Alert will send issue updates to."
            headerIcon={['fab', 'windows']}
        >
            <AzureBoardsTable readonly={readOnly} allowDelete={canDelete} />
        </PageLayout>
    );
};

AzureBoardsLayout.propTypes = {
    descriptor: PropTypes.object
};

export default AzureBoardsLayout;
