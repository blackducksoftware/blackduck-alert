class MessageLocator {
    constructor(messageMap) {
        this.messageMap = messageMap;
    }

    findMessage(messageKey) {
        const message = messageKey && this.messageMap[messageKey];
        if (!message) {
            // this is for backwards compatibility where we may have a status message.
            return messageKey;
        }
        return message;
    }

    findFieldMessage(fieldErrorObject) {
        const messageKey = fieldErrorObject && fieldErrorObject.messageKey;
        const message = messageKey && this.messageMap[messageKey];
        if (!message) {
            // this is for backwards compatibility where we may have a status message.
            return {
                ...fieldErrorObject
            };
        }
        return {
            ...fieldErrorObject,
            fieldMessage: message
        };
    }
}

export default MessageLocator;
