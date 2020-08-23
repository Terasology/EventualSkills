/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.eventualSkills.ui;

import org.terasology.nui.UIWidget;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UILabel;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.eventualSkills.components.EventualSkillDescriptionComponent;
import org.terasology.eventualSkills.systems.EventualSkillsManager;
import org.terasology.registry.CoreRegistry;

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
