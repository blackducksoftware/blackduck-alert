'use strict';

import React from 'react';
import GlobalConfiguration from './component/GlobalConfiguration';
import EmailConfiguration from './component/EmailConfiguration';
import HipChatConfiguration from './component/HipChatConfiguration';
import SlackConfiguration from './component/SlackConfiguration';
import DistributionConfiguration from './component/DistributionConfiguration';
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
                        <Tab className={styles.configTabs}>Hub settings</Tab>
                        <Tab className={styles.configTabs}>Channel configuration</Tab>
                        <Tab className={styles.configTabs}>Distribution configuration</Tab>
                    </TabList>
                    <div className={styles.tableBorder}>
                        <TabPanel className={styles.tabContent}>
                            <GlobalConfiguration restUrl="/configuration/global" testUrl="/configuration/global/test" />
                        </TabPanel>
                        <TabPanel className={styles.tabContent}>
                            <Tabs selectedTabClassName={styles.tabSelected} selectedIndex={this.state.channelIndex} onSelect={index => this.setState({ channelIndex: index })}>
                                <TabList className={styles.table}>
                                    <Tab className={styles.channelTabs}>Email</Tab>
                                    <Tab className={styles.channelTabs}>Hipchat</Tab>
                                    <Tab className={styles.channelTabs}>Slack</Tab>
                                </TabList>
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
                            <DistributionConfiguration />
                        </TabPanel>
                    </div>
                </Tabs>
            </div>
		)
	}
}

export default MainPage;