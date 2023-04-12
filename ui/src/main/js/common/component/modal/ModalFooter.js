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

const ModalFooter = ({ handleCancel, handleSubmit, handleTest, submitText, style = 'default', 
    showLoader, testText, disableSubmit, submitTitle
}) => {
    const classes = useStyles();

    return (
        <div className={classes.modalFooter}>
            <div className={classes.footerActions}>
                { handleTest && (
                    <Button 
                        onClick={handleTest}
                        text={testText}
                        style="transparent"
                        showLoader={showLoader === 'test'}
                    />
                )}

                { (handleCancel && !handleTest) && (
                    <Button 
                        onClick={handleCancel}
                        text="Cancel" style="transparent"
                        showLoader={showLoader === 'cancel'}
                    />
                )}
                <Button 
                    onClick={handleSubmit}
                    text={submitText}
                    style={style}
                    showLoader={showLoader === 'save'}
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
    showLoader: PropTypes.bool,
    submitText: PropTypes.string,
    style: PropTypes.string,
    testText: PropTypes.string
};

export default ModalFooter;
