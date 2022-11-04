import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import Modal from 'common/component/modal/Modal';
import NumberInput from 'common/component/input/NumberInput';
import TextInput from 'common/component/input/TextInput';
import PasswordInput from 'common/component/input/PasswordInput';
import { GitHubFields } from 'common/util/enums/GitHubEnums';
import { fetchGithub, postGithubConfiguration, validateGitHubConfiguration } from 'store/actions/github';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    descriptorContainer: {
        display: 'flex',
        alignItems: 'center', 
        
    },
    descriptor: {
        fontSize: '14px',
        paddingLeft: '8px'
    }
});

{/* 
Form Fields:
    Name (required): Github Account name
    Api token (required): API token
    Timeout: How long should we wait for the connection to timeout
*/}

const EditGithubRowAction = ({ data, isOpen, toggleModal }) => {
    const classes = useStyles();
    const dispatch = useDispatch();
    const [githubAccountForm, setGithubAccountFormUser] = useState(data);

    const fieldErrors = useSelector(state => state.github.error.fieldErrors);
    const inProgress = useSelector(state => state.github.inProgress);
    const saveStatus = useSelector(state => state.github.saveStatus);

    useEffect(() => {
        if (saveStatus === 'VALIDATED' && !inProgress) { 
            handleSave();
        }
    }, [saveStatus]);

    function clearInputs() {
        setGithubAccountFormUser(data);
    }

    function handleClose() {
        toggleModal(false);
        clearInputs();
        dispatch(fetchGithub());
    }

    const handleOnChange = (label) => {
        return ({ target: { value } }) => {
            setGithubAccountFormUser(userData => ({...userData, [label]: value }));
        }
    }

    function handleSave() {
        dispatch(postGithubConfiguration(githubAccountForm));
        handleClose();
        location.reload();
    }

    function handleSubmit() {
        dispatch(validateGitHubConfiguration(githubAccountForm));
    }

    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title="Edit Github Connection"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Save"
        >   
            <div>
                <div style={{margin: '0 25% 30px 25%'}}>
                    <div className={classes.descriptorContainer}>
                        <FontAwesomeIcon icon="exclamation-circle" size="2x" />
                        <span className={classes.descriptor}>
                            Performing this action will edit '{data.name}'
                        </span>
                    </div>
                </div>
                <TextInput
                    id={GitHubFields.USERNAME}
                    name={GitHubFields.USERNAME}
                    label="Github Connection Name"
                    description="The GitHub Username"
                    placeholder="Enter username..."
                    readOnly={false}
                    required={true}
                    onChange={handleOnChange(GitHubFields.USERNAME)}
                    value={githubAccountForm[GitHubFields.USERNAME]}
                    errorName={GitHubFields.USERNAME}
                    errorValue={fieldErrors[GitHubFields.USERNAME]}
                />
                <PasswordInput
                    id={GitHubFields.API_TOKEN}
                    name={GitHubFields.API_TOKEN}
                    label="API Token"
                    description="The GitHub Account's API Token"
                    placeholder="Enter API Token..."
                    readOnly={false}
                    required={true}
                    onChange={handleOnChange(GitHubFields.API_TOKEN)}
                    value={githubAccountForm[GitHubFields.API_TOKEN]}
                    errorName={GitHubFields.API_TOKEN}
                    errorValue={fieldErrors[GitHubFields.API_TOKEN]}
                />
                <NumberInput
                    id={GitHubFields.TIMEOUT}
                    name={GitHubFields.TIMEOUT}
                    label="Timeout"
                    description="The timeout in seconds for all connections to the Black Duck server."
                    readOnly={false}
                    onChange={handleOnChange(GitHubFields.TIMEOUT)}
                    value={githubAccountForm[GitHubFields.TIMEOUT]}
                    errorName={GitHubFields.TIMEOUT}
                    errorValue={fieldErrors[GitHubFields.TIMEOUT]}
                />
            </div>
        </Modal>
    );
};

export default EditGithubRowAction;