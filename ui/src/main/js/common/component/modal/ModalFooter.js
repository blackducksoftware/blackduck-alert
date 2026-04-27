import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import Button from 'common/component/button/Button';

const useStyles = createUseStyles((theme) => ({
    modalFooter: {
        fontSize: '20px',
        padding: ['10px', '30px', '20px'],
        display: 'flex',
        borderBottomLeftRadius: theme.modal.modalBorderRadius,
        borderBottomRightRadius: theme.modal.modalBorderRadius,
        position: 'relative'
    },
    footerActions: {
        display: 'flex',
        margin: [0, '5px', 0, 'auto'],
        justifyContent: 'end',
        alignItems: 'center',
        borderTop: `1px solid ${theme.colors.defaultBackgroundColor}`,
        width: '100%',
        paddingTop: '10px'
    },
    loader: {
        position: 'absolute',
        right: '195px',
        top: '2px'
    }
}));

const ModalFooter = ({ handleCancel, handleSubmit, handleTest, submitText, buttonStyle = 'action',
    showLoader, testText, disableSubmit, disableTest = false, submitTitle }) => {
    const classes = useStyles();

    return (
        <div className={classes.modalFooter}>
            <div className={classes.footerActions}>
                { handleTest && !disableTest && (
                    <Button
                        onClick={handleTest}
                        text={testText}
                        buttonStyle="actionSecondary"
                        showLoader={showLoader === 'test'}
                    />
                )}

                { (handleCancel && !handleTest) && (
                    <Button
                        onClick={handleCancel}
                        text="Cancel"
                        buttonStyle="actionSecondary"
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
    disableTest: PropTypes.bool,
    submitTitle: PropTypes.string
};

export default ModalFooter;
