import React from 'react';
import PageLayout from 'common/component/PageLayout';
import TaskManagementTable from 'page/task/TaskManagementTable';
import { TASK_MANAGEMENT_INFO } from 'page/task/TaskManagementModel';

const TaskManagementPageLayout = () => (
    <PageLayout
        title={TASK_MANAGEMENT_INFO.label}
        description="This page allows you to view the tasks running internally within Alert."
        headerIcon="list"
    >
        <TaskManagementTable />
    </PageLayout>
);

export default TaskManagementPageLayout;
