import { routerReducer as routing } from 'react-router-redux';
import { combineReducers } from 'redux';

import about from 'store/reducers/about';
import audit from 'store/reducers/audit';
import blackduck from 'store/reducers/blackduck';
import descriptors from 'store/reducers/descriptors';
import distributions from 'store/reducers/distributions';
import distributionConfigs from 'store/reducers/distributionConfigs';
import emailConfig from 'store/reducers/emailConfig';
import hipChatConfig from 'store/reducers/hipChatConfig';
import projects from 'store/reducers/projects';
import schedulingConfig from 'store/reducers/schedulingConfig';
import session from 'store/reducers/session';
import system from 'store/reducers/system';
import refresh from 'store/reducers/refresh';


const rootReducer = combineReducers({
    about,
    audit,
    blackduck,
    descriptors,
    distributions,
    distributionConfigs,
    emailConfig,
    hipChatConfig,
    projects,
    schedulingConfig,
    session,
    system,
    refresh,
    routing
});

export default rootReducer;
