import { ABOUT_INFO_FETCH_ERROR, ABOUT_INFO_FETCHED, ABOUT_INFO_FETCHING, SERIALIZE } from 'store/actions/types';

const initialState = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: '',
    commitHash: '',
    copyrightYear: '',
    documentationUrl: '',
    channelList: [],
    providerList: [],
    initialized: false,
    startupTime: '',
    latestMessages: []
};

const config = (state = initialState, action) => {
    switch (action.type) {
        case ABOUT_INFO_FETCHING:
            return { ...state, fetching: true };
        case ABOUT_INFO_FETCHED:
            return {
                ...state,
                fetching: false,
                version: action.version,
                description: action.description,
                projectUrl: action.projectUrl,
                commitHash: action.commitHash,
                copyrightYear: action.copyrightYear,
                documentationUrl: action.documentationUrl,
                initialized: action.initialized,
                startupTime: action.startupTime,
                providerList: action.providerList,
                channelList: action.channels
            };
        case ABOUT_INFO_FETCH_ERROR:
            return { ...state, fetching: false };
        case SERIALIZE:
            return initialState;
        default:
            return state;
    }
};

export default config;
