'use strict';

import React from 'react';
import HubConfiguration from './component/server/HubConfiguration';
import SchedulingConfiguration from './component/server/SchedulingConfiguration';
import EmailConfiguration from './component/server/EmailConfiguration';
import HipChatConfiguration from './component/server/HipChatConfiguration';
import SlackConfiguration from './component/server/SlackConfiguration';
import Header from './component/Header';

import styles from '../css/main.css';

import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

class MainPage extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
				mainIndex: 0,
				channelIndex: 0
		};
	}

	render() {
		return (
				<div className={styles.wrapper}>
				<Header handleState={this.props.handleState} fixed="true" includeLogout="true"></Header>
                <div className={styles.alertHeader}>
                    <h1 className={styles.alertHeaderTag}>Black Duck Alert</h1>
                </div>

                <Tabs className={styles.tabContainer} selectedTabClassName={styles.tabSelected} selectedIndex={this.state.mainIndex} onSelect={index => this.setState({ mainIndex: index })}>
                    <TabList className={styles.table}>
                        <Tab className={styles.configTabs}>Server Configuration </Tab>
                        <Tab className={styles.configTabs}>Distribution Configuration</Tab>
                    </TabList>
                    <div className={styles.tableBorder}>
                        <TabPanel className={styles.tabContent}>
                            <Tabs selectedTabClassName={styles.tabSelected} selectedIndex={this.state.channelIndex} onSelect={index => this.setState({ channelIndex: index })}>
                                <TabList className={styles.table}>
                                    <Tab className={styles.channelTabs}>Hub</Tab>
                                    <Tab className={styles.channelTabs}>Scheduling</Tab>
                                    <Tab className={styles.channelTabs}>Email</Tab>
                                    <Tab className={styles.channelTabs}>Hipchat</Tab>
                                    <Tab className={styles.channelTabs}>Slack</Tab>
                                </TabList>
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
                            </Tabs>
                        </TabPanel>
                        <TabPanel className={styles.tabContent}>

                        </TabPanel>
                    </div>
                </Tabs>
            </div>
		)
	}
}

export default MainPage;
