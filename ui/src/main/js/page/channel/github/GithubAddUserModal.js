import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Modal from 'common/component/modal/Modal';
import NumberInput from 'common/component/input/NumberInput';
import TextInput from 'common/component/input/TextInput';
import PasswordInput from 'common/component/input/PasswordInput';
import { GitHubFields } from 'common/util/enums/GitHubEnums';
import { fetchGithub, postGithubConfiguration, validateGitHubConfiguration } from 'store/actions/github';

{/* 
Form Fields:
    Name (required): Github Account name
    Api token (required): API token
    Timeout: How long should we wait for the connection to timeout
*/}

const GithubAddUserModal = ({ isOpen, toggleModal }) => {
    const dispatch = useDispatch();
    const [newGitHubUser, setNewGitHubUser] = useState({timeoutInSeconds: 300});

    const fieldErrors = useSelector(state => state.github.error.fieldErrors);
    const inProgress = useSelector(state => state.github.inProgress);
    const saveStatus = useSelector(state => state.github.saveStatus);

    useEffect(() => {
        if (saveStatus === 'VALIDATED' && !inProgress) { 
            handleSave();
        }
    }, [saveStatus]);

    function clearInputs() {
        setNewGitHubUser({timeoutInSeconds: 300});
    }

    function handleClose() {
        toggleModal(false);
        clearInputs();
        dispatch(fetchGithub());
    }

    const handleOnChange = (label) => {
        return ({ target: { value } }) => {
            setNewGitHubUser(userData => ({...userData, [label]: value }));
        }
    }

    function handleSave() {
        dispatch(postGithubConfiguration(newGitHubUser));
        handleClose();
        location.reload();
    }

    function handleSubmit() {
        dispatch(validateGitHubConfiguration(newGitHubUser));
    }

    return (
        <Modal 
            isOpen={isOpen} 
            size="lg" 
            title="Add Github Connection"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Save"
        >   
            <div>
                <TextInput
                    id={GitHubFields.USERNAME}
                    name={GitHubFields.USERNAME}
                    label="Github Connection Name"
                    description="The GitHub Username"
                    placeholder="Enter username..."
                    readOnly={false}
                    required={true}
                    onChange={handleOnChange(GitHubFields.USERNAME)}
                    value={newGitHubUser[GitHubFields.USERNAME]}
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
                    value={newGitHubUser[GitHubFields.API_TOKEN]}
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
                    value={newGitHubUser[GitHubFields.TIMEOUT]}
                    errorName={GitHubFields.TIMEOUT}
                    errorValue={fieldErrors[GitHubFields.TIMEOUT]}
                />
            </div>
        </Modal>
    );
};

export default GithubAddUserModal;