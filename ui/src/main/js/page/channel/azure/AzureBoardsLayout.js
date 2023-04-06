import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import AzureBoardTale from 'page/channel/azure/AzureBoardTable';
import { AZURE_BOARDS_INFO } from 'page/channel/azure/AzureBoardsModel';

const AzureBoardsLayout = ({ readonly, allowDelete }) => (
    <div>
        <PageHeader
            title={AZURE_BOARDS_INFO.label}
            description="Configure the Azure Boards instance that Alert will send issue updates to."
        />
        <AzureBoardTale readonly={readonly} allowDelete={allowDelete} />
    </div>
);

export default AzureBoardsLayout;
