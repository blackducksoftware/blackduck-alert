import { routerReducer as routing } from 'react-router-redux';
import { combineReducers } from 'redux';

import about from 'store/reducers/about';
import audit from 'store/reducers/audit';
import certificates from "store/reducers/certificates";
import descriptors from 'store/reducers/descriptors';
import distributions from 'store/reducers/distributions';
import distributionConfigs from 'store/reducers/distributionConfigs';
import session from 'store/reducers/session';
import system from 'store/reducers/system';
import refresh from 'store/reducers/refresh';
import globalConfiguration from 'store/reducers/globalConfiguration';
import roles from 'store/reducers/roles';
import tasks from 'store/reducers/tasks';
import users from 'store/reducers/users';


const rootReducer = combineReducers({
    about,
    audit,
    certificates,
    globalConfiguration,
    descriptors,
    distributions,
    distributionConfigs,
    session,
    system,
    refresh,
    routing,
    roles,
    tasks,
    users
});

export default rootReducer;
