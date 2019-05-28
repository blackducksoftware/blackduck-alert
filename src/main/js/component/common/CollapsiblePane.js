import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class CollapsiblePane extends Component {
    constructor(props) {
        super(props);
        this.state = { expanded: false };
        this.toggleDisplay = this.toggleDisplay.bind(this);
        this.selfExpand = false;
    }

    toggleDisplay() {
        this.selfExpand = true;
        this.setState({ expanded: !this.state.expanded });
    }

    render() {
        let shouldExpand = false;
        const propsExand = this.props.expanded();
        if (this.selfExpand) {
            shouldExpand = this.state.expanded;
        } else {
            // We don't want to use setState here because that would trigger an endless re-render but we need the expanded field to be up to date for when the button takes control of the toggling
            this.state = { expanded: propsExand };
            shouldExpand = propsExand;
        }

        const contentClass = shouldExpand ? 'shown' : 'hidden';
        const iconClass = shouldExpand ? 'minus' : 'plus';
        return (
            <div className="collapsiblePanel">
                <button
                    type="button"
                    className="btn btn-link"
                    onClick={this.toggleDisplay}>
                    <FontAwesomeIcon icon={iconClass} className='icon' size="lg" />
                    {this.props.title}
                </button>
                <div className={contentClass}>
                    {this.props.children}
                </div>
            </div>
        );
    }
}

CollapsiblePane.propTypes = {
    title: PropTypes.string.isRequired,
    expanded: PropTypes.func

};

CollapsiblePane.defaultProps = {
    expanded: () => false
};

export default CollapsiblePane;
