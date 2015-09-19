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

import com.google.common.base.Strings;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.eventualSkills.components.EntityEventualSkillsComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UILabel;

public class NoSkillsNagWidget extends CoreHudWidget {
    UILabel message;

    @Override
    protected void initialise() {
        message = find("message", UILabel.class);
        if (message != null) {
            message.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    EntityRef targetEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                    EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                    if (targetSkills == null || targetSkills.learnedSkills.size() == 0) {
                        return "You have no skills, press 'K' to start training";
                    }
                    return null;
                }

            });
        }
        bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                if (message != null) {
                    return !Strings.isNullOrEmpty(message.getText());
                } else {
                    return false;
                }
            }
        });
    }
}
