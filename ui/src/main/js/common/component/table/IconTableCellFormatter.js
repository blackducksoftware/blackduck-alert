import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class IconTableCellFormatter extends Component {
    constructor(props) {
        super(props);
        this.onClick = this.onClick.bind(this);
    }

    onClick() {
        const { handleButtonClicked, currentRowSelected } = this.props;
        handleButtonClicked(currentRowSelected);
    }

    render() {
        let { buttonClass, id } = this.props;

        if (buttonClass) {
            buttonClass = `${buttonClass} tableButton`;
        } else {
            buttonClass = 'btn btn-link jobIconButton';
        }

        return (
            <button id={id} className={buttonClass} type="button" title={this.props.buttonText} onClick={this.onClick}>
                <FontAwesomeIcon icon={this.props.buttonIconName} className="alert-icon" size="lg" />
            </button>
        );
    }
}

IconTableCellFormatter.propTypes = {
    id: PropTypes.string,
    currentRowSelected: PropTypes.object.isRequired,
    handleButtonClicked: PropTypes.func.isRequired,
    buttonText: PropTypes.string.isRequired,
    buttonIconName: PropTypes.string.isRequired,
    buttonClass: PropTypes.string
};

IconTableCellFormatter.defaultProps = {
    buttonClass: null,
    id: 'iconCellFormatterId'
};

export default IconTableCellFormatter;
