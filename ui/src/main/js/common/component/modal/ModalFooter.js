import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import Button from 'common/component/button/Button';

const useStyles = createUseStyles({
    modalFooter: {
        fontSize: '20px',
        padding: '10px',
        display: 'flex',
        borderBottomLeftRadius: '5px',
        borderBottomRightRadius: '5px',
        position: 'relative',
        backgroundColor: '#eeeeee',
        borderTop: 'solid 1px #D6D6D6'
    },
    footerActions: {
        display: 'flex',
        margin: [0, '5px', 0, 'auto']
    },
    loader: {
        position: 'absolute',
        right: '195px',
        top: '2px'
    }
});

const ModalFooter = ({ handleCancel, handleSubmit, handleTest, submitText, buttonStyle = 'default',
    showLoader, testText, disableSubmit, submitTitle }) => {
    const classes = useStyles();

    return (
        <div className={classes.modalFooter}>
            <div className={classes.footerActions}>
                { handleTest && (
                    <Button
                        onClick={handleTest}
                        text={testText}
                        buttonStyle="transparent"
                        showLoader={showLoader === 'test'}
                    />
                )}

                { (handleCancel && !handleTest) && (
                    <Button
                        onClick={handleCancel}
                        text="Cancel" 
                        buttonStyle="transparent"
                        showLoader={showLoader === 'cancel'}
                    />
                )}
                <Button
                    onClick={handleSubmit}
                    text={submitText}
                    buttonStyle={buttonStyle}
                    showLoader={showLoader === 'save' || showLoader}
                    isDisabled={disableSubmit}
                    title={submitTitle}
                />
            </div>
        </div>
    );
};

ModalFooter.propTypes = {
    handleCancel: PropTypes.func,
    handleSubmit: PropTypes.func,
    handleTest: PropTypes.func,
    showLoader: PropTypes.oneOfType([PropTypes.string, PropTypes.bool]),
    submitText: PropTypes.string,
    buttonStyle: PropTypes.string,
    testText: PropTypes.string,
    disableSubmit: PropTypes.bool,
    submitTitle: PropTypes.string
};

export default ModalFooter;
