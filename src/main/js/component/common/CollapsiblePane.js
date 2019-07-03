import React, { Component } from 'react';
import PropTypes from 'prop-types';

class CollapsiblePane extends Component {
    constructor(props) {
        super(props);

        this.state = ({ expanded: this.props.expanded });
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (!prevProps.expanded && this.props.expanded) {
            this.setState({
                expanded: this.props.expanded
            });
        }
    }

    render() {
        const { expanded } = this.state;
        const contentClass = expanded ? 'shown' : 'hidden';
        const iconClass = expanded ? 'fa-minus' : 'fa-plus';
        return (
            <div className="collapsiblePanel">
                <button
                    type="button"
                    className="btn btn-link"
                    onClick={() => this.setState({ expanded: !this.state.expanded })}
                >
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
    expanded: PropTypes.bool,
    children: PropTypes.array.isRequired
};

CollapsiblePane.defaultProps = {
    expanded: false
};

export default CollapsiblePane;
