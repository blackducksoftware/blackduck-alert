import React from 'react';
import PropTypes from 'prop-types';
import PageLayout from 'common/component/PageLayout';
import AzureBoardsTable from 'page/channel/azure/AzureBoardsTable';
import { AZURE_BOARDS_INFO } from 'page/channel/azure/AzureBoardsModel';

const AzureBoardsLayout = ({ readonly, allowDelete }) => (
    <PageLayout
        title={AZURE_BOARDS_INFO.label}
        description="Configure the Azure Boards instance that Alert will send issue updates to."
        headerIcon={['fab', 'windows']}
    >
        <AzureBoardsTable readonly={readonly} allowDelete={allowDelete} />
    </PageLayout>
);

AzureBoardsLayout.propTypes = {
    readonly: PropTypes.bool,
    allowDelete: PropTypes.bool
};

export default AzureBoardsLayout;