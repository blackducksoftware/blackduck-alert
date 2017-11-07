'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import GlobalConfiguration from './component/GlobalConfiguration';
import EmailConfiguration from './component/EmailConfiguration';
import HipChatConfiguration from './component/HipChatConfiguration';
import SlackConfiguration from './component/SlackConfiguration';

import styles from '../css/main.css';

import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

class App extends React.Component {
    constructor() {
        super();
        this.state = {
            mainIndex: 0,
            channelIndex: 0
        };
    }

    render() {
        return (
            <div>
                <div className={styles.header}>
                    <h1>Black Duck Alert</h1>
                </div>
                
                <Tabs selectedIndex={this.state.mainIndex} onSelect={index => this.setState({ mainIndex: index })}>
                    <TabList className={styles.table}>
                        <Tab className={styles.tabSpacing}>Hub settings</Tab>
                        <Tab className={styles.tabSpacing}>Channel configuration</Tab>
                    </TabList>
                    <TabPanel className={styles.tabContent}>
                        <GlobalConfiguration />
                    </TabPanel>
                    <TabPanel className={styles.tabContent}>
                        <Tabs selectedIndex={this.state.channelIndex} onSelect={index => this.setState({ channelIndex: index })}>
                            <TabList className={styles.table}>
                                <Tab>Email</Tab>
                                <Tab>Hipchat</Tab>
                                <Tab>Slack</Tab>
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
                </Tabs>
            </div>
        )
    }
}


ReactDOM.render(
		<App />, document.getElementById('react')
);