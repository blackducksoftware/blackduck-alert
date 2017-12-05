'use strict';

import React from 'react';
import HubConfiguration from './HubConfiguration';
import SchedulingConfiguration from './SchedulingConfiguration';
import EmailConfiguration from './EmailConfiguration';
import HipChatConfiguration from './HipChatConfiguration';
import SlackConfiguration from './SlackConfiguration';
import Header from '../Header';

import { tabContent, table, channelTabs, tabSelected} from '../../../css/main.css';
import { content_block } from '../../../css/server_config.css';

import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

class ServerContent extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
            channelIndex: 0
		};
	}

	render() {
		return (
            <Tabs selectedTabClassName={tabSelected} selectedIndex={this.state.channelIndex} onSelect={index => this.setState({ channelIndex: index })}>
                <TabList className={table}>
                    <Tab className={channelTabs}>Hub</Tab>
                    <Tab className={channelTabs}>Scheduling</Tab>
                    <Tab className={channelTabs}>Email</Tab>
                    <Tab className={channelTabs}>Hipchat</Tab>
                    <Tab className={channelTabs}>Slack</Tab>
                </TabList>
                <div>
                    <TabPanel>
                        <HubConfiguration restUrl="/configuration/global" testUrl="/configuration/global/test" />
                    </TabPanel>
                    <TabPanel>
                        <SchedulingConfiguration restUrl="/configuration/global" testUrl="/configuration/global/test" />
                    </TabPanel>
                    <TabPanel>
                        <EmailConfiguration restUrl="/configuration/email" testUrl="/configuration/email/test" />
                    </TabPanel>
                    <TabPanel>
                        <HipChatConfiguration restUrl="/configuration/hipchat" testUrl="/configuration/hipchat/test" />
                    </TabPanel>
                    <TabPanel>
                        <SlackConfiguration restUrl="/configuration/slack" testUrl="/configuration/slack/test" />
                    </TabPanel>
                </div>
            </Tabs>
		)
	}
}

export default ServerContent;
