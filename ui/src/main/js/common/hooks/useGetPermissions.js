import { 
    isOperationAssigned,
    isOneOperationAssigned,
    DESCRIPTOR_TYPE,
    OPERATIONS
} from 'common/util/descriptorUtilities';

/**
 * useGetPermissions - Custom hook to retrieve permissions for a given descriptor.
 * @param {object} descriptor - The descriptor object for which permissions are being evaluated.
 * @param {boolean} hasTestFields - Flag indicating whether the descriptor has test fields.
 * @returns {canDelete: boolean, canSave: boolean, canTest: boolean, readOnly: boolean} An object containing the permissions for delete, save, test operations, and read only status.
 */

export default function useGetPermissions(descriptor, hasTestFields) {
    if (!descriptor) {
        return {
            canDelete: false,
            canSave: false,
            canTest: false,
            readOnly: true
        };
    }

    const { type } = descriptor;
    const includeTestButton = (type !== DESCRIPTOR_TYPE.COMPONENT) || hasTestFields;

    const canDelete = isOperationAssigned(descriptor, OPERATIONS.DELETE) && (type !== DESCRIPTOR_TYPE.COMPONENT);
    const canSave = isOneOperationAssigned(descriptor, [OPERATIONS.CREATE, OPERATIONS.WRITE]);
    const canTest = isOperationAssigned(descriptor, OPERATIONS.EXECUTE) && includeTestButton;

    const readOnly = descriptor.readOnly;

    return {
        canDelete,
        canSave,
        canTest,
        readOnly
    };
}
