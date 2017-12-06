'use strict';

import React, { Component } from 'react';
import { channelTabs, bg_icon } from '../../../css/tabs.css';

class TabTitle extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const { text, icon } = this.props;
        let iconElement = null;
        if(icon) {
            const fontAwesomeIcon = `fa ${icon}`;
            iconElement = <i className={fontAwesomeIcon} aria-hidden='true'></i>;
        }

        return (
            <span>
                {iconElement}
                <span>{text}</span>
            </span>
        );
    }
};

export default TabTitle;
