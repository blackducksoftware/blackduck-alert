import React from 'react';
import PropTypes from 'prop-types';
import PageHeader from 'common/component/navigation/PageHeader';
import AzureBoardsTable from 'page/channel/azure/AzureBoardsTable';
import { AZURE_BOARDS_INFO } from 'page/channel/azure/AzureBoardsModel';

const AzureBoardsLayout = ({ readonly, allowDelete }) => (
    <div>
        <PageHeader
            title={AZURE_BOARDS_INFO.label}
            description="Configure the Azure Boards instance that Alert will send issue updates to."
            icon={['fab', 'windows']}
        />
        <AzureBoardsTable readonly={readonly} allowDelete={allowDelete} />
    </div>
);

AzureBoardsLayout.propTypes = {
    readonly: PropTypes.bool,
    allowDelete: PropTypes.bool
};

export default AzureBoardsLayout;
