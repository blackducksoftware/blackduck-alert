'use strict';

import React from 'react';

import DistributionConfiguration from './component/distribution/DistributionConfiguration';
import HubConfiguration from './component/server/HubConfiguration';
import SchedulingConfiguration from './component/server/SchedulingConfiguration';
import EmailConfiguration from './component/server/EmailConfiguration';
import HipChatConfiguration from './component/server/HipChatConfiguration';
import SlackConfiguration from './component/server/SlackConfiguration';
import Header from './component/Header';
import ServerContent from './component/server/ServerContent';

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
                <Tabs className={styles.tabContainer} selectedTabClassName={styles.tabSelected} selectedIndex={this.state.mainIndex} onSelect={index => this.setState({ mainIndex: index })}>
                    <TabList className={styles.table}>
                        <Tab className={styles.configTabs}>Server Configuration </Tab>
                        <Tab className={styles.configTabs}>Distribution Configuration</Tab>
                    </TabList>
                    <div className={styles.tableBorder}>
                        <TabPanel className={styles.tabContent}>
                            <ServerContent/>
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
