/**
 * @internal
 */
export const normalizeNativePermissionStatus = (nativeStatus) => {
    switch (nativeStatus) {
        case 'ShouldRequest':
            return 'shouldRequest';
        case 'NotDetermined':
        case 'ShouldExplainThenRequest':
            return 'shouldExplainThenRequest';
        case 'Authorized':
        case 'Granted':
            return 'granted';
        case 'Denied':
            return 'denied';
    }
};
