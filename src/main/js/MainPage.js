'use strict';

import React from 'react';
import GlobalConfiguration from './component/GlobalConfiguration';
import EmailConfiguration from './component/EmailConfiguration';
import HipChatConfiguration from './component/HipChatConfiguration';
import SlackConfiguration from './component/SlackConfiguration';

import logo from '../img/BDTextLogo.png';

import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

class MainPage extends React.Component {
	constructor() {
		super();
		this.state = {
				mainIndex: 0,
				channelIndex: 0
		};
	}

	render() {
		return (
				<div className={styles.wrapper}>
				<div className={styles.header}>
				<img src={logo} alt="logo" />
				</div>
				<div className={styles.alertHeader}>
				<h1 className={styles.alertHeaderTag}>Black Duck Alert</h1>
				</div>

				<Tabs className={styles.tabContainer} selectedTabClassName={styles.tabSelected} selectedIndex={this.state.mainIndex} onSelect={index => this.setState({ mainIndex: index })}>
				<TabList className={styles.table}>
				<Tab className={styles.configTabs}>Hub settings</Tab>
				<Tab className={styles.configTabs}>Channel configuration</Tab>
				</TabList>
				<div className={styles.tableBorder}>
				<TabPanel className={styles.tabContent}>
				<GlobalConfiguration />
				</TabPanel>
				<TabPanel className={styles.tabContent}>
				<Tabs selectedTabClassName={styles.tabSelected} selectedIndex={this.state.channelIndex} onSelect={index => this.setState({ channelIndex: index })}>
				<TabList className={styles.table}>
				<Tab className={styles.channelTabs}>Email</Tab>
				<Tab className={styles.channelTabs}>Hipchat</Tab>
				<Tab className={styles.channelTabs}>Slack</Tab>
				</TabList>
				<TabPanel>
				<EmailConfiguration />
				</TabPanel>
				<TabPanel>
				<HipChatConfiguration />
				</TabPanel>
				<TabPanel>
				<SlackConfiguration />
				</TabPanel>
				</Tabs>
				</TabPanel>
				</div>
				</Tabs>
				</div>
		)
	}
}

export default MainPage;