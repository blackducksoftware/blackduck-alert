import React from 'react';

export class LoadComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            module: null
        };
    }

    async componentDidMount() {
        const { resolve } = this.props;
        const { default: module } = await resolve();
        this.setState({ module });
    }

    render() {
        const { module } = this.state;

        if (!module) return <div>Loading module...</div>;
        if (module.view) return React.createElement(module.view);
        return null;
    }
}
