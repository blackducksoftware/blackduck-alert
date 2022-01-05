import { ABOUT_INFO_FETCH_ERROR, ABOUT_INFO_FETCHED, ABOUT_INFO_FETCHING, SERIALIZE } from 'store/actions/types';

const initialState = {
    fetching: false,
    version: '',
    description: '',
    projectUrl: '',
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
                documentationUrl: action.documentationUrl,
                initialized: action.initialized,
                startupTime: action.startupTime,
                providerList: action.providers,
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
