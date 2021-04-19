import { combineReducers } from 'redux';
import { connectRouter } from 'connected-react-router'

import about from 'store/reducers/about';
import audit from 'store/reducers/audit';
import certificates from 'store/reducers/certificates';
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

const rootReducer = (history) => combineReducers({
    router: connectRouter(history),
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
    roles,
    tasks,
    users
});

export default rootReducer;
