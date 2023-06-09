import React from 'react';
import PropTypes from 'prop-types';
import PageHeader from 'common/component/navigation/PageHeader';
import AzureBoardTable from 'page/channel/azure/AzureBoardTable';
import { useLocation } from 'react-router-dom';
import { AZURE_BOARDS_INFO } from 'page/channel/azure/AzureBoardsModel';

const AzureBoardsLayout = ({ readonly, allowDelete }) => {
    const location = useLocation();

    return (
        <div>
            <PageHeader
                title={AZURE_BOARDS_INFO.label}
                description="Configure the Azure Boards instance that Alert will send issue updates to."
                icon={['fab', 'windows']}
            />
            <AzureBoardTable readonly={readonly} allowDelete={allowDelete} location={location} params={params} />
        </div>
    );
};

AzureBoardsLayout.propTypes = {
    readonly: PropTypes.bool,
    allowDelete: PropTypes.bool
};

export default AzureBoardsLayout;
