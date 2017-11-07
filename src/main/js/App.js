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
        this.state = {index: 0};
    }

    render() {
        return (
            <div>
                <div className={styles.header}>
                    <h1>Blackduck Software Alert</h1>
                </div>
                
                <Tabs selectedIndex={this.state.index} onSelect={index => this.setState({ index })}>
                    <TabList className={styles.table}>
                        <Tab>Hub settings</Tab>
                        <Tab>Channel configuration</Tab>
                    </TabList>
                    <TabPanel className={styles.tabContent}>
                        <GlobalConfiguration />
                    </TabPanel>
                    <TabPanel className={styles.tabContent}>
                        <EmailConfiguration />
                        <HipChatConfiguration />
                        <SlackConfiguration />
                    </TabPanel>
                </Tabs>
            </div>
        )
    }
}


ReactDOM.render(
		<App />, document.getElementById('react')
);