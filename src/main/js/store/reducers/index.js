import { routerReducer as routing } from 'react-router-redux';
import { combineReducers } from 'redux';

import about from 'store/reducers/about';
import audit from 'store/reducers/audit';
import descriptors from 'store/reducers/descriptors';
import distributions from 'store/reducers/distributions';
import distributionConfigs from 'store/reducers/distributionConfigs';
import session from 'store/reducers/session';
import system from 'store/reducers/system';
import refresh from 'store/reducers/refresh';
import globalConfiguration from './globalConfiguration';


const rootReducer = combineReducers({
    about,
    audit,
    globalConfiguration,
    descriptors,
    distributions,
    distributionConfigs,
    session,
    system,
    refresh,
    routing
});

export default rootReducer;
