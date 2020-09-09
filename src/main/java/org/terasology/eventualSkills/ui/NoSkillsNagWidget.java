// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.ui;

import com.google.common.base.Strings;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UILabel;

public class NoSkillsNagWidget extends CoreHudWidget {
    UILabel message;

    @Override
    public void initialise() {
        message = find("message", UILabel.class);
        if (message != null) {
            message.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    EntityRef targetEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                    EntitySkillsComponent skillsComponent = targetEntity.getComponent(EntitySkillsComponent.class);
                    if (skillsComponent == null || skillsComponent.learnedSkills.size() == 0) {
                        return "You have no skills, press 'L' to start training";
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
