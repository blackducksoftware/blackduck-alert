import React from 'react';
import PropTypes from 'prop-types';
import { Tab, Tabs } from 'react-bootstrap';
import DistributionTable from 'page/audit/DistributionTable';
import TextArea from 'common/component/input/TextArea';

const AuditFailureModalContent = ({ data }) => {
    let jsonContent;
    if (data.notification.content) {
        jsonContent = JSON.parse(data.notification.content);
    } else {
        jsonContent = { warning: 'Content in an Unknown Format' };
    }

    const jsonPrettyPrintContent = JSON.stringify(jsonContent, null, 2);

    return (
        <Tabs defaultActiveKey={1} id="audit-details-tabs">
            <Tab eventKey={1} title="Distribution Jobs">
                <DistributionTable data={data} />
            </Tab>
            <Tab eventKey={2} title="Notification Content">
                <TextArea
                    sizeClass="col-sm-12"
                    label=""
                    readOnly
                    name="notificationContent"
                    value={jsonPrettyPrintContent}
                />
            </Tab>
        </Tabs>
    );
};

AuditFailureModalContent.propTypes = {
    data: PropTypes.object
};

export default AuditFailureModalContent;
