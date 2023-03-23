import { library } from '@fortawesome/fontawesome-svg-core';

import { faCog, faPlus } from '@fortawesome/free-solid-svg-icons';

export default function registerIcons() {
    library.add(
        faPlus,
        faCog
    );
}
