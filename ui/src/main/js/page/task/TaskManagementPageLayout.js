import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import TaskManagementTable from 'page/task/TaskManagementTable';
import { TASK_MANAGEMENT_INFO } from 'page/task/TaskManagementModel';

const TaskManagementPageLayout = () => (
    <div>
        <PageHeader
            title={TASK_MANAGEMENT_INFO.label}
            description="This page allows you to view the tasks running internally within Alert."
            icon="list"
        />
        <TaskManagementTable />
    </div>
);

export default TaskManagementPageLayout;
