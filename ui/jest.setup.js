import '@testing-library/jest-dom';
import { loadIconData } from './src/main/js/common/util/iconUtility';

/**
 * Load icon data for tests so that FontAwesomeIcon can render icons correctly and not throw errors about missing icons.
 * This is necessary because the icon data is normally loaded in the main application entry point, but in the test environment,
 * we need to load it explicitly.
 */
loadIconData();
