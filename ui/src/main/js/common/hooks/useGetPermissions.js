import {
    isOperationAssigned,
    isOneOperationAssigned,
    OPERATIONS
} from 'common/util/descriptorUtilities';

/**
 * useGetPermissions - Custom hook to retrieve permissions for a given descriptor.
 * @param {object} descriptor - The descriptor object for which permissions are being evaluated.
 * @returns {canDelete: boolean, canSave: boolean, canTest: boolean, readOnly: boolean} An object containing the permissions for delete, save, test operations, and read only status.
 */

export default function useGetPermissions(descriptor) {
    if (!descriptor) {
        return {
            canDelete: false,
            canSave: false,
            canTest: false,
            readOnly: true
        };
    }

    const canDelete = isOperationAssigned(descriptor, OPERATIONS.DELETE);
    const canSave = isOneOperationAssigned(descriptor, [OPERATIONS.CREATE, OPERATIONS.WRITE]);
    const canTest = isOperationAssigned(descriptor, OPERATIONS.EXECUTE);

    const readOnly = descriptor.readOnly;

    return {
        canDelete,
        canSave,
        canTest,
        readOnly
    };
}
