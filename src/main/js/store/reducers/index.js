import { routerReducer as routing } from 'react-router-redux';
import { combineReducers } from 'redux';

import about from 'store/reducers/about';
import audit from 'store/reducers/audit';
import descriptors from 'store/reducers/descriptors';
import distributions from 'store/reducers/distributions';
import distributionConfigs from 'store/reducers/distributionConfigs';
import projects from 'store/reducers/projects';
import session from 'store/reducers/session';
import system from 'store/reducers/system';
import globalConfiguration from './globalConfiguration';


const rootReducer = combineReducers({
    about,
    audit,
    globalConfiguration,
    descriptors,
    distributions,
    distributionConfigs,
    projects,
    session,
    system,
    routing
});

export default rootReducer;
