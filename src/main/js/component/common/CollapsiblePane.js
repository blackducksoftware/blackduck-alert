import React, { Component } from 'react';
import PropTypes from 'prop-types';

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
        const iconClass = shouldExpand ? 'fa-minus' : 'fa-plus';
        return (
            <div key={this.props.title} className="collapsiblePanel">
                <button
                    type="button"
                    className="btn btn-link"
                    onClick={this.toggleDisplay}>
                    <span className={`fa ${iconClass} icon`} aria-hidden="true" />
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
