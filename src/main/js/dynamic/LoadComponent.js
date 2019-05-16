import React, { Component } from 'react';
import PropTypes from 'prop-types';

export class LoadComponent extends Component {
    constructor(props) {
        super(props);

        this.state = {
            module: null
        };
    }

    // after the initial render, wait for module to load
    async componentDidMount() {
        // Comment here to improve webpack performance.
        const { default: module } = await import(/* webpackMode: "eager" */ `component/${this.props.componentPath}.js`);
        this.setState({ module });
    }

    render() {
        const { module } = this.state;

        if (!module) return <div>Loading...</div>;
        if (module.view) return React.createElement(module.view);
        return (<div>There was an error retrieving your page.</div>);
    }
}

LoadComponent.propTypes = {
    componentPath: PropTypes.string.isRequired
};

export default LoadComponent;
