// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.ui;

import org.terasology.assets.ResourceUrn;
import org.terasology.eventualSkills.components.EventualSkillDescriptionComponent;
import org.terasology.eventualSkills.systems.EventualSkillsManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UILabel;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.Assets;

public final class EventualSkillsUIUtil {
    public static UIWidget createEventualSkillsIcon(ResourceUrn skillUrn, int level) {
        EventualSkillsManager eventualSkillsManager = CoreRegistry.get(EventualSkillsManager.class);
        EventualSkillDescriptionComponent skillDescriptionComponent = eventualSkillsManager.getSkill(skillUrn);

        UIImage image = new UIImage(Assets.getTextureRegion("EventualSkills:EventualSkills#skill").get());
        UILabel label = new UILabel(String.valueOf(skillDescriptionComponent.shortName + "\n" + level));
        OverlapLayout layout = new OverlapLayout();
        layout.addWidget(image);
        layout.addWidget(label);
        layout.setTooltip(skillDescriptionComponent.name);
        layout.setTooltipDelay(0);

        return layout;
    }
}
