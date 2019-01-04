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
        const iconClass = this.state.expanded ? 'fa-minus' : 'fa-plus';
        return (
            <div className="collapsiblePanel">
                <button
                    id="collapsiblePaneButton"
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
    expanded: PropTypes.bool

};

CollapsiblePane.defaultProps = {
    title: '',
    expanded: false
};

export default CollapsiblePane;
