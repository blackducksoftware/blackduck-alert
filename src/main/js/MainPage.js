'use strict';

import React, { Component } from 'react';

import Audit from './component/audit/Audit';
import DistributionConfiguration from './component/distribution/DistributionConfiguration';
import HubConfiguration from './component/server/HubConfiguration';
import SchedulingConfiguration from './component/server/SchedulingConfiguration';
import EmailConfiguration from './component/server/EmailConfiguration';
import HipChatConfiguration from './component/server/HipChatConfiguration';
import SlackConfiguration from './component/server/SlackConfiguration';
import Header from './component/Header';
import ServerContent from './component/server/ServerContent';

import styles from '../css/main.css';
import { tabContainer, configTabs, tabContent, tabSelected } from '../css/tabs.css';

import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

class MainPage extends Component {
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
				<Header handleState={this.props.handleState} fixed={true} includeLogout={true}></Header>
                <Tabs className={tabContainer} selectedTabClassName={tabSelected} selectedIndex={this.state.mainIndex} onSelect={index => this.setState({ mainIndex: index })}>
                    <TabList className={styles.table}>
                        <Tab className={configTabs}>Server Configuration</Tab>
                        <Tab className={configTabs}>Distribution Configuration</Tab>
                        <Tab className={configTabs}>Audit</Tab>
                    </TabList>
                    <div className={styles.tableBorder}>
                        <TabPanel className={tabContent}>
                            <ServerContent/>
                        </TabPanel>
                        <TabPanel className={tabContent}>
                            <DistributionConfiguration />
                        </TabPanel>
                        <TabPanel className={tabContent}>
                            <Audit />
                        </TabPanel>
                    </div>
                </Tabs>
            </div>
		)
	}
}

export default MainPage;
