'use strict';

import React, { Component } from 'react';
import TabTitle from '../tabs/TabTitle';
import HubConfiguration from './HubConfiguration';
import SchedulingConfiguration from './SchedulingConfiguration';
import EmailConfiguration from './EmailConfiguration';
import HipChatConfiguration from './HipChatConfiguration';
import SlackConfiguration from './SlackConfiguration';
import Header from '../Header';

import { table } from '../../../css/main.css';
import { subTab, subTabSelected, subTable, subTabContainer, subTabScrollableContainer } from '../../../css/tabs.css';
import { content_block } from '../../../css/server_config.css';

import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

class ServerContent extends Component {
	constructor(props) {
		super(props);
		this.state = {
            channelIndex: 0
		};
	}

	render() {
		return (
            <Tabs selectedTabClassName={subTabSelected} selectedIndex={this.state.channelIndex} onSelect={index => this.setState({ channelIndex: index })}>
                <div className={subTabContainer}>
                    <div className={subTabScrollableContainer}>
                        <TabList className={subTable}>
                            <Tab className={subTab}><TabTitle text='Hub' icon='fa-laptop'></TabTitle></Tab>
                            <Tab className={subTab}><TabTitle text='Scheduling' icon='fa-clock-o'></TabTitle></Tab>
                            <Tab className={subTab}><TabTitle text='Email' icon='fa-envelope'></TabTitle></Tab>
                            <Tab className={subTab}><TabTitle text='Hipchat' icon='fa-comments'></TabTitle></Tab>
                            <Tab className={subTab}><TabTitle text='Slack' icon='fa-slack'></TabTitle></Tab>
                        </TabList>
                    </div>
                </div>
                <div>
                    <TabPanel>
                        <HubConfiguration />
                    </TabPanel>
                    <TabPanel>
                        <SchedulingConfiguration />
                    </TabPanel>
                    <TabPanel>
                        <EmailConfiguration/>
                    </TabPanel>
                    <TabPanel>
                        <HipChatConfiguration/>
                    </TabPanel>
                    <TabPanel>
                        <SlackConfiguration/>
                    </TabPanel>
                </div>
            </Tabs>
		)
	}
}

export default ServerContent;
