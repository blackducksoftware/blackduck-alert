import React, { useEffect, useState } from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import GithubTable from 'page/channel/github/GithubTable';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import { useDispatch, useSelector } from 'react-redux';
import { fetchGithub } from 'store/actions/github';

const GithubPageLayout = ({
    csrfToken, readonly, showRefreshButton, displayDelete
}) => {
    const dispatch = useDispatch();
    const githubData = useSelector(state => state.github);
    const apiUrl = `${ConfigurationRequestBuilder.CONFIG_API_URL}/github`;

    useEffect(() => {
        dispatch(fetchGithub())
    }, []);

    return (
        <>
            <PageHeader
                title='Github'
                description="This page allows you to configure Alert notifications for Github."
                icon={['fab', 'github']}
            />
            {githubData.data.models ? (
                <GithubTable 
                    apiUrl={apiUrl} 
                    csrfToken={csrfToken}
                    data={githubData.data}
                />
            ) : null }
            
        </>
    );
};


export default GithubPageLayout;
