import React, { Component } from "react";
import PropTypes from "prop-types";

class CollapsiblePane extends Component {
    constructor(props) {
        super(props)
        this.state = { expanded: this.props.expanded }
        this.toggleDisplay = this.toggleDisplay.bind(this);
    }

    toggleDisplay() {
        this.setState({ expanded: !this.state.expanded })
    }

    render() {
        const contentClass = this.state.expanded ? 'shown' : 'hidden';
        return (
            <div className="collapsiblePane">
                <div className="title" onClick={this.toggleDisplay}>
                    {this.props.titleComponent}
                </div>
                <div className={contentClass}>
                    {this.props.children}
                </div>
            </div>
        );
    }
}

CollapsiblePane.propTypes = {
    titleComponent: PropTypes.object.isRequired,
    expanded: PropTypes.bool
};

CollapsiblePane.defaultProps = {
    titleComponent: '',
    expanded: false
};

export default CollapsiblePane;
